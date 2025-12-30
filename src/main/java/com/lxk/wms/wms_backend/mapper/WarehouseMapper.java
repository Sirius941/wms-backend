package com.lxk.wms.wms_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lxk.wms.wms_backend.entity.Warehouse;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WarehouseMapper extends BaseMapper<Warehouse> {
    // 可以在这里手写复杂的自定义 SQL，但基础 CRUD 不需要
}
