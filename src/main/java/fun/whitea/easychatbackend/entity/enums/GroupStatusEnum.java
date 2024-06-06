package fun.whitea.easychatbackend.entity.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public enum GroupStatusEnum {

    ENABLE(1, "正常"),
    UNABLE(0, "解散"),


    ;

    private final Integer status;
    private final String desc;

    public static GroupStatusEnum getByStatus(Integer status) {
        for (GroupStatusEnum groupStatusEnum : GroupStatusEnum.values()) {
            if (Objects.equals(groupStatusEnum.getStatus(), status)) {
                return groupStatusEnum;
            }
        }
        return null;
    }

}
