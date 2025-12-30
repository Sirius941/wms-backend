package com.lxk.wms.wms_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lxk.wms.wms_backend.common.Result;
import com.lxk.wms.wms_backend.entity.Rack;
import com.lxk.wms.wms_backend.service.RackService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="RackController", description="货架管理接口")
@RestController
@RequestMapping("/rack")
public class RackController {

    @Autowired
    private RackService rackService;

    // 1. 新增/编辑
    @PostMapping("/save")
    public Result<?> save(@RequestBody Rack rack) {
        try {
            rackService.saveRack(rack);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 2. 删除
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        rackService.removeById(id);
        return Result.success();
    }

    // 3. 修改状态
    @PostMapping("/status/{id}/{status}")
    public Result<?> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        Rack rack = new Rack();
        rack.setId(id);
        rack.setStatus(status);
        rackService.updateById(rack);
        return Result.success();
    }

    // 4. 分页查询 (含多重筛选)
    @GetMapping("/page")
    public Result<IPage<Rack>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Integer status
    ) {
        return Result.success(rackService.pageQuery(pageNum, pageSize, name, warehouseId, zoneId, status));
    }

    // 5. 根据库区查询货架 (级联用：选了库区 -> 出现该库区的货架)
    @GetMapping("/list/{zoneId}")
    public Result<List<Rack>> listByZone(@PathVariable Long zoneId) {
        LambdaQueryWrapper<Rack> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Rack::getZoneId, zoneId);
        wrapper.eq(Rack::getStatus, 1);
        return Result.success(rackService.list(wrapper));
    }

    @GetMapping("/{id}")
    public Result<Rack> getById(@PathVariable Long id) {
        return Result.success(rackService.getById(id));
    }
}