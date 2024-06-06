package fun.whitea.easychatbackend.entity.enums;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum UserContactTypeEnum {

    USER(0, "U", "好友"),
    GROUP(1, "G", "群"),

    ;
    private final Integer type;
    private final String prefix;
    private final String desc;

    public static UserContactTypeEnum getByName(String name) {
        if (name == null) {
            return null;
        }
        return UserContactTypeEnum.valueOf(name.toUpperCase());
    }

    public static UserContactTypeEnum getByPrefix(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return null;
        }
        prefix = prefix.substring(0, 1);
        for (UserContactTypeEnum type : UserContactTypeEnum.values()) {
            if (type.getPrefix().equals(prefix)) {
                return type;
            }
        }
        return null;
    }

}
