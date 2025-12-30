package com.lxk.wms.wms_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lxk.wms.wms_backend.entity.PickTask;

public interface PickTaskService extends IService<PickTask> {
    // 创建拣货任务 (需校验库存)
    void createTask(PickTask task);

    // 完成任务 (扣减库存)
    void completeTask(Long taskId);

    // 分页查询
    IPage<PickTask> pageQuery(Integer pageNum, Integer pageSize, Long outboundId, Integer status);
}