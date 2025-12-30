package com.lxk.wms.wms_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lxk.wms.wms_backend.dto.InventorySummaryDTO;
import com.lxk.wms.wms_backend.entity.Inventory;

public interface InventoryService extends IService<Inventory> {

    // 1. 分页查询 (明细)
    IPage<Inventory> pageQuery(Integer pageNum, Integer pageSize, Long warehouseId, String productName);

    // 2. ✅ 修改这里：增加 batchNo 参数
    void addInventory(Long productId, Long warehouseId, Long zoneId, Long rackId, Long binId, Integer quantity, String batchNo);

    // 3. 扣减库存
    boolean deductInventory(Long productId, Long binId, Integer quantity);

    // 4. 查询某位置某产品库存
    Integer getStockQuantity(Long productId, Long binId);

    // 5. ✅ 新增：库存汇总查询接口
    IPage<InventorySummaryDTO> pageSummary(Integer pageNum, Integer pageSize, String keyword);
}