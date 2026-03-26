package com.example.city_noise_system.handler;

import com.example.city_noise_system.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * 全局异常处理类
 * 统一处理系统中的各种异常，提供清晰的错误信息
 * 同时记录详细的异常日志，便于问题排查
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理系统级异常
     * 捕获所有未被其他异常处理器捕获的异常
     * 
     * @param e 异常对象
     * @return 统一的错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResultVO<String> handleException(Exception e) {
        // 记录详细的异常日志，包括堆栈信息
        log.error("系统异常: ", e);
        // 返回统一的系统错误信息，避免暴露敏感信息
        return ResultVO.error(500, "系统繁忙，请稍后重试");
    }

    /**
     * 处理业务级异常
     * 捕获业务逻辑中抛出的RuntimeException
     * 
     * @param e 业务异常对象
     * @return 包含业务错误信息的响应
     */
    @ExceptionHandler(RuntimeException.class)
    public ResultVO<String> handleRuntimeException(RuntimeException e) {
        // 记录业务异常日志
        log.warn("业务异常: {}", e.getMessage());
        // 直接返回业务错误信息，因为这些信息通常是用户友好的
        return ResultVO.error(400, e.getMessage());
    }

    /**
     * 处理文件上传大小超限异常
     * 
     * @return 文件大小超限的错误响应
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResultVO<String> handleMaxUploadSizeExceededException() {
        log.warn("文件大小超限");
        return ResultVO.error(400, "文件大小不能超过10MB");
    }

    /**
     * 处理404异常
     * 当请求的资源不存在时触发
     * 
     * @param e 404异常对象
     * @return 404错误响应
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResultVO<String> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("资源不存在: {}", e.getRequestURL());
        return ResultVO.error(404, "请求的资源不存在");
    }
}
