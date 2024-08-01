package fun.whitea.easychatbackend.entity.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {

    private Long messageId;
    private String sessionId;
    private Integer messageType;
    private String messageContent;
    private String sendUserId;
    private String sendUserNickName;
    private Long sendTime;
    private String contactId;
    private Integer contactType;
    private Long fileSize;
    private String fileName;
    private Integer fileType;
    private Integer status;

}
