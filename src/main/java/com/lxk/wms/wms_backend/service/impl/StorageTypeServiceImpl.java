package com.lxk.wms.wms_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lxk.wms.wms_backend.entity.StorageType;
import com.lxk.wms.wms_backend.mapper.StorageTypeMapper;
import com.lxk.wms.wms_backend.service.StorageTypeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class StorageTypeServiceImpl extends ServiceImpl<StorageTypeMapper, StorageType> implements StorageTypeService {

    @Override
    public boolean saveStorageType(StorageType storageType) {
        // 1. 校验名称唯一性
        checkUnique(storageType.getId(), StorageType::getTypeName, storageType.getTypeName(), "存放类型名称已存在");

        // 2. 校验编码唯一性
        checkUnique(storageType.getId(), StorageType::getTypeCode, storageType.getTypeCode(), "存放类型编码已存在");

        return this.saveOrUpdate(storageType);
    }

    @Override
    public IPage<StorageType> pageQuery(Integer pageNum, Integer pageSize, String name, Integer status) {
        LambdaQueryWrapper<StorageType> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(StorageType::getTypeName, name);
        }
        if (status != null) {
            wrapper.eq(StorageType::getStatus, status);
        }
        wrapper.orderByDesc(StorageType::getCreateTime);

        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    private void checkUnique(Long id, SFunction<StorageType, ?> column, Object value, String errorMsg) {
        LambdaQueryWrapper<StorageType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(column, value);
        if (id != null) wrapper.ne(StorageType::getId, id);
        if (this.count(wrapper) > 0) throw new RuntimeException(errorMsg);
    }
}