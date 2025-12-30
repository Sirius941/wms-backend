package com.lxk.wms.wms_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lxk.wms.wms_backend.entity.Bin;

public interface BinService extends IService<Bin> {
    // 新增/修改 (会自动处理层级ID)
    boolean saveBin(Bin bin);

    // 分页查询 (支持全链路筛选)
    IPage<Bin> pageQuery(Integer pageNum, Integer pageSize, String binCode,
                         Long warehouseId, Long zoneId, Long rackId, Integer status, Integer usageStatus);
}