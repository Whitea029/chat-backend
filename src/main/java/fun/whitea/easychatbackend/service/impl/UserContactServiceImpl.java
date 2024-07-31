package fun.whitea.easychatbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.whitea.easychatbackend.entity.constants.Constants;
import fun.whitea.easychatbackend.entity.dto.TokenUserInfoDto;
import fun.whitea.easychatbackend.entity.dto.UserContactSearchResultDto;
import fun.whitea.easychatbackend.entity.enums.*;
import fun.whitea.easychatbackend.entity.po.UserContact;
import fun.whitea.easychatbackend.entity.po.UserContactApply;
import fun.whitea.easychatbackend.exception.BusinessException;
import fun.whitea.easychatbackend.mapper.GroupInfoMapper;
import fun.whitea.easychatbackend.mapper.UserContactApplyMapper;
import fun.whitea.easychatbackend.mapper.UserContactMapper;
import fun.whitea.easychatbackend.mapper.UserInfoMapper;
import fun.whitea.easychatbackend.service.UserContactApplyService;
import fun.whitea.easychatbackend.service.UserContactService;
import fun.whitea.easychatbackend.utils.CopyUtil;
import fun.whitea.easychatbackend.utils.StringTool;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Service
public class UserContactServiceImpl implements UserContactService {

    @Resource
    UserContactMapper userContactMapper;
    @Resource
    UserInfoMapper userInfoMapper;
    @Resource
    GroupInfoMapper groupInfoMapper;
    @Resource
    UserContactApplyMapper userContactApplyMapper;
    @Resource
    UserContactApplyService userContactApplyService;

    @Override
    public List<UserContact> getUserContactList(String id) {
        return userContactMapper.selectList4Chat(id);
    }

    @Override
    public UserContactSearchResultDto search(String userId, String contactId) {
        val typeEnum = UserContactTypeEnum.getByPrefix(contactId);
        if (typeEnum == null) {
            return null;
        }
        UserContactSearchResultDto resultDto = new UserContactSearchResultDto();
        switch (typeEnum) {
            case USER -> {
                val userInfo = userInfoMapper.selectById(contactId);
                if (userInfo == null) {
                    return null;
                }
                resultDto = CopyUtil.copy(userInfo, UserContactSearchResultDto.class);
            }
            case GROUP -> {
                val groupInfo = groupInfoMapper.selectById(contactId);
                if (groupInfo == null) {
                    return null;
                }
                resultDto.setNickName(groupInfo.getGroupName());
            }
        }
        resultDto.setContactType(typeEnum.toString());
        resultDto.setContactId(contactId);
        if (userId.equals(contactId)) {
            resultDto.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            return resultDto;
        }
        // 查询是否是好友
        val userContact = userContactMapper.selectOne(new LambdaQueryWrapper<UserContact>().eq(UserContact::getContactId, contactId).eq(UserContact::getId, userId));
        resultDto.setStatus(userContact == null ? null : userContact.getStatus());
        return resultDto;
    }

