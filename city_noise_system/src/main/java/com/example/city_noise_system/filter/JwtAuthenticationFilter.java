package com.example.city_noise_system.filter;

import com.example.city_noise_system.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * JWT认证过滤器
 * 作用：拦截所有API请求，验证JWT Token是否有效
 * 注意：Spring Boot 3.x 使用 jakarta.servlet 包
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    // 不需要认证的路径列表（白名单）
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/auth/login", // 登录接口
            "/api/auth/register", // 注册接口
            "/api/test/", // 测试接口
            "/api/monitor/generate-data", // 数据生成接口
            "/swagger-ui", // Swagger UI页面
            "/v3/api-docs", // OpenAPI文档
            "/uploads/", // 上传文件访问
            "/error" // 错误页面
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 获取请求的URI
        String requestURI = request.getRequestURI();
        log.debug("处理请求: {} {}", request.getMethod(), requestURI);

        // 检查是否是白名单路径
        if (isWhiteListPath(requestURI)) {
            log.debug("白名单路径，跳过认证: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // 从请求头获取Token
        String authHeader = request.getHeader("Authorization");

        // 检查Token是否存在
        if (!StringUtils.hasText(authHeader)) {
            log.warn("请求缺少Authorization头: {}", requestURI);
            sendUnauthorizedError(response, "未提供认证Token");
            return;
        }

        // 检查Token格式（应该是 "Bearer {token}"）
        if (!authHeader.startsWith("Bearer ")) {
            log.warn("Token格式不正确，应该以'Bearer '开头: {}", authHeader);
            sendUnauthorizedError(response, "Token格式不正确，应该以'Bearer '开头");
            return;
        }

        // 提取真正的Token（去掉"Bearer "前缀）
        String token = authHeader.substring(7);

        // 验证Token是否有效
        if (!jwtUtil.validateToken(token)) {
            log.warn("Token已过期或无效: {}", token);
            sendUnauthorizedError(response, "Token已过期或无效");
            return;
        }

        // 从Token中解析用户信息
        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);

        if (userId == null || username == null || role == null) {
            log.warn("Token解析失败，无法获取用户信息: {}", token);
            sendUnauthorizedError(response, "Token解析失败");
            return;
        }

        // 将用户信息存入请求属性，方便后续Controller使用
        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        request.setAttribute("role", role);

        log.debug("用户认证通过: {} (ID: {}, 角色: {}) 访问 {}", username, userId, role, requestURI);

        // 继续处理请求
        filterChain.doFilter(request, response);
    }

    /**
     * 检查请求路径是否在白名单中
     * 
     * @param requestURI 请求的URI
     * @return 如果在白名单中返回true，否则返回false
     */
    private boolean isWhiteListPath(String requestURI) {
        for (String whitePath : WHITE_LIST) {
            if (requestURI.startsWith(whitePath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回401未授权错误响应
     * 
     * @param response HTTP响应对象
     * @param message  错误信息
     * @throws IOException IO异常
     */
    private void sendUnauthorizedError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        String jsonResponse = String.format(
                "{\"code\":401,\"message\":\"%s\",\"data\":null}",
                message);

        PrintWriter writer = response.getWriter();
        writer.write(jsonResponse);
        writer.flush();
    }
}