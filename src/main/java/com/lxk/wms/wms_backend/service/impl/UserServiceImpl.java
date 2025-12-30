package com.lxk.wms.wms_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lxk.wms.wms_backend.entity.User;
import com.lxk.wms.wms_backend.mapper.UserMapper;
import com.lxk.wms.wms_backend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public boolean saveUser(User user) {
        // 1. 校验用户名唯一性
        if (StringUtils.hasText(user.getUsername())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, user.getUsername());

            // 如果是更新操作（id存在），需要排除掉自己，防止"自己和自己重名"报错
            if (user.getId() != null) {
                wrapper.ne(User::getId, user.getId());
            }

            if (this.count(wrapper) > 0) {
                throw new RuntimeException("用户名 [" + user.getUsername() + "] 已存在，请更换");
            }
        }

        // 2. 密码处理 (可选)
        // 如果是新增用户且没填密码，给一个默认密码
        if (user.getId() == null && !StringUtils.hasText(user.getPassword())) {
            user.setPassword("123456");
        }

        // 3. 保存或更新
        return this.saveOrUpdate(user);
    }
}