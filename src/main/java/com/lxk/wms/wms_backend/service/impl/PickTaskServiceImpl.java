package com.lxk.wms.wms_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lxk.wms.wms_backend.entity.*;
import com.lxk.wms.wms_backend.mapper.*;
import com.lxk.wms.wms_backend.service.InventoryService;
import com.lxk.wms.wms_backend.service.PickTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PickTaskServiceImpl extends ServiceImpl<PickTaskMapper, PickTask> implements PickTaskService {

    @Autowired private InventoryService inventoryService;
    @Autowired private OutboundDetailMapper outboundDetailMapper;
    @Autowired private OutboundMapper outboundMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private WarehouseMapper warehouseMapper;
    @Autowired private BinMapper binMapper;

    @Override
    public void createTask(PickTask task) {
        // 1. 自动补全层级ID
        if (task.getBinId() != null) {
            Bin bin = binMapper.selectById(task.getBinId());
            if (bin != null) {
                task.setRackId(bin.getRackId());
                task.setZoneId(bin.getZoneId());
                task.setWarehouseId(bin.getWarehouseId());
            }
        }

        // 2. 核心校验：创建任务时，检查该库位有没有那么多货
        Integer currentStock = inventoryService.getStockQuantity(task.getProductId(), task.getBinId());
        if (currentStock < task.getQuantity()) {
            throw new RuntimeException("库存不足！该库位只有 " + currentStock + " 个，无法拣货 " + task.getQuantity() + " 个");
        }

        task.setStatus(0); // 待执行
        this.save(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(Long taskId) {
        PickTask task = this.getById(taskId);
        if (task == null || task.getStatus() != 0) {
            throw new RuntimeException("任务不存在或已处理");
        }

        // 1. 真正扣减库存
        boolean success = inventoryService.deductInventory(task.getProductId(), task.getBinId(), task.getQuantity());
        if (!success) {
            throw new RuntimeException("扣减库存失败，可能是期间库存被占用了");
        }

        // 2. 更新出库单明细的实际数量
        OutboundDetail detail = outboundDetailMapper.selectById(task.getOutboundDetailId());
        if (detail != null) {
            detail.setRealQuantity(detail.getRealQuantity() + task.getQuantity());
            outboundDetailMapper.updateById(detail);
        }

        // 3. 更新任务状态
        task.setStatus(1);
        this.updateById(task);
    }

    @Override
    public IPage<PickTask> pageQuery(Integer pageNum, Integer pageSize, Long outboundId, Integer status) {
        LambdaQueryWrapper<PickTask> wrapper = new LambdaQueryWrapper<>();
        if (outboundId != null) wrapper.eq(PickTask::getOutboundId, outboundId);
        if (status != null) wrapper.eq(PickTask::getStatus, status);

        wrapper.orderByDesc(PickTask::getCreateTime);

        Page<PickTask> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<PickTask> records = page.getRecords();

        // 填充关联名称
        if (!records.isEmpty()) {
            Set<Long> pIds = records.stream().map(PickTask::getProductId).collect(Collectors.toSet());
            Set<Long> bIds = records.stream().map(PickTask::getBinId).collect(Collectors.toSet());
            Set<Long> outIds = records.stream().map(PickTask::getOutboundId).collect(Collectors.toSet());

            Map<Long, Product> pMap = productMapper.selectBatchIds(pIds).stream().collect(Collectors.toMap(Product::getId, p->p));
            Map<Long, Bin> bMap = binMapper.selectBatchIds(bIds).stream().collect(Collectors.toMap(Bin::getId, b->b));
            Map<Long, Outbound> outMap = outboundMapper.selectBatchIds(outIds).stream().collect(Collectors.toMap(Outbound::getId, o->o));

            for (PickTask t : records) {
                if (pMap.containsKey(t.getProductId())) {
                    t.setProductName(pMap.get(t.getProductId()).getProductName());
                    t.setProductSku(pMap.get(t.getProductId()).getSku());
                }
                if (bMap.containsKey(t.getBinId())) {
                    t.setBinCode(bMap.get(t.getBinId()).getBinCode());
                }
                if (outMap.containsKey(t.getOutboundId())) {
                    t.setOutboundNo(outMap.get(t.getOutboundId()).getOutboundNo());
                }
            }
        }
        return page;
    }
}