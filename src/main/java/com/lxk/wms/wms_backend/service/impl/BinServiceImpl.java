package com.lxk.wms.wms_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lxk.wms.wms_backend.entity.*;
import com.lxk.wms.wms_backend.mapper.*;
import com.lxk.wms.wms_backend.service.BinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BinServiceImpl extends ServiceImpl<BinMapper, Bin> implements BinService {

    @Autowired
    private RackMapper rackMapper; // 用于反查父级ID
    @Autowired
    private ZoneMapper zoneMapper; // 用于显示名字
    @Autowired
    private WarehouseMapper warehouseMapper; // 用于显示名字

    @Override
    public boolean saveBin(Bin bin) {
        // 1. 自动填充 ZoneId 和 WarehouseId
        if (bin.getRackId() != null) {
            Rack rack = rackMapper.selectById(bin.getRackId());
            if (rack == null) throw new RuntimeException("所选货架不存在");

            bin.setZoneId(rack.getZoneId());
            bin.setWarehouseId(rack.getWarehouseId());
        } else {
            throw new RuntimeException("必须选择所属货架");
        }

        // 2. 唯一性校验
        checkUnique(bin.getId(), Bin::getBinCode, bin.getBinCode(), "库位编码已存在");

        // 3. 默认状态初始化
        if (bin.getUsageStatus() == null) {
            bin.setUsageStatus(0); // 默认为空闲
        }

        return this.saveOrUpdate(bin);
    }

    @Override
    public IPage<Bin> pageQuery(Integer pageNum, Integer pageSize, String binCode, Long warehouseId, Long zoneId, Long rackId, Integer status, Integer usageStatus) {
        // 1. 构造查询条件
        LambdaQueryWrapper<Bin> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(binCode)) wrapper.like(Bin::getBinCode, binCode);
        if (warehouseId != null) wrapper.eq(Bin::getWarehouseId, warehouseId);
        if (zoneId != null) wrapper.eq(Bin::getZoneId, zoneId);
        if (rackId != null) wrapper.eq(Bin::getRackId, rackId);
        if (status != null) wrapper.eq(Bin::getStatus, status);
        if (usageStatus != null) wrapper.eq(Bin::getUsageStatus, usageStatus);

        wrapper.orderByDesc(Bin::getCreateTime);

        Page<Bin> page = this.page(new Page<>(pageNum, pageSize), wrapper);

        // 2. 填充 仓库名、库区名、货架名 (三级联动显示)
        List<Bin> records = page.getRecords();
        if (!records.isEmpty()) {
            Set<Long> whIds = records.stream().map(Bin::getWarehouseId).collect(Collectors.toSet());
            Set<Long> znIds = records.stream().map(Bin::getZoneId).collect(Collectors.toSet());
            Set<Long> rkIds = records.stream().map(Bin::getRackId).collect(Collectors.toSet());

            Map<Long, String> whMap = warehouseMapper.selectBatchIds(whIds).stream().collect(Collectors.toMap(Warehouse::getId, Warehouse::getWarehouseName));
            Map<Long, String> znMap = zoneMapper.selectBatchIds(znIds).stream().collect(Collectors.toMap(Zone::getId, Zone::getZoneName));
            Map<Long, String> rkMap = rackMapper.selectBatchIds(rkIds).stream().collect(Collectors.toMap(Rack::getId, Rack::getRackName));

            for (Bin b : records) {
                b.setWarehouseName(whMap.get(b.getWarehouseId()));
                b.setZoneName(znMap.get(b.getZoneId()));
                b.setRackName(rkMap.get(b.getRackId()));
            }
        }
        return page;
    }

    private void checkUnique(Long id, SFunction<Bin, ?> column, Object value, String errorMsg) {
        LambdaQueryWrapper<Bin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(column, value);
        if (id != null) wrapper.ne(Bin::getId, id);
        if (this.count(wrapper) > 0) throw new RuntimeException(errorMsg);
    }
}