package fun.whitea.easychatbackend.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatSession {

    private String sessionId;
    private String lastMessage;
    private Long lastReceiveTime;

}
