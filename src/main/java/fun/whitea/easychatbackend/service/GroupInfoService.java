package fun.whitea.easychatbackend.service;

import fun.whitea.easychatbackend.entity.po.GroupInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface GroupInfoService {

    void saveGroup(String userId, String id, String groupName, String groupNotice, Integer joinType, MultipartFile avatarFile, MultipartFile avatarCover);

    List<GroupInfo> getGroups(String userId);

    GroupInfo getGroup(String userId, String id);
}
