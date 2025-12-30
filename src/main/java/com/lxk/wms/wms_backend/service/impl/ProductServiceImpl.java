package com.lxk.wms.wms_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lxk.wms.wms_backend.entity.*;
import com.lxk.wms.wms_backend.mapper.*;
import com.lxk.wms.wms_backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired private CategoryMapper categoryMapper;
    @Autowired private UnitMapper unitMapper;
    @Autowired private StorageTypeMapper storageTypeMapper;

    @Override
    public boolean saveProduct(Product product) {
        // 1. 校验SKU唯一性 (产品身份证，绝对不能重)
        checkUnique(product.getId(), Product::getSku, product.getSku(), "SKU编码已存在");

        // 2. 保存
        return this.saveOrUpdate(product);
    }

    @Override
    public IPage<Product> pageQuery(Integer pageNum, Integer pageSize, String keyword, Long categoryId, Integer status) {
        // 1. 基础查询
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            // 支持搜 名称 或 SKU
            wrapper.and(w -> w.like(Product::getProductName, keyword)
                    .or()
                    .like(Product::getSku, keyword));
        }
        if (categoryId != null) wrapper.eq(Product::getCategoryId, categoryId);
        if (status != null) wrapper.eq(Product::getStatus, status);

        wrapper.orderByDesc(Product::getCreateTime);

        Page<Product> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<Product> records = page.getRecords();

        // 2. 填充关联名称 (Category, Unit, StorageType)
        if (!records.isEmpty()) {
            // 提取ID集合
            Set<Long> catIds = records.stream().map(Product::getCategoryId).collect(Collectors.toSet());
            Set<Long> unitIds = records.stream().map(Product::getUnitId).collect(Collectors.toSet());
            Set<Long> typeIds = records.stream().map(Product::getStorageTypeId).collect(Collectors.toSet());

            // 批量查询并转Map (防空指针处理)
            Map<Long, String> catMap = catIds.isEmpty() ? Collections.emptyMap() :
                    categoryMapper.selectBatchIds(catIds).stream().collect(Collectors.toMap(Category::getId, Category::getCategoryName));

            Map<Long, String> unitMap = unitIds.isEmpty() ? Collections.emptyMap() :
                    unitMapper.selectBatchIds(unitIds).stream().collect(Collectors.toMap(Unit::getId, Unit::getUnitName));

            Map<Long, String> typeMap = typeIds.isEmpty() ? Collections.emptyMap() :
                    storageTypeMapper.selectBatchIds(typeIds).stream().collect(Collectors.toMap(StorageType::getId, StorageType::getTypeName));

            // 赋值
            for (Product p : records) {
                p.setCategoryName(catMap.get(p.getCategoryId()));
                p.setUnitName(unitMap.get(p.getUnitId()));
                p.setStorageTypeName(typeMap.get(p.getStorageTypeId()));
            }
        }
        return page;
    }

    private void checkUnique(Long id, SFunction<Product, ?> column, Object value, String errorMsg) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(column, value);
        if (id != null) wrapper.ne(Product::getId, id);
        if (this.count(wrapper) > 0) throw new RuntimeException(errorMsg);
    }
}