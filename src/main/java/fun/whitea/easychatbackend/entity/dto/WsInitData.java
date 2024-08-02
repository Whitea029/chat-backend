package fun.whitea.easychatbackend.entity.dto;

import fun.whitea.easychatbackend.entity.po.ChatMessage;
import fun.whitea.easychatbackend.entity.po.ChatSessionUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WsInitData {

    private List<ChatSessionUser> chatSessionList;

    private List<ChatMessage> chatMessageList;

    private Integer applyCount;
}
