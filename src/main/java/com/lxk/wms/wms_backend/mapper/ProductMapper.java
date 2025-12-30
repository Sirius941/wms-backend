package com.lxk.wms.wms_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lxk.wms.wms_backend.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}