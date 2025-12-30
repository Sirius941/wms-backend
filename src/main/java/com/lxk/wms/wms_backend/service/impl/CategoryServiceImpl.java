package com.lxk.wms.wms_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lxk.wms.wms_backend.entity.Category;
import com.lxk.wms.wms_backend.mapper.CategoryMapper;
import com.lxk.wms.wms_backend.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public boolean saveCategory(Category category) {
        // 1. 校验分类名称唯一性
        checkUnique(category.getId(), Category::getCategoryName, category.getCategoryName(), "分类名称已存在");

        // 2. 校验分类编码唯一性
        checkUnique(category.getId(), Category::getCategoryCode, category.getCategoryCode(), "分类编码已存在");

        return this.saveOrUpdate(category);
    }

    @Override
    public IPage<Category> pageQuery(Integer pageNum, Integer pageSize, String name, Integer status) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Category::getCategoryName, name);
        }
        if (status != null) {
            wrapper.eq(Category::getStatus, status);
        }
        wrapper.orderByDesc(Category::getCreateTime);

        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    private void checkUnique(Long id, SFunction<Category, ?> column, Object value, String errorMsg) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(column, value);
        if (id != null) wrapper.ne(Category::getId, id);
        if (this.count(wrapper) > 0) throw new RuntimeException(errorMsg);
    }
}