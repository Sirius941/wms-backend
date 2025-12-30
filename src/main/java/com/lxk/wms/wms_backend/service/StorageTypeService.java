package com.lxk.wms.wms_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lxk.wms.wms_backend.entity.StorageType;

public interface StorageTypeService extends IService<StorageType> {
    // 新增/修改 (含唯一性校验)
    boolean saveStorageType(StorageType storageType);

    // 分页查询
    IPage<StorageType> pageQuery(Integer pageNum, Integer pageSize, String name, Integer status);
}