package com.lxk.wms.wms_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lxk.wms.wms_backend.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // MyBatis-Plus 已内置基础 CRUD，暂无需手写 SQL
}