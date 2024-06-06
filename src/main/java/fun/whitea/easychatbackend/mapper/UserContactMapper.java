package fun.whitea.easychatbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.whitea.easychatbackend.entity.po.UserContact;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserContactMapper extends BaseMapper<UserContact> {
    List<UserContact> selectList4Chat(String id);

    List<UserContact> selectUserContactsAndUserInfo(String id, Integer contactType);

    List<UserContact> selectUserContactsAndGroupInfo(String id, Integer contactType);
}
