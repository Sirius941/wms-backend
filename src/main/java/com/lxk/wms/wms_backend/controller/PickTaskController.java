package com.lxk.wms.wms_backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lxk.wms.wms_backend.common.Result;
import com.lxk.wms.wms_backend.entity.PickTask;
import com.lxk.wms.wms_backend.service.PickTaskService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "拣货任务管理接口", description = "拣货任务的创建、完成、分页查询、删除")
@RestController
@RequestMapping("/pick-task")
public class PickTaskController {

    @Autowired
    private PickTaskService pickTaskService;

    // 1. 创建拣货任务
    @PostMapping("/save")
    public Result<?> save(@RequestBody PickTask task) {
        try {
            pickTaskService.createTask(task);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 2. 完成任务 (点击按钮 -> 扣库存)
    @PostMapping("/complete/{id}")
    public Result<?> complete(@PathVariable Long id) {
        try {
            pickTaskService.completeTask(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 3. 分页查询
    @GetMapping("/page")
    public Result<IPage<PickTask>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long outboundId,
            @RequestParam(required = false) Integer status
    ) {
        return Result.success(pickTaskService.pageQuery(pageNum, pageSize, outboundId, status));
    }

    // 4. 删除
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        PickTask task = pickTaskService.getById(id);
        if (task.getStatus() == 1) {
            return Result.error("已完成的任务无法删除");
        }
        pickTaskService.removeById(id);
        return Result.success();
    }
}