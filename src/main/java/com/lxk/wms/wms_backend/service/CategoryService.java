package com.lxk.wms.wms_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lxk.wms.wms_backend.entity.Category;

public interface CategoryService extends IService<Category> {
    // 新增/修改 (含唯一性校验)
    boolean saveCategory(Category category);

    // 分页查询
    IPage<Category> pageQuery(Integer pageNum, Integer pageSize, String name, Integer status);
}