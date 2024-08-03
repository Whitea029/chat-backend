package fun.whitea.easychatbackend.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class MessageSendDto<T> implements Serializable {

    private Long messageId;
    private String sessionId;
    private String sendUserId;
    private String sendUserNickName;
    private String contactId;
    private String contactName;
    private String messageContent;
    private String lastMessage;
    private Integer messageType;
    private Long sendTime;
    private Integer contactType;
    private T extendData;

    // 0：发送中 1：已发送 对于文件是异步上传用状态处理
    private Integer status;

    private Long fileSize;
    private String fileName;
    private Integer fileType;

    private Integer memberCount;

    public String getLastMessage() {
        if (StringUtils.isEmpty(lastMessage)) {
            return messageContent;
        }
        return lastMessage;
    }

}
