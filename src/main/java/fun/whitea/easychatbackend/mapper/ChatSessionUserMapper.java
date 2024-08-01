package fun.whitea.easychatbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.whitea.easychatbackend.entity.po.ChatSessionUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChatSessionUserMapper extends BaseMapper<ChatSessionUser> {

    List<ChatSessionUser> selectListByUserId(String userId);
}
