package com.lxk.wms.wms_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lxk.wms.wms_backend.entity.Zone;

public interface ZoneService extends IService<Zone> {
    // 保存逻辑（含校验）
    boolean saveZone(Zone zone);

    // 分页查询（含关联仓库名称处理）
    IPage<Zone> pageQuery(Integer pageNum, Integer pageSize, String name, Long warehouseId, Integer status);
}