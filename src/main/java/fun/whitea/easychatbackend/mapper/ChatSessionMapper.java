package fun.whitea.easychatbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.whitea.easychatbackend.entity.po.ChatSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}
