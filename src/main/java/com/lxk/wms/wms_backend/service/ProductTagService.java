package com.lxk.wms.wms_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lxk.wms.wms_backend.entity.ProductTag;

public interface ProductTagService extends IService<ProductTag> {
    // 新增/修改 (含唯一性校验)
    boolean saveProductTag(ProductTag productTag);

    // 分页查询
    IPage<ProductTag> pageQuery(Integer pageNum, Integer pageSize, String name, Integer status);
}