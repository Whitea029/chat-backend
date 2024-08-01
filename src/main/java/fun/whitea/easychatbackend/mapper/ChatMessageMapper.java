package fun.whitea.easychatbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.whitea.easychatbackend.entity.po.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
