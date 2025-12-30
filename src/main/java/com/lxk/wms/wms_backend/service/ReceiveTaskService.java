package com.lxk.wms.wms_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lxk.wms.wms_backend.entity.ReceiveTask;

public interface ReceiveTaskService extends IService<ReceiveTask> {
    // 创建任务
    void createTasks(ReceiveTask receiveTask);

    // 完成任务 (核心：会增加库存)
    void completeTask(Long taskId);

    // 查询任务
    IPage<ReceiveTask> pageQuery(Integer pageNum, Integer pageSize, Long inboundId, Integer status);
}