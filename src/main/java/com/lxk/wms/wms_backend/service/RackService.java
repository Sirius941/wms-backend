package com.lxk.wms.wms_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lxk.wms.wms_backend.entity.Rack;

public interface RackService extends IService<Rack> {
    // 保存/更新
    boolean saveRack(Rack rack);

    // 分页查询 (支持按 仓库+库区+名称 筛选)
    IPage<Rack> pageQuery(Integer pageNum, Integer pageSize, String name, Long warehouseId, Long zoneId, Integer status);
}