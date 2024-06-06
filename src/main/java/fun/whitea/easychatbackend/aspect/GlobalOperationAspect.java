package fun.whitea.easychatbackend.aspect;

import fun.whitea.easychatbackend.annotation.GlobalInterceptor;
import fun.whitea.easychatbackend.entity.constants.Constants;
import fun.whitea.easychatbackend.entity.dto.TokenUserInfoDto;
import fun.whitea.easychatbackend.entity.enums.ErrorEnum;
import fun.whitea.easychatbackend.exception.BusinessException;
import fun.whitea.easychatbackend.utils.RedisUtil;
import lombok.SneakyThrows;
import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import java.lang.reflect.Method;

@Aspect
@Component
public class GlobalOperationAspect {

    @Resource
    RedisUtil redisUtil;

    @Pointcut("@annotation(fun.whitea.easychatbackend.annotation.GlobalInterceptor)")
    private void pointcut() {

    }

    @Before("pointcut()")
    @SneakyThrows
    public void loginIntercept(JoinPoint joinPoint) {
        Method method =  ((MethodSignature) joinPoint.getSignature()).getMethod();
        val annotation = method.getAnnotation(GlobalInterceptor.class);
        if (annotation == null) {
            return;
        } else if (annotation.checkLogin() || annotation.checkAdmin()) {
            checkLogin(annotation.checkAdmin());
        }
    }

    private void checkLogin(boolean checkAdmin) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        val request = attributes.getRequest();
        val token = request.getHeader("TOKEN");
        if (token == null || token.isEmpty()) {
            throw new BusinessException(ErrorEnum.LOGIN_ERROR, "login timeout");
        }
        TokenUserInfoDto tokenUserInfoDto = (TokenUserInfoDto) redisUtil.get(Constants.REDIS_KEY_WS_TOKEN + token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ErrorEnum.LOGIN_ERROR, "login timeout");
        } else if (checkAdmin && tokenUserInfoDto.getAdmin()) {
            throw new BusinessException(ErrorEnum.PERMISSION_ERROR, "you are not admin");
        }
    }
}
