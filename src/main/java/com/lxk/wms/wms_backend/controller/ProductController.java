package com.lxk.wms.wms_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lxk.wms.wms_backend.common.Result;
import com.lxk.wms.wms_backend.entity.Product;
import com.lxk.wms.wms_backend.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "产品管理")
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    // 1. 新增/编辑
    @PostMapping("/save")
    public Result<?> save(@RequestBody Product product) {
        try {
            productService.saveProduct(product);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 2. 删除
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        // 实际上线时，如果有库存则不能删除。期末项目可暂时忽略。
        productService.removeById(id);
        return Result.success();
    }

    // 3. 上架/下架
    @PostMapping("/status/{id}/{status}")
    public Result<?> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(status);
        productService.updateById(product);
        return Result.success();
    }

    // 4. 分页查询
    @GetMapping("/page")
    public Result<IPage<Product>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,    // 搜索 SKU 或 名称
            @RequestParam(required = false) Long categoryId,   // 按分类筛选
            @RequestParam(required = false) Integer status
    ) {
        return Result.success(productService.pageQuery(pageNum, pageSize, keyword, categoryId, status));
    }

    // 5. 根据ID获取详情
    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    // 6. 获取所有上架产品 (用于入库单选择产品)
    @GetMapping("/list")
    public Result<List<Product>> list() {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1); // 只能选上架的
        return Result.success(productService.list(wrapper));
    }
}