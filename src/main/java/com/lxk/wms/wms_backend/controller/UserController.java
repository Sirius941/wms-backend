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
    @Autowired private HttpServletRequest request; // 注入 request 以获取当前用户

    // 1. 登录 (生成 Token)
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User loginUser) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginUser.getUsername());
        wrapper.eq(User::getPassword, loginUser.getPassword());

        User user = userService.getOne(wrapper);
        if (user != null) {
            if(user.getStatus() == 0) return Result.error("账号已被冻结");

            // 生成 Token
            String token = JwtUtils.generateToken(user.getId(), user.getUsername(), user.getRoleKey());

            Map<String, Object> map = new HashMap<>();
            map.put("user", user);
            map.put("token", token);
            return Result.success(map);
        }
        return Result.error("用户名或密码错误");
    }

    // --- 下面的接口都需要鉴权 ---

    // 2. 用户管理列表 (只有 admin 能看)
    @GetMapping("/page")
    public Result<IPage<User>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                    @RequestParam(required = false) String username) {
        // 权限检查
        if (!isAdmin()) return Result.error("无权访问：需要管理员权限");

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if(StringUtils.hasText(username)) wrapper.like(User::getUsername, username);
        return Result.success(userService.page(new Page<>(pageNum, pageSize), wrapper));
    }

    // 3. 新增/修改用户 (只有 admin 能操作)
    @PostMapping("/save")
    public Result<?> save(@RequestBody User user) {
        // 权限检查逻辑保持不变...
        if (!isAdmin()) return Result.error("无权操作");

        try {
            // 改为调用自定义的 saveUser 方法，包含唯一性校验
            userService.saveUser(user);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 4. 删除用户 (只有 admin 能操作)
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        if (!isAdmin()) return Result.error("无权操作");

        // 不能删除自己
        Claims currentUser = (Claims) request.getAttribute("currentUser");
        Long currentUserId = Long.parseLong(currentUser.get("userId").toString());
        if(currentUserId.equals(id)) {
            return Result.error("不能删除自己");
        }

        userService.removeById(id);
        return Result.success();
    }

    /**
     * 辅助方法：判断当前登录用户是否是管理员
     */
    private boolean isAdmin() {
        // 从 request 中拿出拦截器存进去的 claims
        Claims claims = (Claims) request.getAttribute("currentUser");
        if (claims == null) return false;

        String role = (String) claims.get("role");
        return "admin".equals(role); // 假设数据库里管理员的 role_key 是 "admin"
    }
}