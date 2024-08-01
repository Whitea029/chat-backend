package fun.whitea.easychatbackend.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageTypeEnum {

    INIT(0, "", "连接ws获取信息"),
    ADD_FRIEND(1, "", "收到好友打招呼消息"),
    CHAT(2, "", "普通聊天消息"),
    GROUP_CREATE(3, "群组已经创建，可以和好友一起畅聊了", "群创建成功"),
    CONTACT_APPLY(4, "", "好友申请"),
    MEDIA_CHAT(5, "", "媒体文件"),
    FILE_UPLOAD(6, "", "文件上传完成"),
    FORCE_OFF_LINE(7, "", "强制下线"),
    DISSOLUTION_GROUP(8, "群聊已经解散", "解散群聊"),
    ADD_GROUP(9, "%s加入了群组", "加入群组"),
    GROUP_NAME_UPDATE(10, "", "更新群组昵称"),
    LEAVE_GROUP(11, "%s退出了群聊", "退出群聊"),
    REMOVE_GROUP(12, "该管理员移出了群聊", "该管理员移出了群聊");

    private final Integer type;
    private final String initMessage;
    private final String desc;

    public static MessageTypeEnum getByType(Integer type) {
        for (MessageTypeEnum e : MessageTypeEnum.values()) {
            if (e.type == type) {
                return e;
            }
        }
        return null;
    }

}
