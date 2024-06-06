package fun.whitea.easychatbackend.entity.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserContactStatusEnum {
    NOT_FRIEND(0, "非好友"),
    FRIEND(1, "好友"),
    DEL(2, "已删除好友"),
    DEL_BE(3, "被好友删除"),
    BLACKLIST(4, "已拉黑好友"),
    BLACKLIST_BE(5, "被好友拉黑"),
    BLACKLIST_BE_FIRST(6, "首次被好友拉黑")

;
    private final Integer status;
    private final String desc;

    public static UserContactStatusEnum getByStatus(Integer status) {
        for (UserContactStatusEnum e : UserContactStatusEnum.values()) {
            if (e.status.equals(status)) {
                return e;
            }
        }
        return null;
    }

}
