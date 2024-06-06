package fun.whitea.easychatbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.whitea.easychatbackend.entity.dto.TokenUserInfoDto;
import fun.whitea.easychatbackend.entity.dto.UserContactSearchResultDto;
import fun.whitea.easychatbackend.entity.po.UserContact;

import java.util.List;

public interface UserContactService{
    List<UserContact> getUserContactList(String id);

    UserContactSearchResultDto search(String userId, String contactId);

    Integer applyAdd(TokenUserInfoDto tokenUserInfoDto, String contactId, String applyInfo);

    void getUserContaactInfo(String userId, String contactId);
}
