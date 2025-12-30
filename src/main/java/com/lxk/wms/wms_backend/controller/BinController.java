package com.lxk.wms.wms_backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lxk.wms.wms_backend.common.Result;
import com.lxk.wms.wms_backend.entity.Bin;
import com.lxk.wms.wms_backend.service.BinService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "库位管理接口", description = "库位的新增/编辑、删除、状态修改、分页查询、详情")
@RestController
@RequestMapping("/bin")
public class BinController {

    @Autowired
    private BinService binService;

    // 1. 新增/编辑
    @PostMapping("/save")
    public Result<?> save(@RequestBody Bin bin) {
        try {
            binService.saveBin(bin);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 2. 删除
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        binService.removeById(id);
        return Result.success();
    }

    // 3. 修改状态 (启用/禁用)
    @PostMapping("/status/{id}/{status}")
    public Result<?> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        Bin bin = new Bin();
        bin.setId(id);
        bin.setStatus(status);
        binService.updateById(bin);
        return Result.success();
    }

    // 4. 分页查询 (全能查询接口)
    @GetMapping("/page")
    public Result<IPage<Bin>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String binCode,     // 库位编码
            @RequestParam(required = false) Long warehouseId,   // 仓库
            @RequestParam(required = false) Long zoneId,        // 库区
            @RequestParam(required = false) Long rackId,        // 货架
            @RequestParam(required = false) Integer status,     // 状态(启用/禁用)
            @RequestParam(required = false) Integer usageStatus // 使用状态(空/满)
    ) {
        return Result.success(binService.pageQuery(pageNum, pageSize, binCode, warehouseId, zoneId, rackId, status, usageStatus));
    }

    // 5. 详情
    @GetMapping("/{id}")
    public Result<Bin> getById(@PathVariable Long id) {
        return Result.success(binService.getById(id));
    }
}