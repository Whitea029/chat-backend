package fun.whitea.easychatbackend.response;

import fun.whitea.easychatbackend.entity.enums.ErrorEnum;
import fun.whitea.easychatbackend.utils.JsonUtils;
import lombok.SneakyThrows;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;


/**
 * 全局响应处理器
 */
@RestControllerAdvice
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {


    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @SneakyThrows
    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        return switch (body) {
            case null -> GlobalResponse.success();
            case GlobalResponse<?> resp -> resp;
            case String str -> {
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                yield GlobalResponse.success(JsonUtils.convertObj2Json(str));
            }
            case ErrorEnum error -> GlobalResponse.failure(error);
            default -> GlobalResponse.success(body);
        };
    }
}



