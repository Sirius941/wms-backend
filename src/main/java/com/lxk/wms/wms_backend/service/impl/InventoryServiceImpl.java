package com.lxk.wms.wms_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lxk.wms.wms_backend.dto.InventorySummaryDTO;
import com.lxk.wms.wms_backend.entity.*;
import com.lxk.wms.wms_backend.mapper.*;
import com.lxk.wms.wms_backend.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory> implements InventoryService {

    @Autowired private ProductMapper productMapper;
    @Autowired private WarehouseMapper warehouseMapper;
    @Autowired private ZoneMapper zoneMapper;
    @Autowired private RackMapper rackMapper;
    @Autowired private BinMapper binMapper;
    @Autowired private UnitMapper unitMapper; // 用来显示单位
    @Autowired private InventoryMapper inventoryMapper;

    // ✅ 新增：实现库存汇总查询
    public IPage<InventorySummaryDTO> pageSummary(Integer pageNum, Integer pageSize, String keyword) {
        return inventoryMapper.selectInventorySummary(new Page<>(pageNum, pageSize), keyword);
    }
    @Override
    public IPage<Inventory> pageQuery(Integer pageNum, Integer pageSize, Long warehouseId, String productName) {
        // 1. 构造查询条件
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        if (warehouseId != null) {
            wrapper.eq(Inventory::getWarehouseId, warehouseId);
        }

        // 如果用户输入了产品名称，我们需要先查出对应的 ProductID，再查 Inventory (子查询逻辑)
        // 这里简化处理：假设前端传的是 keyword，我们先不在这里做复杂关联搜索，
        // 实际开发中通常使用 SQL JOIN，但为了保持代码风格一致，这里先只支持按仓库筛选
        // *如果必须支持按产品名筛选，请在 Mapper.xml 中写 SQL*

        wrapper.orderByDesc(Inventory::getQuantity); // 库存多的排前面

        Page<Inventory> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<Inventory> records = page.getRecords();

        // 2. 填充关联数据 (Product, Warehouse, Location)
        if (!records.isEmpty()) {
            Set<Long> pIds = records.stream().map(Inventory::getProductId).collect(Collectors.toSet());
            Set<Long> wIds = records.stream().map(Inventory::getWarehouseId).collect(Collectors.toSet());
            Set<Long> bIds = records.stream().map(Inventory::getBinId).collect(Collectors.toSet());

            // 查产品
            Map<Long, Product> pMap = productMapper.selectBatchIds(pIds).stream()
                    .collect(Collectors.toMap(Product::getId, p -> p));
            // 查单位
            Set<Long> uIds = pMap.values().stream().map(Product::getUnitId).collect(Collectors.toSet());
            Map<Long, String> uMap = uIds.isEmpty() ? Collections.emptyMap() :
                    unitMapper.selectBatchIds(uIds).stream().collect(Collectors.toMap(Unit::getId, Unit::getUnitName));

            // 查仓库
            Map<Long, String> wMap = warehouseMapper.selectBatchIds(wIds).stream()
                    .collect(Collectors.toMap(Warehouse::getId, Warehouse::getWarehouseName));

            // 查库位 (为了获取 BinCode)
            Map<Long, Bin> bMap = binMapper.selectBatchIds(bIds).stream()
                    .collect(Collectors.toMap(Bin::getId, b -> b));

            // 填充数据
            for (Inventory inv : records) {
                // 填充产品信息
                Product p = pMap.get(inv.getProductId());
                if (p != null) {
                    inv.setProductName(p.getProductName());
                    inv.setProductSku(p.getSku());
                    inv.setProductImg(p.getImgUrl());
                    inv.setUnitName(uMap.get(p.getUnitId()));
                }

                // 填充仓库信息
                inv.setWarehouseName(wMap.get(inv.getWarehouseId()));

                // 填充位置信息
                Bin b = bMap.get(inv.getBinId());
                if (b != null) {
                    inv.setBinCode(b.getBinCode());
                }
            }
        }
        return page;
    }

    @Override
    public void addInventory(Long productId, Long warehouseId, Long zoneId, Long rackId, Long binId, Integer quantity, String batchNo) {
        // 1. 查询该库位是否已经有该产品的库存记录
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inventory::getProductId, productId);
        wrapper.eq(Inventory::getBinId, binId); // 一个库位+一个产品 = 唯一记录
        wrapper.eq(Inventory::getBatchNo, batchNo == null ? "" : batchNo);

        Inventory inventory = this.getOne(wrapper);

        if (inventory == null) {
            // 2. 如果没有，新建记录
            inventory = new Inventory();
            inventory.setProductId(productId);
            inventory.setWarehouseId(warehouseId);
            inventory.setZoneId(zoneId);
            inventory.setRackId(rackId);
            inventory.setBinId(binId);
            inventory.setQuantity(quantity);
            inventory.setBatchNo(batchNo == null ? "" : batchNo); // 设置批号
            inventory.setLockedQuantity(0);
            this.save(inventory);
        } else {
            // 3. 如果有，直接累加数量
            inventory.setQuantity(inventory.getQuantity() + quantity);
            this.updateById(inventory);
        }
    }

    // ... 原有代码 ...

    @Override
    public boolean deductInventory(Long productId, Long binId, Integer quantity) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inventory::getProductId, productId);
        wrapper.eq(Inventory::getBinId, binId);
        Inventory inventory = this.getOne(wrapper);

        if (inventory == null || inventory.getQuantity() < quantity) {
            return false; // 库存不足
        }

        inventory.setQuantity(inventory.getQuantity() - quantity);
        this.updateById(inventory);
        return true;
    }

    @Override
    public Integer getStockQuantity(Long productId, Long binId) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inventory::getProductId, productId);
        wrapper.eq(Inventory::getBinId, binId);
        Inventory inventory = this.getOne(wrapper);
        return inventory == null ? 0 : inventory.getQuantity();
    }
}