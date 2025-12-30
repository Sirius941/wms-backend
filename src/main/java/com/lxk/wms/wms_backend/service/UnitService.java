package com.lxk.wms.wms_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lxk.wms.wms_backend.entity.Unit;

public interface UnitService extends IService<Unit> {
    // 新增/修改 (含唯一性校验)
    boolean saveUnit(Unit unit);

    // 分页查询
    IPage<Unit> pageQuery(Integer pageNum, Integer pageSize, String name, Integer status);
}