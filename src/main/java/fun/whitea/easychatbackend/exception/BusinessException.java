package fun.whitea.easychatbackend.exception;

import fun.whitea.easychatbackend.entity.enums.ErrorEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务异常类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException {

    private ErrorEnum errorEnum;

    public BusinessException(ErrorEnum errorEnum) {
        super(errorEnum.getErrMsg());
        this.errorEnum = errorEnum;
    }

    public BusinessException(ErrorEnum errorEnum, String message) {
        super(errorEnum.getErrMsg() + "," + message);
        this.errorEnum = errorEnum;
    }

    public BusinessException(String message) {
        super(message);
    }

}
