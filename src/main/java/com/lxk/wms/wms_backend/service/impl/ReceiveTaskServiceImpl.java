package com.lxk.wms.wms_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lxk.wms.wms_backend.entity.*;
import com.lxk.wms.wms_backend.mapper.*;
import com.lxk.wms.wms_backend.service.InventoryService;
import com.lxk.wms.wms_backend.service.ReceiveTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReceiveTaskServiceImpl extends ServiceImpl<ReceiveTaskMapper, ReceiveTask> implements ReceiveTaskService {

    @Autowired private InventoryService inventoryService;
    @Autowired private InboundDetailMapper inboundDetailMapper;
    @Autowired private InboundMapper inboundMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private WarehouseMapper warehouseMapper;
    @Autowired private BinMapper binMapper;
    @Autowired private RackMapper rackMapper; // 用于反查 ID

    @Override
    public void createTasks(ReceiveTask task) {
        // 1. 自动补全层级ID (前端可能只传了 binId)
        if (task.getBinId() != null) {
            Bin bin = binMapper.selectById(task.getBinId());
            if (bin != null) {
                task.setRackId(bin.getRackId());
                task.setZoneId(bin.getZoneId());
                task.setWarehouseId(bin.getWarehouseId());
            }
        }

        task.setStatus(0); // 默认待执行
        this.save(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(Long taskId) {
        ReceiveTask task = this.getById(taskId);
        String batchNo = "BATCH-" + task.getInboundId();
        if (task == null || task.getStatus() != 0) {
            throw new RuntimeException("任务不存在或已处理");
        }

        // 1. 真正增加库存 (调用 InventoryService)
        inventoryService.addInventory(
                task.getProductId(),
                task.getWarehouseId(),
                task.getZoneId(),
                task.getRackId(),
                task.getBinId(),
                task.getQuantity(),
                batchNo
        );

        // 2. 更新入库单明细的"实际数量" (RealQuantity)
        InboundDetail detail = inboundDetailMapper.selectById(task.getInboundDetailId());
        if (detail != null) {
            detail.setRealQuantity(detail.getRealQuantity() + task.getQuantity());
            inboundDetailMapper.updateById(detail);
        }

        // 3. 更新任务状态为已完成
        task.setStatus(1);
        this.updateById(task);

        // 4. (可选) 检查入库单是否全部完成，如果是，更新主单状态为1
        // 这里为了简化代码暂不自动更新主单状态，保留手动或简化逻辑
    }

    @Override
    public IPage<ReceiveTask> pageQuery(Integer pageNum, Integer pageSize, Long inboundId, Integer status) {
        LambdaQueryWrapper<ReceiveTask> wrapper = new LambdaQueryWrapper<>();
        if (inboundId != null) wrapper.eq(ReceiveTask::getInboundId, inboundId);
        if (status != null) wrapper.eq(ReceiveTask::getStatus, status);

        wrapper.orderByDesc(ReceiveTask::getCreateTime);

        Page<ReceiveTask> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<ReceiveTask> records = page.getRecords();

        // 填充关联名称 (产品名、库位名、入库单号)
        if (!records.isEmpty()) {
            Set<Long> pIds = records.stream().map(ReceiveTask::getProductId).collect(Collectors.toSet());
            Set<Long> bIds = records.stream().map(ReceiveTask::getBinId).collect(Collectors.toSet());
            Set<Long> inIds = records.stream().map(ReceiveTask::getInboundId).collect(Collectors.toSet());

            Map<Long, Product> pMap = productMapper.selectBatchIds(pIds).stream().collect(Collectors.toMap(Product::getId, p->p));
            Map<Long, Bin> bMap = binMapper.selectBatchIds(bIds).stream().collect(Collectors.toMap(Bin::getId, b->b));
            Map<Long, Inbound> inMap = inboundMapper.selectBatchIds(inIds).stream().collect(Collectors.toMap(Inbound::getId, in->in));

            for (ReceiveTask t : records) {
                if (pMap.containsKey(t.getProductId())) {
                    t.setProductName(pMap.get(t.getProductId()).getProductName());
                    t.setProductSku(pMap.get(t.getProductId()).getSku());
                }
                if (bMap.containsKey(t.getBinId())) {
                    t.setBinCode(bMap.get(t.getBinId()).getBinCode());
                }
                if (inMap.containsKey(t.getInboundId())) {
                    t.setInboundNo(inMap.get(t.getInboundId()).getInboundNo());
                }
            }
        }
        return page;
    }
}