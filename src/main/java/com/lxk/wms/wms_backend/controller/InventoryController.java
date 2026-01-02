package com.lxk.wms.wms_backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lxk.wms.wms_backend.common.Result;
import com.lxk.wms.wms_backend.dto.InventorySummaryDTO;
import com.lxk.wms.wms_backend.entity.Inventory;
import com.lxk.wms.wms_backend.service.InventoryService;
import com.lxk.wms.wms_backend.service.impl.InventoryServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "库存管理接口", description = "库存的分页查询、库存汇总")
@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    // 分页查询库存
    // 前端示例图展示：列表应包含 产品图片、SKU、名称、所在仓库、所在库位、数量
    @GetMapping("/page")
    public Result<IPage<Inventory>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String productName
    ) {
        return Result.success(inventoryService.pageQuery(pageNum, pageSize, warehouseId, productName));
    }

    // 只有在非常特殊的情况下（比如库存盘点发现少了），才允许管理员手动改库存
    // 期末项目如果有"盘点"功能，可以调用这个
    /*
    @PostMapping("/update")
    public Result<?> update(@RequestBody Inventory inventory) {
        inventoryService.updateById(inventory);
        return Result.success();
    }
    */
    // 2. ✅ 新增接口 -> 对应 "StorageModule" (库存汇总)
    @GetMapping("/summary-page")
    public Result<IPage<InventorySummaryDTO>> summaryPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword
    ) {
        // 需要在 Service 接口里定义这个方法，并在 Impl 里实现
        // 这里的强制类型转换取决于你怎么定义的 Interface
        return Result.success(((InventoryServiceImpl)inventoryService).pageSummary(pageNum, pageSize, keyword));
    }
}