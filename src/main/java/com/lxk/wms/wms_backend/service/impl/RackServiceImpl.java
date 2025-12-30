package com.lxk.wms.wms_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lxk.wms.wms_backend.entity.Rack;
import com.lxk.wms.wms_backend.entity.Warehouse;
import com.lxk.wms.wms_backend.entity.Zone;
import com.lxk.wms.wms_backend.mapper.RackMapper;
import com.lxk.wms.wms_backend.mapper.WarehouseMapper;
import com.lxk.wms.wms_backend.mapper.ZoneMapper;
import com.lxk.wms.wms_backend.service.RackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RackServiceImpl extends ServiceImpl<RackMapper, Rack> implements RackService {

    @Autowired
    private ZoneMapper zoneMapper;
    @Autowired
    private WarehouseMapper warehouseMapper;

    @Override
    public boolean saveRack(Rack rack) {
        // 1. 自动补全 warehouseId (防止前端没传)
        if (rack.getZoneId() != null) {
            Zone zone = zoneMapper.selectById(rack.getZoneId());
            if (zone != null) {
                rack.setWarehouseId(zone.getWarehouseId());
            } else {
                throw new RuntimeException("所选库区不存在");
            }
        }

        // 2. 唯一性校验
        checkUnique(rack.getId(), Rack::getRackName, rack.getRackName(), "货架名称已存在");
        checkUnique(rack.getId(), Rack::getRackCode, rack.getRackCode(), "货架编码已存在");

        return this.saveOrUpdate(rack);
    }

    @Override
    public IPage<Rack> pageQuery(Integer pageNum, Integer pageSize, String name, Long warehouseId, Long zoneId, Integer status) {
        // 1. 构造查询
        LambdaQueryWrapper<Rack> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) wrapper.like(Rack::getRackName, name);
        if (warehouseId != null) wrapper.eq(Rack::getWarehouseId, warehouseId);
        if (zoneId != null) wrapper.eq(Rack::getZoneId, zoneId);
        if (status != null) wrapper.eq(Rack::getStatus, status);

        wrapper.orderByDesc(Rack::getCreateTime);

        Page<Rack> page = this.page(new Page<>(pageNum, pageSize), wrapper);

        // 2. 填充 仓库名称 和 库区名称 (为了前端展示好看)
        List<Rack> records = page.getRecords();
        if (!records.isEmpty()) {
            // 提取所有ID
            Set<Long> whIds = records.stream().map(Rack::getWarehouseId).collect(Collectors.toSet());
            Set<Long> znIds = records.stream().map(Rack::getZoneId).collect(Collectors.toSet());

            // 批量查询并转Map
            Map<Long, String> whMap = warehouseMapper.selectBatchIds(whIds).stream()
                    .collect(Collectors.toMap(Warehouse::getId, Warehouse::getWarehouseName));
            Map<Long, String> znMap = zoneMapper.selectBatchIds(znIds).stream()
                    .collect(Collectors.toMap(Zone::getId, Zone::getZoneName));

            // 赋值
            for (Rack r : records) {
                r.setWarehouseName(whMap.get(r.getWarehouseId()));
                r.setZoneName(znMap.get(r.getZoneId()));
            }
        }
        return page;
    }

    private void checkUnique(Long id, SFunction<Rack, ?> column, Object value, String errorMsg) {
        LambdaQueryWrapper<Rack> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(column, value);
        if (id != null) wrapper.ne(Rack::getId, id);
        if (this.count(wrapper) > 0) throw new RuntimeException(errorMsg);
    }
}