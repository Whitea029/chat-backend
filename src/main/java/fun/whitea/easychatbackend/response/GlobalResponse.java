package fun.whitea.easychatbackend.response;

import fun.whitea.easychatbackend.entity.enums.ErrorEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 全局统一响应类
 */
@Data
@NoArgsConstructor
@ToString
public class GlobalResponse<T> {

    private boolean success;
    private Integer errCode;
    private String errMsg;
    private T data;

    public static <T> GlobalResponse<T> success(T data) {
        GlobalResponse<T> r = new GlobalResponse<>();
        r.setSuccess(true);
        r.setErrCode(null);
        r.setErrMsg(null);
        r.setData(data);
        return r;
    }

    public static <T> GlobalResponse<T> success() {
        return success(null);
    }

    public static <T> GlobalResponse<T> failure(ErrorEnum errorEnum) {
        GlobalResponse<T> r = new GlobalResponse<>();
        r.setSuccess(false);
        r.setErrCode(errorEnum.getCode());
        r.setErrMsg(errorEnum.getErrMsg());
        r.setData(null);
        return r;
    }

    public static <T> GlobalResponse<T> failure(ErrorEnum errorEnum, String message) {
        GlobalResponse<T> r = new GlobalResponse<>();
        r.setSuccess(false);
        r.setErrCode(errorEnum.getCode());
        r.setErrMsg(errorEnum.getErrMsg() + message);
        r.setData(null);
        return r;
    }


    public static <T> GlobalResponse<T> failure(ErrorEnum errorEnum, String message, T data) {
        GlobalResponse<T> r = new GlobalResponse<>();
        r.setSuccess(false);
        r.setErrCode(errorEnum.getCode());
        r.setErrMsg(errorEnum.getErrMsg() + message);
        r.setData(data);
        return r;
    }

}
