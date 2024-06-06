package fun.whitea.easychatbackend.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum JoinTypeEnum {

    JOIN(0, "直接加入"),
    APPLY(1, "需要审核"),

    ;

    private final Integer type;
    private final String desc;

    public static JoinTypeEnum getByName(String name) {
        for (JoinTypeEnum joinTypeEnum : JoinTypeEnum.values()) {
            if (joinTypeEnum.name().equals(name)) {
                return joinTypeEnum;
            }
        }
        return null;
    }


    public static JoinTypeEnum getByType(Integer type) {
        for (JoinTypeEnum joinTypeEnum : JoinTypeEnum.values()) {
            if (joinTypeEnum.getType().equals(type)) {
                return joinTypeEnum;
            }
        }
        return null;
    }

}
