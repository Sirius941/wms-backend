package com.lxk.wms.wms_backend.config;

import com.lxk.wms.wms_backend.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    public class LoginInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            // 1. 放行 OPTIONS 请求
            if ("OPTIONS".equals(request.getMethod().toUpperCase())) {
                return true;
            }

            // 2. 校验 Token 存在性
            String token = request.getHeader("token"); // 前端 request.ts 中配置的 header key
            if (!StringUtils.hasText(token)) {
                response.setStatus(401);
                return false;
            }

            // 3. 解析 Token
            Claims claims = JwtUtils.getClaimsByToken(token);
            if (claims == null) {
                response.setStatus(401);
                return false;
            }

            // 4. 获取当前用户角色
            String role = (String) claims.get("role");
            String path = request.getRequestURI();

            // 5. 【核心修改】基于角色的简易权限控制
            // 如果是 admin，直接放行 (上帝模式)
            if ("admin".equals(role)) {
                request.setAttribute("currentUser", claims);
                return true;
            }

            // 权限校验失败标志
            boolean hasPermission = checkPermission(role, path);

            if (!hasPermission) {
                response.setStatus(403); // 403 禁止访问
                return false;
            }

            // 校验通过，存入请求域
            request.setAttribute("currentUser", claims);
            return true;
        }

        /**
         * 简单的路径匹配逻辑
         */
        private boolean checkPermission(String role, String path) {
            if (role == null) return false;

            // 1. 基础数据/产品管理
            // 允许: product_manager
            if (path.startsWith("/category") || path.startsWith("/product") || path.startsWith("/unit") || path.startsWith("/client")) {
                return "product_manager".equals(role) || "asset_manager".equals(role); // 仓库管理员可能也需要看产品
            }

            // 2. 仓库管理/库存
            // 允许: asset_manager
            if (path.startsWith("/warehouse") || path.startsWith("/location") || path.startsWith("/inventory")) {
                return "asset_manager".equals(role);
            }

            // 3. 入库业务 (Inbound)
            // 允许: asset_manager (管理入库单), picker (执行收货任务)
            if (path.startsWith("/inbound")) {
                // 如果是具体收货任务 API，picker 可以访问
                if (path.contains("task") || path.contains("receive")) {
                    return "asset_manager".equals(role) || "picker".equals(role);
                }
                // 入库单管理，仅 limit manager
                return "asset_manager".equals(role);
            }

            // 4. 出库业务 (Outbound)
            // 允许: asset_manager (管理出库单), receiver (执行拣货任务)
            if (path.startsWith("/outbound")) {
                if (path.contains("task") || path.contains("pick")) {
                    return "asset_manager".equals(role) || "receiver".equals(role);
                }
                return "asset_manager".equals(role);
            }

            // 5. 系统管理 (User)
            // 允许: 仅 admin (在上面已经处理了，这里默认拦截)
            if (path.startsWith("/user") || path.startsWith("/role")) {
                // 只有获取自己信息的接口允许非 admin 访问
                if (path.contains("/info") || path.contains("/logout")) {
                    return true;
                }
                return false;
            }

            // 默认策略：如果未命中的路径（如首页数据），默认允许登录用户访问
            return true;
        }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/**"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600);
    }
}