    /**
     * 申请添加
     *
     * @param tokenUserInfoDto
     * @param contactId
     * @param applyInfo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer applyAdd(TokenUserInfoDto tokenUserInfoDto, String contactId, String applyInfo) {
        UserContactTypeEnum typeEnum = UserContactTypeEnum.getByPrefix(contactId);
        if (typeEnum == null) {
            throw new BusinessException(ErrorEnum.APPLY_ERROR, "apply params error");
        }
        // 申请人
        String applyUserId = tokenUserInfoDto.getUserId();

        // 默认申请信息
        applyInfo = StringTool.isEmpty(applyUserId) ? String.format(Constants.APPLY_INFO_TEMPLATE, tokenUserInfoDto.getNickName()) : applyInfo;

        val curTime = System.currentTimeMillis();
        Integer joinType = null;
        String receiveId = contactId;

        // 查询对方好友是否已经添加，如果已经拉黑则无法添加
        val userContact = userContactMapper.selectOne(new LambdaQueryWrapper<UserContact>().eq(UserContact::getContactId, contactId).eq(UserContact::getId, applyUserId));
        if (userContact != null && (UserContactStatusEnum.BLACKLIST_BE.getStatus().equals(userContact.getStatus()) || UserContactStatusEnum.BLACKLIST_BE_FIRST.getStatus().equals(userContact.getStatus()))) {
            throw new BusinessException(ErrorEnum.APPLY_ERROR, "You have been in his blacklist and cannot be added");
        }

        if (UserContactTypeEnum.GROUP == typeEnum) {
            val groupInfo = groupInfoMapper.selectById(contactId);
            if (groupInfo == null || GroupStatusEnum.UNABLE.getStatus().equals(groupInfo.getStatus())) {
                throw new BusinessException(ErrorEnum.APPLY_ERROR, "The group does not exist or has been disbanded");
            }
            receiveId = groupInfo.getGroupOwnerId();
            joinType = groupInfo.getJoinType();
        } else {
            val userInfo = userInfoMapper.selectById(contactId);
            if (userInfo == null) {
                throw new BusinessException(ErrorEnum.APPLY_ERROR, "User does not exist");
            }
            joinType = userInfo.getJoinType();
        }
        // 直接加入不用记录申请记录
        if (JoinTypeEnum.JOIN.getType().equals(joinType)) {
            // 添加联系人
            userContactApplyService.addContact(applyUserId, receiveId, contactId, typeEnum.getType(), applyInfo);
            return joinType;
        }
        val dbApply = userContactApplyMapper.selectOne(new LambdaQueryWrapper<UserContactApply>().eq(UserContactApply::getContactId, contactId).eq(UserContactApply::getReceiveUserId, receiveId).eq(UserContactApply::getId, applyUserId));
        if (dbApply == null) {
            UserContactApply contactApply = UserContactApply.builder()
                    .applyUserId(applyUserId)
                    .contactType(typeEnum.getType())
                    .receiveUserId(receiveId)
                    .lastApplyTime(curTime)
                    .contactId(contactId)
                    .status(UserContactApplyStatusEnum.INIT.getStatus())
                    .applyInfo(applyInfo)
                    .build();
            userContactApplyMapper.insert(contactApply);
        } else {
            // 更新状态
            UserContactApply contactApply = UserContactApply.builder()
                    .status(UserContactApplyStatusEnum.INIT.getStatus())
                    .lastApplyTime(curTime)
                    .applyInfo(applyInfo)
                    .build();
            userContactApplyMapper.updateById(contactApply);
        }
        if (dbApply == null || !UserContactApplyStatusEnum.INIT.getStatus().equals(dbApply.getStatus())) {
            // TODO 发送ws消息
        }
        return joinType;
    }

    @Override
    public void getUserContactInfo(String userId, String contactId) {

    }


    @Override
    public void removeUserContact(String userId, String contactId, UserContactStatusEnum userContactStatusEnum) {
        // 移除好友
        userContactMapper.update(new UpdateWrapper<UserContact>()
                .set("status", userContactStatusEnum.getStatus())
                .eq("contact_id", contactId)
                .eq("id", userId));
        // 将好友中也移除自己
        UpdateWrapper<UserContact> userContactUpdateWrapper = new UpdateWrapper<>();
        if (UserContactStatusEnum.DEL == userContactStatusEnum) {
            userContactUpdateWrapper.set("status", UserContactStatusEnum.DEL_BE.getStatus());
        } else if (UserContactStatusEnum.BLACKLIST == userContactStatusEnum) {
            userContactUpdateWrapper.set("status", UserContactStatusEnum.BLACKLIST_BE.getStatus());
        }
        userContactMapper.update(userContactUpdateWrapper.eq("id", contactId).eq("contact_id", userId));
        // todo 从我的好友列表缓存中删除好友
        // todo 从好友缓存列表中删除我
    }

    List<List<String>> res = new ArrayList<>();
    List<String> path = new LinkedList<>();

    public List<List<String>> partition(String s) {
        backTracking(s, 0);
        return res;
    }

    public void backTracking(String s, int startIndex) {
        if (startIndex > s.length()) {
            res.add(new ArrayList<>(path));
            return;
        }
        for (int i = startIndex; i < s.length(); i++) {
            if (fun(s.substring(startIndex, i))) {
                path.add(s.substring(startIndex, i));
            } else {
                continue;
            }
            backTracking(s, i + 1);
            path.removeLast();
        }
    }

    public Boolean fun(String s) {
        for (int i = 0, j = s.length() - 1; i < j; i++, j--) {
            if (s.charAt(i) != s.charAt(j)) {
                return false;
            }
        }
        return true;
    }
}
