package fun.whitea.easychatbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import fun.whitea.easychatbackend.config.AppConfig;
import fun.whitea.easychatbackend.entity.constants.Constants;
import fun.whitea.easychatbackend.entity.dto.SysSettingDto;
import fun.whitea.easychatbackend.entity.enums.ErrorEnum;
import fun.whitea.easychatbackend.entity.enums.GroupStatusEnum;
import fun.whitea.easychatbackend.entity.enums.UserContactStatusEnum;
import fun.whitea.easychatbackend.entity.enums.UserContactTypeEnum;
import fun.whitea.easychatbackend.entity.po.GroupInfo;
import fun.whitea.easychatbackend.entity.po.UserContact;
import fun.whitea.easychatbackend.exception.BusinessException;
import fun.whitea.easychatbackend.mapper.GroupInfoMapper;
import fun.whitea.easychatbackend.mapper.UserContactMapper;
import fun.whitea.easychatbackend.service.GroupInfoService;
import fun.whitea.easychatbackend.utils.RedisComponent;
import fun.whitea.easychatbackend.utils.StringTool;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;
import java.util.List;

@Service
public class GroupInfoServiceImpl implements GroupInfoService {

    @Resource
    RedisComponent redisComponent;
    @Resource
    GroupInfoMapper groupInfoMapper;
    @Resource
    UserContactMapper userContactMapper;
    @Resource
    AppConfig appConfig;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SneakyThrows
    public void saveGroup(String userId, String id, String groupName, String groupNotice, Integer joinType, MultipartFile avatarFile, MultipartFile avatarCover) {
        Date curDate = new Date();
        GroupInfo groupInfo = GroupInfo.builder()
                .id(id)
                .groupOwnerId(userId)
                .groupName(groupName)
                .groupNotice(groupNotice)
                .joinType(joinType)
                .build();
        // 新增
        if (StringTool.isEmpty(id)) {
            val count = groupInfoMapper.selectCount(new LambdaQueryWrapper<GroupInfo>().eq(GroupInfo::getGroupOwnerId, userId));
            SysSettingDto sysSettingDto = redisComponent.getSysSetting();
            if (count >= sysSettingDto.getMaxGroupCount()) {
                throw new BusinessException(ErrorEnum.GROUP_ERROR, "you can create up to " + sysSettingDto.getMaxGroupCount() + " group chats");
            } else if (avatarFile == null) {
                throw new BusinessException(ErrorEnum.PARAM_ERROR, "avatar file is null");
            }
            groupInfo.setId(StringTool.getGroupId());
            groupInfo.setCreateTime(curDate);
            groupInfo.setStatus(GroupStatusEnum.ENABLE.getStatus());
            groupInfoMapper.insert(groupInfo);
            // 将群组添加为联系人
            UserContact userContact = UserContact.builder()
                    .status(UserContactStatusEnum.FRIEND.getStatus())
                    .contactType(UserContactTypeEnum.GROUP.getType())
                    .contactId(groupInfo.getId())
                    .id(userId)
                    .createTime(curDate)
                    .lastUpdateTime(curDate)
                    .build();
            userContactMapper.insert(userContact);

            // TODO 创建会话
            // TODO 发送消息
        } else {
            val dbInfo = groupInfoMapper.selectOne(new LambdaQueryWrapper<GroupInfo>().eq(GroupInfo::getId, id));
            if (!dbInfo.getGroupOwnerId().equals(userId)) {
                throw new BusinessException(ErrorEnum.PARAM_ERROR, "user id not match");
            }
            groupInfoMapper.updateById(groupInfo);

            // TODO 更新相关表冗余信息
            // TODO 修改群昵称发送ws信息
        }
        if (avatarFile == null) {
            return;
        }
        String baseFolder = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
        File targetFileFolder = new File(baseFolder + Constants.FILE_FOLDER_AVATAR_NAME);
        if (!targetFileFolder.exists()) {
            targetFileFolder.mkdirs();
        }
        String filePath = targetFileFolder.getPath() + "/" + id + Constants.IMAGE_SUFFIX;
        avatarFile.transferTo(new File(filePath));
        avatarCover.transferTo(new File(filePath + Constants.COVER_IMAGE_SUFFIX));

    }

    @Override
    public List<GroupInfo> getGroups(String userId) {
        return groupInfoMapper.selectList(new LambdaQueryWrapper<GroupInfo>().eq(GroupInfo::getGroupOwnerId, userId).orderByDesc(GroupInfo::getCreateTime));
    }

    @Override
    public GroupInfo getGroup(String userId, String id) {
        val userContact = userContactMapper.selectOne(new LambdaQueryWrapper<UserContact>().eq(UserContact::getId, userId).eq(UserContact::getContactId, id));
        if (userContact == null || !UserContactStatusEnum.FRIEND.getStatus().equals(userContact.getStatus())) {
            throw new BusinessException(ErrorEnum.GROUP_ERROR, "You have not joined the group or group");
        }
        val groupInfo = groupInfoMapper.selectOne(new LambdaQueryWrapper<GroupInfo>().eq(GroupInfo::getId, id));
        if (groupInfo == null || !GroupStatusEnum.ENABLE.getStatus().equals(groupInfo.getStatus())) {
            throw new BusinessException(ErrorEnum.GROUP_ERROR, "Group is not exist or not enable");
        }
        val count = userContactMapper.selectCount(new LambdaQueryWrapper<UserContact>().eq(UserContact::getContactId, id));
        groupInfo.setMemberCount(count);
        return groupInfo;
    }
}
