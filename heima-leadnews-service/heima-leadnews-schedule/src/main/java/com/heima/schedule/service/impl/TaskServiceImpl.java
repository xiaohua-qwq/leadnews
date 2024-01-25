package com.heima.schedule.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.common.constants.ScheduleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.schedule.pojos.Taskinfo;
import com.heima.model.schedule.pojos.TaskinfoLogs;
import com.heima.schedule.mapper.TaskinfoLogsMapper;
import com.heima.schedule.mapper.TaskinfoMapper;
import com.heima.schedule.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
        boolean flag = false;
        //更改数据库 删除任务信息并修改日志信息
        Task task = updateDB(taskId, ScheduleConstants.CANCELLED);

        //删除Redis缓存中的任务数据
        if (task != null) {
            removeTaskFromCache(task);
            flag = true;
        }
        return flag;
    }

    private void removeTaskFromCache(Task task) {
        String key = task.getTaskType() + "_" + task.getPriority();
        if (task.getExecuteTime() <= System.currentTimeMillis()) {
            cacheService.lRemove(ScheduleConstants.TOPIC + key, 0, JSON.toJSONString(task));
        } else {
            cacheService.zRemove(ScheduleConstants.FUTURE + key, JSON.toJSONString(task));
        }
    }

    private Task updateDB(long taskId, int status) {
        Task task = null;
        try {
            taskinfoMapper.deleteById(taskId); //删除数据库中的任务

            //更新数据库任务日志表 设置任务状态
            TaskinfoLogs taskinfoLogs = taskinfoLogsMapper.selectById(taskId);
            taskinfoLogs.setStatus(status);
            taskinfoLogsMapper.updateById(taskinfoLogs);

            task = new Task();
            BeanUtils.copyProperties(taskinfoLogs, task);
            task.setExecuteTime(taskinfoLogs.getExecuteTime().getTime());
        } catch (Exception e) {
            log.error("更新任务状态时出现异常TaskId:{}", taskId);
        }

        return task;
    }

    @Override
    public Task pull(int type, int priority) {
        Task task = null;
        try {
            String key = type + "_" + priority; //组装Key
            String task_json = cacheService.lRightPop(ScheduleConstants.TOPIC + key); //取出任务
            if (StringUtils.isNotBlank(task_json)) {
                task = JSON.parseObject(task_json, Task.class);
                this.updateDB(task.getTaskId(), ScheduleConstants.EXECUTED);
            }
        } catch (Exception e) {
            log.error("取出任务时出现异常:{}", e.getMessage());
        }
        return task;
    }

    @Override
    @Scheduled(cron = "0 */1 * * * ?") //定时任务 每分钟执行一次
    public void refresh() {
        //尝试加锁 如果加锁成功则继续操作
        String token = cacheService.tryLock("FUTURE_TASK_SYNC", 1000 * 30);
        if (StringUtils.isNotBlank(token)) {
            log.info("开始扫描定时任务");
            //扫描所有将要执行的任务
            Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");
            for (String futureKey : futureKeys) {
                //切割原来的带有时间的FutureKey并组装成TopicKey
                String topicKey = ScheduleConstants.TOPIC + futureKey.split(ScheduleConstants.FUTURE)[1];
                //检查任务的执行时间有没有小于等于当前系统时间 如果有则代表需要将其加入List中
                Set<String> tasks = cacheService.zRangeByScore(futureKey, 0, System.currentTimeMillis());
                if (!tasks.isEmpty()) {
                    cacheService.refreshWithPipeline(futureKey, topicKey, tasks);
                    log.info("已成功同步{}到待执行列表中 当前Key为{}", futureKey, topicKey);
                }
            }
        }
    }

    @PostConstruct //标明此类为初始化类 当服务器启动时则执行
    @Scheduled(cron = "0 */5 * * * ?") //每五分钟执行一次
    public void reloadData() {
        //清除Redis缓存
        this.clearCache();

        //从数据库中加载要执行的数据
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        List<Taskinfo> taskInfoList = taskinfoMapper.selectList
                (Wrappers.<Taskinfo>lambdaQuery().lt(Taskinfo::getExecuteTime, calendar.getTime()));

        //将新的待执行列表添加到Redis中
        if (taskInfoList != null && taskInfoList.size() > 0) {
            for (Taskinfo taskinfo : taskInfoList) {
                Task task = new Task();
                BeanUtils.copyProperties(taskinfo, task);
                task.setExecuteTime(taskinfo.getExecuteTime().getTime());
                this.addTaskToCache(task);
            }
        }

        log.info("已将数据库任务同步到Redis");
    }

    /**
     * 清除Redis缓存
     */
    public void clearCache() {
        Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");
        Set<String> topicKeys = cacheService.scan(ScheduleConstants.TOPIC + "*");
        cacheService.delete(futureKeys);
        cacheService.delete(topicKeys);
    }
}
