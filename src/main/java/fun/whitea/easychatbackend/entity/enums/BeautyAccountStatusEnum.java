package fun.whitea.easychatbackend.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BeautyAccountStatusEnum {

    NO_USE(0, "未使用"),
    USED(1, "已使用"),

    ;
    private final Integer status;
    private final String desc;
    public static BeautyAccountStatusEnum getByStatus(Integer status) {
        for (BeautyAccountStatusEnum e : BeautyAccountStatusEnum.values()) {
            if (e.status.equals(status)) {
                return e;
            }
        }
        return null;
    }


}
