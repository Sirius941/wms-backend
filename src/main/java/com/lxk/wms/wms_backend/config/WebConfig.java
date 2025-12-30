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

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 1. 定义拦截器逻辑
    public class LoginInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            // 如果是 OPTIONS 请求（前端跨域预检），直接放行
            if("OPTIONS".equals(request.getMethod().toUpperCase())) {
                return true;
            }

            String token = request.getHeader("token");
            if (!StringUtils.hasText(token)) {
                response.setStatus(401); // 401 未授权
                return false;
            }

            Claims claims = JwtUtils.getClaimsByToken(token);
            if (claims == null) {
                response.setStatus(401);
                return false;
            }

            // 把解析出来的用户信息存入 request，方便 Controller 使用
            request.setAttribute("currentUser", claims);
            return true;
        }
    }

    // 2. 注册拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**") // 拦截所有路径
                .excludePathPatterns("/user/login", "/user/register",                    // Knife4j / Swagger UI
                        "/doc.html",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/webjars/**",

                        // springdoc-openapi
                        "/v3/api-docs",
                        "/v3/api-docs/**"); // 放行登录和注册
    }

    // 3. 解决跨域问题 (Vue连后端必须配这个)
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600);
    }
}