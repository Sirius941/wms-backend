package com.lxk.wms.wms_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lxk.wms.wms_backend.entity.User;

public interface UserService extends IService<User> {

    /**
     * 新增或修改用户（包含唯一性校验逻辑）
     * @param user 用户信息
     * @return 是否成功
     */
    boolean saveUser(User user);
}