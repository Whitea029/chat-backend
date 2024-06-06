package fun.whitea.easychatbackend.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserContactApplyStatusEnum {

    INIT(0,"待处理"),
    PASS(1,"已删除"),
    REJECT(2,"已拒绝"),
    BLACKLIST(3,"已拉黑"),
    ;
    private final Integer status;
    private final String desc;

    public static UserContactApplyStatusEnum getByStatus(Integer status) {
        for (UserContactApplyStatusEnum e : UserContactApplyStatusEnum.values()) {
            if (e.status.equals(status)) {
                return e;
            }
        }
        return null;
    }

}
