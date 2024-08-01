package fun.whitea.easychatbackend.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatSessionUser {

    private String userId; // 用户ID
    private String contactId; // 联系人ID
    private String sessionId; // 会话ID
    private String contactName; // 联系人姓名
    @TableField(exist = false)
    private String lastMessage;
    @TableField(exist = false)
    private String lastReceiveTime;
    @TableField(exist = false)
    private Integer MemberCount;

}
