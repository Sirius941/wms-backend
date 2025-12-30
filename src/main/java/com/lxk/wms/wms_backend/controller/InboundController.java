package com.lxk.wms.wms_backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lxk.wms.wms_backend.common.Result;
import com.lxk.wms.wms_backend.dto.InboundDTO;
import com.lxk.wms.wms_backend.entity.Inbound;
import com.lxk.wms.wms_backend.service.InboundService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "入库单管理接口", description = "入库单的创建、分页查询、详情、删除/取消")
@RestController
@RequestMapping("/inbound")
public class InboundController {

    @Autowired
    private InboundService inboundService;

    // 1. 创建入库单 (对应图5、6)
    // 前端传 JSON: { warehouseId: 1, supplierName: "Apple", products: [ {productId:1, planQuantity:10} ] }
    @PostMapping("/save")
    public Result<?> save(@RequestBody InboundDTO inboundDTO) {
        try {
            inboundService.saveInbound(inboundDTO);
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    // 2. 分页查询 (对应图2、3、4)
    @GetMapping("/page")
    public Result<IPage<Inbound>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String inboundNo,
            @RequestParam(required = false) Integer status
    ) {
        return Result.success(inboundService.pageQuery(pageNum, pageSize, inboundNo, status));
    }

    // 3. 获取详情 (点击单号查看明细)
    @GetMapping("/{id}")
    public Result<Inbound> getById(@PathVariable Long id) {
        return Result.success(inboundService.getDetailById(id));
    }

    // 4. 删除/取消
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        // 这里可以加逻辑：只有状态为0(待收货)的才能删除
        Inbound inbound = inboundService.getById(id);
        if(inbound.getStatus() != 0) {
            return Result.error("只有待收货状态的订单可以删除");
        }
        inboundService.removeById(id);
        return Result.success();
    }
}