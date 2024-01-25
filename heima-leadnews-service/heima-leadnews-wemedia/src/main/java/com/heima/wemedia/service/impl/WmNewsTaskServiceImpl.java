package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.apis.schedule.IScheduleClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.TaskTypeEnum;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.utils.common.ProtostuffUtil;
import com.heima.wemedia.service.WmNewsAutoScanService;
import com.heima.wemedia.service.WmNewsTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class WmNewsTaskServiceImpl implements WmNewsTaskService {
    @Autowired
    private IScheduleClient scheduleClient;

    /**
     * 添加任务到延迟队列中
     *
     * @param id          任务id
     * @param publishTime 任务的执行时间
     */
    @Override
    @Async
    public void addNewsToTask(Integer id, Date publishTime) {
        log.info("添加任务到延迟服务中...");

        Task task = new Task();
        task.setExecuteTime(publishTime.getTime());
        task.setTaskType(TaskTypeEnum.REMOTEERROR.getTaskType());
        task.setPriority(TaskTypeEnum.REMOTEERROR.getPriority());
        WmNews wmNews = new WmNews();
        wmNews.setId(id);
        task.setParameters(ProtostuffUtil.serialize(wmNews));

        scheduleClient.addTask(task);

        log.info("添加任务到延迟服务成功");
    }

    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;

    /**
     * 消费任务 审核文章
     */
    @Override
    @Scheduled(fixedRate = 1000) //每秒钟拉取一次
    public void scanNewsByTask() {
        log.info("自动审核文章");

        ResponseResult responseResult = scheduleClient.pull
                (TaskTypeEnum.REMOTEERROR.getTaskType(), TaskTypeEnum.REMOTEERROR.getPriority());
        if (responseResult.getCode().equals(200) && responseResult.getData() != null) {
            //转为JSON字符串 然后转换为Task对象
            Task task = JSON.parseObject(JSON.toJSONString(responseResult.getData()), Task.class);
            //反序列化
            WmNews wmNews = ProtostuffUtil.deserialize(task.getParameters(), WmNews.class);
            wmNewsAutoScanService.autoScanNews(wmNews.getId());
        }
    }
}
