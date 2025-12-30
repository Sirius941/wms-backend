package com.lxk.wms.wms_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lxk.wms.wms_backend.entity.ProductTag;
import com.lxk.wms.wms_backend.mapper.ProductTagMapper;
import com.lxk.wms.wms_backend.service.ProductTagService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ProductTagServiceImpl extends ServiceImpl<ProductTagMapper, ProductTag> implements ProductTagService {

    @Override
    public boolean saveProductTag(ProductTag productTag) {
        // 1. 校验名称唯一性
        checkUnique(productTag.getId(), ProductTag::getTagName, productTag.getTagName(), "标签名称已存在");

        // 2. 校验编码唯一性
        checkUnique(productTag.getId(), ProductTag::getTagCode, productTag.getTagCode(), "标签编码已存在");

        // 3. 设置默认颜色 (如果前端没传)
        if (!StringUtils.hasText(productTag.getColor())) {
            productTag.setColor("#409EFF"); // 默认蓝色
        }

        return this.saveOrUpdate(productTag);
    }

    @Override
    public IPage<ProductTag> pageQuery(Integer pageNum, Integer pageSize, String name, Integer status) {
        LambdaQueryWrapper<ProductTag> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(ProductTag::getTagName, name);
        }
        if (status != null) {
            wrapper.eq(ProductTag::getStatus, status);
        }
        wrapper.orderByDesc(ProductTag::getCreateTime);

        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    private void checkUnique(Long id, SFunction<ProductTag, ?> column, Object value, String errorMsg) {
        LambdaQueryWrapper<ProductTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(column, value);
        if (id != null) wrapper.ne(ProductTag::getId, id);
        if (this.count(wrapper) > 0) throw new RuntimeException(errorMsg);
    }
}