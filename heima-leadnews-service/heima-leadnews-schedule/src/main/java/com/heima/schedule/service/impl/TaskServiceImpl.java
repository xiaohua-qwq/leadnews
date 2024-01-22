package com.heima.schedule.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.common.constants.ScheduleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.schedule.pojos.Taskinfo;
import com.heima.model.schedule.pojos.TaskinfoLogs;
import com.heima.schedule.mapper.TaskinfoLogsMapper;
import com.heima.schedule.mapper.TaskinfoMapper;
import com.heima.schedule.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;

@Service
@Transactional
@Slf4j
public class TaskServiceImpl implements TaskService {
    /**
     * 添加延迟任务
     */
    @Override
    public long addTask(Task task) {
        //1.把任务添加到数据库
        boolean success = addTaskToDB(task);

        //2.从数据库获取任务放入Redis队列中
        if (success) {
            addTaskToCache(task);
        }

        return task.getTaskId(); //返回Task任务id
    }

    @Autowired
    private CacheService cacheService;

    private void addTaskToCache(Task task) {
        String key = task.getTaskType() + "_" + task.getPriority();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        long nextScheduleTime = calendar.getTimeInMillis(); //计算当前时间延后五分钟的时间

        //2.1任务预期执行时间小于等于当前时间 放入List中
        if (task.getExecuteTime() <= System.currentTimeMillis()) {
            cacheService.lLeftPush(ScheduleConstants.TOPIC + key, JSON.toJSONString(task));
        } else if (task.getExecuteTime() <= nextScheduleTime) {
            //2.2任务预期执行时间大于当前时间 && 小于预期时间(当前时间 + 5分钟) 放入Zset中
            cacheService.zAdd(ScheduleConstants.FUTURE + key, JSON.toJSONString(task), task.getExecuteTime());
        }

    }

    @Autowired
    private TaskinfoMapper taskinfoMapper;
    @Autowired
    private TaskinfoLogsMapper taskinfoLogsMapper;

    private boolean addTaskToDB(Task task) {
        boolean flag = false;

        try {
            //添加任务信息
            Taskinfo taskinfo = new Taskinfo();
            org.springframework.beans.BeanUtils.copyProperties(task, taskinfo);
            taskinfo.setExecuteTime(new Date(task.getExecuteTime()));
            taskinfoMapper.insert(taskinfo);

            //获取Task任务id
            task.setTaskId(taskinfo.getTaskId());

            //记录任务日志
            TaskinfoLogs taskinfoLogs = new TaskinfoLogs();
            org.springframework.beans.BeanUtils.copyProperties(taskinfo, taskinfoLogs);
            taskinfoLogs.setVersion(1);
            taskinfoLogs.setStatus(ScheduleConstants.SCHEDULED);
            taskinfoLogsMapper.insert(taskinfoLogs);
            flag = true;
        } catch (Exception e) {
            log.error("添加延时任务到数据库时出现异常: " + e.getMessage());
        }
        return flag;
    }

    @Override
    public boolean cancelTask(long taskId) {
        //更改数据库 删除任务信息并修改日志信息
        Task task = updateDB(taskId, ScheduleConstants.CANCELLED);
        return false;
    }

    private Task updateDB(long taskId, int status) {
        taskinfoMapper.deleteById(taskId); //删除数据库中的任务

        //更新数据库任务日志表 设置为已取消
        TaskinfoLogs taskinfoLogs = taskinfoLogsMapper.selectById(taskId);
        taskinfoLogs.setStatus(status);
        taskinfoLogsMapper.updateById(taskinfoLogs);

        Task task = new Task();
        BeanUtils.copyProperties(taskinfoLogs, task);
        task.setExecuteTime(taskinfoLogs.getExecuteTime().getTime());

        return task;
    }
}
