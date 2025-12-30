package com.lxk.wms.wms_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lxk.wms.wms_backend.entity.Product;

public interface ProductService extends IService<Product> {
    // 保存产品 (含唯一性校验)
    boolean saveProduct(Product product);

    // 分页查询 (含多表关联名称填充)
    IPage<Product> pageQuery(Integer pageNum, Integer pageSize, String keyword, Long categoryId, Integer status);
}