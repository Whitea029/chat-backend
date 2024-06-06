package fun.whitea.easychatbackend.exception;



import fun.whitea.easychatbackend.entity.enums.ErrorEnum;
import fun.whitea.easychatbackend.response.GlobalResponse;
import fun.whitea.easychatbackend.utils.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常处理
     *
     * @param e
     * @param <T>
     * @return
     */
    @ExceptionHandler(BusinessException.class)
    public <T> GlobalResponse<T> handleBusinessException(BusinessException e) {
        if (e == null) {
            return GlobalResponse.failure(ErrorEnum.COMMON_ERROR);
        }
        log.error("业务异常\n {}", TraceUtil.generateTraceMessage(e));
        ErrorEnum errorEnum = e.getErrorEnum();
        if (!e.getMessage().isEmpty()) {
            if (errorEnum != null) {
                return GlobalResponse.failure(errorEnum, e.getMessage());
            }
            return GlobalResponse.failure(ErrorEnum.COMMON_ERROR, e.getMessage());
        }
        return GlobalResponse.failure(errorEnum == null ? ErrorEnum.COMMON_ERROR : errorEnum);
    }

    /**
     * 参数校验异常处理
     *
     * @param e
     * @param <T>
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public <T> GlobalResponse<T> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("参数校验异常\n {}", TraceUtil.generateTraceMessage(e));
        String messages = e.getBindingResult().getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining("\n"));
        return GlobalResponse.failure(ErrorEnum.PARAM_ERROR, messages);
    }

    /**
     * 参数解析异常处理
     *
     * @param e
     * @param <T>
     * @return
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public <T> GlobalResponse<T> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("参数解析异常\n {}", TraceUtil.generateTraceMessage(e));
        return GlobalResponse.failure(ErrorEnum.PARAM_ERROR, "Invalid JSON format");
    }

    /**
     * 缺少参数异常处理
     *
     * @param e
     * @param <T>
     * @return
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public <T> GlobalResponse<T> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.error("缺少参数异常\n {}", TraceUtil.generateTraceMessage(e));
        return GlobalResponse.failure(ErrorEnum.PARAM_ERROR, e.getParameterName() + " should not be null");
    }


    /**
     * 服务器内部异常处理
     *
     * @param e
     * @param <T>
     * @return
     */
    @ExceptionHandler(Exception.class)
    public <T> GlobalResponse<T> handleException(Exception e) {
        log.error("服务器内部异常\n {}", TraceUtil.generateTraceMessage(e));
        return GlobalResponse.failure(ErrorEnum.COMMON_ERROR, "Server internal error, please contact the administrator");
    }
}
