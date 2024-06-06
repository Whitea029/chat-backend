package fun.whitea.easychatbackend.entity.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误枚举类
 */
@Getter
@AllArgsConstructor
public enum ErrorEnum {
    // 错误
    COMMON_ERROR(1000, "error"),
    PARAM_ERROR(1001, "Params error"),
    PARAM_TYPE_ERROR(1002, "Param type error"),

    EMAIL_ERROR(1003, "Email error"),
    LOGIN_ERROR(1004, "Login error"),

    PERMISSION_ERROR(1005, "Permission error"),

    GROUP_ERROR(1006, "Group error"),
    APPLY_ERROR(1007, "Apply error"),
    CONCURRENCY_ERROR(1008, "Concurrency error"),
    ;

    private final Integer code;
    private final String errMsg;
}
