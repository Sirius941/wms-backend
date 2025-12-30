package com.lxk.wms.wms_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lxk.wms.wms_backend.common.Result;
import com.lxk.wms.wms_backend.entity.Zone;
import com.lxk.wms.wms_backend.service.ZoneService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "ZoneController", description = "库区管理接口")
@RestController
@RequestMapping("/zone")
public class ZoneController {

    @Autowired
    private ZoneService zoneService;

    // 1. 新增/编辑 (对应图3、图4)
    @PostMapping("/save")
    public Result<?> save(@RequestBody Zone zone) {
        try {
            zoneService.saveZone(zone);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 2. 删除
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        zoneService.removeById(id);
        return Result.success();
    }

    // 3. 修改状态
    @PostMapping("/status/{id}/{status}")
    public Result<?> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        Zone zone = new Zone();
        zone.setId(id);
        zone.setStatus(status);
        zoneService.updateById(zone);
        return Result.success();
    }

    // 4. 分页查询 (对应图2：支持按名称、仓库、状态筛选)
    @GetMapping("/page")
    public Result<IPage<Zone>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,        // 库区名称
            @RequestParam(required = false) Long warehouseId,   // 所属仓库ID
            @RequestParam(required = false) Integer status      // 状态
    ) {
        return Result.success(zoneService.pageQuery(pageNum, pageSize, name, warehouseId, status));
    }

    // 5. 根据仓库ID查询所有库区 (用于级联下拉框：选了仓库A，只能选仓库A下的库区)
    @GetMapping("/list/{warehouseId}")
    public Result<List<Zone>> listByWarehouse(@PathVariable Long warehouseId) {
        LambdaQueryWrapper<Zone> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Zone::getWarehouseId, warehouseId);
        wrapper.eq(Zone::getStatus, 1); // 只查可用的
        return Result.success(zoneService.list(wrapper));
    }

    // 6. 详情
    @GetMapping("/{id}")
    public Result<Zone> getById(@PathVariable Long id) {
        return Result.success(zoneService.getById(id));
    }
}