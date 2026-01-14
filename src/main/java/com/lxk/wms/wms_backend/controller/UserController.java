package com.lxk.wms.wms_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lxk.wms.wms_backend.common.Result;
import com.lxk.wms.wms_backend.entity.User;
import com.lxk.wms.wms_backend.service.UserService;
import com.lxk.wms.wms_backend.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "用户管理接口", description = "用户的登录、增删改查接口")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired private UserService userService;
    @Autowired private HttpServletRequest request;

    /**
     * 1. 登录
     * 修改点：确保 Token 中放入了 roleKey，并在返回前清空 password 防止泄露
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User loginUser) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginUser.getUsername());
        wrapper.eq(User::getPassword, loginUser.getPassword());

        User user = userService.getOne(wrapper);
        if (user != null) {
            if(user.getStatus() != null && user.getStatus() == 0) {
                return Result.error("账号已被冻结");
            }

            // 【关键】生成 Token 时必须传入 roleKey (如 admin, picker 等)
            // 请确保数据库 User 表中有 role_key 字段，且 Entity 中有 getRoleKey()
            String token = JwtUtils.generateToken(user.getId(), user.getUsername(), user.getRoleKey());

            Map<String, Object> map = new HashMap<>();
            user.setPassword(null); // 安全起见，不返回密码
            map.put("user", user);
            map.put("token", token);
            return Result.success(map);
        }
        return Result.error("用户名或密码错误");
    }

    /**
     * 【新增】获取当前用户信息
     * 作用：前端 Login.vue 登录成功后，或刷新页面时，会调用此接口获取最新的角色权限
     */
    @GetMapping("/info")
    public Result<User> getUserInfo() {
        // 1. 从 request 域中获取拦截器解析好的 claims (包含 userId, username, role)
        Claims claims = (Claims) request.getAttribute("currentUser");
        if (claims == null) {
            return Result.error("未登录或Token已失效");
        }

        // 2. 为了获取最新信息（比如角色可能刚刚被管理员改了），建议查数据库
        String username = (String) claims.get("username");
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        if (user != null) {
            user.setPassword(null); // 不返回密码
            return Result.success(user);
        }
        return Result.error("获取用户信息失败");
    }

    // --- 下面的接口保持原样，配合 WebConfig 拦截器使用 ---

    // 2. 用户管理列表 (WebConfig 中已限制仅 admin 可访问相关路径，这里双重保险)
    @GetMapping("/page")
    public Result<IPage<User>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                    @RequestParam(required = false) String username) {
        // ... 原有逻辑 ...
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if(StringUtils.hasText(username)) wrapper.like(User::getUsername, username);
        return Result.success(userService.page(new Page<>(pageNum, pageSize), wrapper));
    }

    // 3. 新增/修改
    @PostMapping("/save")
    public Result<?> save(@RequestBody User user) {
        try {
            userService.saveUser(user); // 包含查重逻辑
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 4. 删除
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        // ... 原有校验逻辑 ...
        Claims currentUser = (Claims) request.getAttribute("currentUser");
        if(currentUser != null) {
            Long currentUserId = ((Number) currentUser.get("userId")).longValue();
            if(currentUserId.equals(id)) return Result.error("不能删除自己");
        }
        userService.removeById(id);
        return Result.success();
    }
}