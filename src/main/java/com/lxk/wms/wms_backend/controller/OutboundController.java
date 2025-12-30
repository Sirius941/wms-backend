package com.lxk.wms.wms_backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lxk.wms.wms_backend.common.Result;
import com.lxk.wms.wms_backend.dto.OutboundDTO;
import com.lxk.wms.wms_backend.entity.Outbound;
import com.lxk.wms.wms_backend.service.OutboundService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "出库单管理接口", description = "出库单的创建、分页查询、详情、删除")
@RestController
@RequestMapping("/outbound")
public class OutboundController {

    @Autowired
    private OutboundService outboundService;

    // 1. 创建出库单 (对应图3, 4, 5, 6)
    @PostMapping("/save")
    public Result<?> save(@RequestBody OutboundDTO dto) {
        try {
            outboundService.saveOutbound(dto);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 2. 分页查询 (对应图2)
    @GetMapping("/page")
    public Result<IPage<Outbound>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String outboundNo,
            @RequestParam(required = false) Integer status
    ) {
        return Result.success(outboundService.pageQuery(pageNum, pageSize, outboundNo, status));
    }

    // 3. 详情
    @GetMapping("/{id}")
    public Result<Outbound> getById(@PathVariable Long id) {
        return Result.success(outboundService.getDetailById(id));
    }

    // 4. 删除
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        Outbound outbound = outboundService.getById(id);
        if (outbound.getStatus() != 0) {
            return Result.error("只有待拣货状态的订单可以删除");
        }
        outboundService.removeById(id);
        return Result.success();
    }
}