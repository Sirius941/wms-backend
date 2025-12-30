package com.lxk.wms.wms_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lxk.wms.wms_backend.common.Result;
import com.lxk.wms.wms_backend.entity.StorageType;
import com.lxk.wms.wms_backend.service.StorageTypeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "存放类型管理接口", description = "存放类型的增删改查接口")
@RestController
@RequestMapping("/storage-type") // URL 建议用中划线
public class StorageTypeController {

    @Autowired
    private StorageTypeService storageTypeService;

    // 1. 新增/编辑
    @PostMapping("/save")
    public Result<?> save(@RequestBody StorageType storageType) {
        try {
            storageTypeService.saveStorageType(storageType);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 2. 删除
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        storageTypeService.removeById(id);
        return Result.success();
    }

    // 3. 修改状态
    @PostMapping("/status/{id}/{status}")
    public Result<?> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        StorageType storageType = new StorageType();
        storageType.setId(id);
        storageType.setStatus(status);
        storageTypeService.updateById(storageType);
        return Result.success();
    }

    // 4. 分页查询
    @GetMapping("/page")
    public Result<IPage<StorageType>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status
    ) {
        return Result.success(storageTypeService.pageQuery(pageNum, pageSize, name, status));
    }

    // 5. 获取所有可用类型 (用于下拉框)
    @GetMapping("/list")
    public Result<List<StorageType>> list() {
        LambdaQueryWrapper<StorageType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StorageType::getStatus, 1);
        return Result.success(storageTypeService.list(wrapper));
    }

    // 6. 详情
    @GetMapping("/{id}")
    public Result<StorageType> getById(@PathVariable Long id) {
        return Result.success(storageTypeService.getById(id));
    }
}