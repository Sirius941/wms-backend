package com.lxk.wms.wms_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lxk.wms.wms_backend.common.Result;
import com.lxk.wms.wms_backend.entity.Client;
import com.lxk.wms.wms_backend.service.ClientService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "客户/供应商管理接口", description = "客户/供应商的新增/编辑、删除、分页查询、可用列表查询")
@RestController
@RequestMapping("/client")
public class ClientController {
    @Autowired private ClientService clientService;

    @PostMapping("/save")
    public Result<?> save(@RequestBody Client client) {
        clientService.saveOrUpdate(client);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        clientService.removeById(id);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<IPage<Client>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer type // 1供应商 2客户
    ) {
        return Result.success(clientService.pageQuery(pageNum, pageSize, name, type));
    }

    // 关键接口：获取所有可用列表 (type: 1或2)
    @GetMapping("/list/{type}")
    public Result<List<Client>> listByType(@PathVariable Integer type) {
        LambdaQueryWrapper<Client> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Client::getStatus, 1);
        wrapper.eq(Client::getClientType, type);
        return Result.success(clientService.list(wrapper));
    }
}