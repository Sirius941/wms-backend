package com.lxk.wms.wms_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lxk.wms.wms_backend.common.Result;
import com.lxk.wms.wms_backend.entity.Unit;
import com.lxk.wms.wms_backend.service.UnitService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="单位管理接口", description="单位管理相关的增删改查接口")
@RestController
@RequestMapping("/unit")
public class UnitController {

    @Autowired
    private UnitService unitService;

    // 1. 新增/编辑 (对应前端示例图2)
    @PostMapping("/save")
    public Result<?> save(@RequestBody Unit unit) {
        try {
            unitService.saveUnit(unit);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 2. 删除
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        unitService.removeById(id);
        return Result.success();
    }

    // 3. 修改状态
    @PostMapping("/status/{id}/{status}")
    public Result<?> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        Unit unit = new Unit();
        unit.setId(id);
        unit.setStatus(status);
        unitService.updateById(unit);
        return Result.success();
    }

    // 4. 分页查询
    @GetMapping("/page")
    public Result<IPage<Unit>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status
    ) {
        return Result.success(unitService.pageQuery(pageNum, pageSize, name, status));
    }

    // 5. 获取所有可用单位 (用于下拉框)
    @GetMapping("/list")
    public Result<List<Unit>> list() {
        LambdaQueryWrapper<Unit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Unit::getStatus, 1);
        return Result.success(unitService.list(wrapper));
    }

    // 6. 详情
    @GetMapping("/{id}")
    public Result<Unit> getById(@PathVariable Long id) {
        return Result.success(unitService.getById(id));
    }
}