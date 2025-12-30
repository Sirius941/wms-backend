package com.lxk.wms.wms_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lxk.wms.wms_backend.entity.Warehouse;
import com.lxk.wms.wms_backend.entity.Zone;
import com.lxk.wms.wms_backend.mapper.WarehouseMapper;
import com.lxk.wms.wms_backend.mapper.ZoneMapper;
import com.lxk.wms.wms_backend.service.ZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ZoneServiceImpl extends ServiceImpl<ZoneMapper, Zone> implements ZoneService {

    @Autowired
    private WarehouseMapper warehouseMapper;

    @Override
    public boolean saveZone(Zone zone) {
        // 1. 唯一性校验：库区名称
        checkUnique(zone.getId(), Zone::getZoneName, zone.getZoneName(), "库区名称已存在");

        // 2. 唯一性校验：库区编码
        checkUnique(zone.getId(), Zone::getZoneCode, zone.getZoneCode(), "库区编码已存在");

        return this.saveOrUpdate(zone);
    }

    @Override
    public IPage<Zone> pageQuery(Integer pageNum, Integer pageSize, String name, Long warehouseId, Integer status) {
        // 1. 构建查询条件
        LambdaQueryWrapper<Zone> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Zone::getZoneName, name);
        }
        if (warehouseId != null) {
            wrapper.eq(Zone::getWarehouseId, warehouseId);
        }
        if (status != null) {
            wrapper.eq(Zone::getStatus, status);
        }
        wrapper.orderByDesc(Zone::getCreateTime);

        // 2. 执行分页查询
        Page<Zone> page = this.page(new Page<>(pageNum, pageSize), wrapper);

        // 3. 填充"仓库名称" (关键步骤：否则前端只能看到 warehouseId)
        if (!page.getRecords().isEmpty()) {
            // 取出所有涉及到的仓库ID
            List<Long> warehouseIds = page.getRecords().stream()
                    .map(Zone::getWarehouseId)
                    .distinct()
                    .collect(Collectors.toList());

            // 批量查询仓库信息
            if (!warehouseIds.isEmpty()) {
                List<Warehouse> warehouses = warehouseMapper.selectBatchIds(warehouseIds);
                // 转成 Map<ID, Name> 方便查找
                Map<Long, String> whMap = warehouses.stream()
                        .collect(Collectors.toMap(Warehouse::getId, Warehouse::getWarehouseName));

                // 赋值
                for (Zone record : page.getRecords()) {
                    record.setWarehouseName(whMap.get(record.getWarehouseId()));
                }
            }
        }
        return page;
    }

    private void checkUnique(Long id, SFunction<Zone, ?> column, Object value, String errorMsg) {
        LambdaQueryWrapper<Zone> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(column, value);
        if (id != null) wrapper.ne(Zone::getId, id);
        if (this.count(wrapper) > 0) throw new RuntimeException(errorMsg);
    }
}