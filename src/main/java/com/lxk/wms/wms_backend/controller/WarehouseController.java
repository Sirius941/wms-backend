package com.lxk.wms.wms_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lxk.wms.wms_backend.common.Result; // 确保 Result 类也在 common 包下
import com.lxk.wms.wms_backend.entity.Warehouse;
import com.lxk.wms.wms_backend.service.WarehouseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "仓库管理接口", description = "仓库的增删改查接口")
@RestController
@RequestMapping("/warehouse")
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    // 1. 新增/编辑 接口 (二合一)
    // 前端注意：点击"编辑"时，传给后端的 JSON 必须包含 "id" 字段
    @PostMapping("/save")
    public Result<?> save(@RequestBody Warehouse warehouse) {
        try {
            // 调用业务逻辑，这里面已经包含了"编辑时的唯一性校验"
            warehouseService.saveWarehouse(warehouse);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 2. 删除 (逻辑删除)
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        warehouseService.removeById(id);
        return Result.success();
    }

    // 3. 修改状态 (启用/禁用)
    @PostMapping("/status/{id}/{status}")
    public Result<?> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(id);
        warehouse.setStatus(status);
        warehouseService.updateById(warehouse);
        return Result.success();
    }

    // 4. 分页查询
    @GetMapping("/page")
    public Result<IPage<Warehouse>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status
    ) {
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Warehouse::getWarehouseName, name);
        }
        if (status != null) {
            wrapper.eq(Warehouse::getStatus, status);
        }
        wrapper.orderByDesc(Warehouse::getCreateTime);

        return Result.success(warehouseService.page(new Page<>(pageNum, pageSize), wrapper));
    }

    // 5. 列表查询 (用于下拉框)
    @GetMapping("/list")
    public Result<List<Warehouse>> list() {
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Warehouse::getStatus, 1);
        return Result.success(warehouseService.list(wrapper));
    }

    @GetMapping("/{id}")
    public Result<Warehouse> getById(@PathVariable Long id) {
        return Result.success(warehouseService.getById(id));
    }
}