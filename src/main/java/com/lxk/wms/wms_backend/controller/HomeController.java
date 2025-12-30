package com.lxk.wms.wms_backend.controller;

import com.lxk.wms.wms_backend.common.Result;
import com.lxk.wms.wms_backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/home")
public class HomeController {

    @Autowired private WarehouseService warehouseService;
    @Autowired private ProductService productService;
    @Autowired private InboundService inboundService;
    @Autowired private OutboundService outboundService;

    // 统计数据接口
    @GetMapping("/stats")
    public Result<Map<String, Long>> stats() {
        Map<String, Long> map = new HashMap<>();

        // 1. 仓库总数
        map.put("warehouseCount", warehouseService.count());

        // 2. 产品SKU总数
        map.put("productCount", productService.count());

        // 3. 入库单总数
        map.put("inboundCount", inboundService.count());

        // 4. 出库单总数
        map.put("outboundCount", outboundService.count());

        // 你还可以加更多的统计，比如 "待处理入库单": inboundService.count(wrapper.eq("status", 0))

        return Result.success(map);
    }
}