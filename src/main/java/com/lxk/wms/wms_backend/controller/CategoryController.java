package com.lxk.wms.wms_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lxk.wms.wms_backend.common.Result;
import com.lxk.wms.wms_backend.entity.Category;
import com.lxk.wms.wms_backend.service.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="分类管理接口", description="分类管理相关的增删改查接口")
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // 1. 新增/编辑 (对应前端示例图2)
    @PostMapping("/save")
    public Result<?> save(@RequestBody Category category) {
        try {
            categoryService.saveCategory(category);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 2. 删除
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        // 严格来讲，删除前应该检查该分类下是否有产品，如果有则不允许删除
        // 这里为了期末项目进度，暂不强制校验
        categoryService.removeById(id);
        return Result.success();
    }

    // 3. 修改状态
    @PostMapping("/status/{id}/{status}")
    public Result<?> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        Category category = new Category();
        category.setId(id);
        category.setStatus(status);
        categoryService.updateById(category);
        return Result.success();
    }

    // 4. 分页查询 (对应前端示例图1)
    @GetMapping("/page")
    public Result<IPage<Category>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status
    ) {
        return Result.success(categoryService.pageQuery(pageNum, pageSize, name, status));
    }

    // 5. 获取所有可用分类 (用于后续产品页面的下拉框)
    @GetMapping("/list")
    public Result<List<Category>> list() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus, 1);
        return Result.success(categoryService.list(wrapper));
    }

    // 6. 详情
    @GetMapping("/{id}")
    public Result<Category> getById(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }
}