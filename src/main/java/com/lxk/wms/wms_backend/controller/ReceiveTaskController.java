package com.lxk.wms.wms_backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lxk.wms.wms_backend.common.Result;
import com.lxk.wms.wms_backend.entity.ReceiveTask;
import com.lxk.wms.wms_backend.service.ReceiveTaskService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "收货任务管理接口", description = "收货任务的创建、完成、分页查询、取消/删除")
@RestController
@RequestMapping("/receive-task")
public class ReceiveTaskController {

    @Autowired
    private ReceiveTaskService receiveTaskService;

    // 1. 创建收货任务 (对应图2,3,4)
    // 前端需传: inboundId, inboundDetailId, productId, binId, quantity
    @PostMapping("/save")
    public Result<?> save(@RequestBody ReceiveTask task) {
        try {
            receiveTaskService.createTasks(task);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 2. 完成任务 (对应图5的操作按钮)
    // 点击"完成"后，库存才会增加
    @PostMapping("/complete/{id}")
    public Result<?> complete(@PathVariable Long id) {
        try {
            receiveTaskService.completeTask(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 3. 分页查询任务列表
    @GetMapping("/page")
    public Result<IPage<ReceiveTask>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long inboundId, // 按入库单查
            @RequestParam(required = false) Integer status  // 按状态查(0待执行/1已完成)
    ) {
        return Result.success(receiveTaskService.pageQuery(pageNum, pageSize, inboundId, status));
    }

    // 4. 取消/删除任务
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        // 只有未执行的任务可以删除
        ReceiveTask task = receiveTaskService.getById(id);
        if (task.getStatus() == 1) {
            return Result.error("已完成的任务无法删除");
        }
        receiveTaskService.removeById(id);
        return Result.success();
    }
}