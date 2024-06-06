package fun.whitea.easychatbackend.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserStatusEnum {

    ENABLE(0, "启用"),
    UNABLE(1, "停用"),
    ;

    private final Integer status;
    private final String desc;

    public static UserStatusEnum getByStatus(Integer status) {
        for (UserStatusEnum userStatusEnum : UserStatusEnum.values()) {
            if (userStatusEnum.getStatus() == status) {
                return userStatusEnum;
            }
        }
        return null;
    }

}
