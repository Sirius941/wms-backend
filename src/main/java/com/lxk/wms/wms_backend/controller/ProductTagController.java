package com.lxk.wms.wms_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lxk.wms.wms_backend.common.Result;
import com.lxk.wms.wms_backend.entity.ProductTag;
import com.lxk.wms.wms_backend.service.ProductTagService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "产品标签管理接口", description = "产品标签的增删改查接口")
@RestController
@RequestMapping("/product-tag")
public class ProductTagController {

    @Autowired
    private ProductTagService productTagService;

    // 1. 新增/编辑
    @PostMapping("/save")
    public Result<?> save(@RequestBody ProductTag productTag) {
        try {
            productTagService.saveProductTag(productTag);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 2. 删除
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        productTagService.removeById(id);
        return Result.success();
    }

    // 3. 修改状态
    @PostMapping("/status/{id}/{status}")
    public Result<?> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        ProductTag productTag = new ProductTag();
        productTag.setId(id);
        productTag.setStatus(status);
        productTagService.updateById(productTag);
        return Result.success();
    }

    // 4. 分页查询
    @GetMapping("/page")
    public Result<IPage<ProductTag>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status
    ) {
        return Result.success(productTagService.pageQuery(pageNum, pageSize, name, status));
    }

    // 5. 获取所有可用标签 (用于在产品编辑页面进行多选)
    @GetMapping("/list")
    public Result<List<ProductTag>> list() {
        LambdaQueryWrapper<ProductTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductTag::getStatus, 1);
        return Result.success(productTagService.list(wrapper));
    }

    // 6. 详情
    @GetMapping("/{id}")
    public Result<ProductTag> getById(@PathVariable Long id) {
        return Result.success(productTagService.getById(id));
    }
}