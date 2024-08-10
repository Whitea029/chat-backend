package fun.whitea.easychatbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.whitea.easychatbackend.entity.constants.Constants;
import fun.whitea.easychatbackend.entity.dto.MessageSendDto;
import fun.whitea.easychatbackend.entity.enums.*;
import fun.whitea.easychatbackend.entity.po.*;
import fun.whitea.easychatbackend.exception.BusinessException;
import fun.whitea.easychatbackend.mapper.*;
import fun.whitea.easychatbackend.service.UserContactApplyService;
import fun.whitea.easychatbackend.service.UserContactService;
import fun.whitea.easychatbackend.utils.CopyUtil;
import fun.whitea.easychatbackend.utils.RedisComponent;
import fun.whitea.easychatbackend.utils.RedisUtil;
import fun.whitea.easychatbackend.utils.StringTool;
import fun.whitea.easychatbackend.websorket.ChannelContextUtil;
import fun.whitea.easychatbackend.websorket.netty.MessageHandle;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class UserContactApplyServiceImpl extends ServiceImpl<UserContactApplyMapper, UserContactApply> implements UserContactApplyService{

    @Resource
    UserContactApplyMapper userContactApplyMapper;
    @Resource
    UserContactMapper userContactMapper;
    @Resource
    RedisComponent redisComponent;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private ChatSessionMapper chatSessionMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private MessageHandle messageHandle;
    @Autowired
    private ChatSessionUserMapper chatSessionUserMapper;
    @Autowired
    private GroupInfoMapper groupInfoMapper;
    @Autowired
    private ChannelContextUtil channelContextUtil;
    @Autowired
    private ChatMessageMapper chatMessageMapper;


    @Override
    public Page<UserContactApply> loadApply(String userId, Integer pageNo) {
        Page<UserContactApply> page = new Page<>(pageNo, Constants.PAGE_SIZE15);
        return (Page<UserContactApply>) userContactApplyMapper.selectUserContactApplyWithContactName(page, userId);
    }

    /**
     * 处理申请
     *
     * @param userId
     * @param applyId
     * @param status
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dealWithApply(String userId, Integer applyId, Integer status) {
        val statusEnum = UserContactApplyStatusEnum.getByStatus(status);
        if (statusEnum == null || statusEnum == UserContactApplyStatusEnum.INIT) {
            throw new BusinessException(ErrorEnum.PARAM_ERROR, "UserContactApplyStatus is error");
        }
        val applyInfo = userContactApplyMapper.selectById(applyId);
        // 校验操作者
        if (applyInfo == null || !userId.equals(applyInfo.getReceiveUserId())) {
            throw new BusinessException(ErrorEnum.PARAM_ERROR, "userId is error");
        }
        // 准备更新数据库
        val updateInfo = new UserContactApply();
        updateInfo.setStatus(statusEnum.getStatus());
        updateInfo.setLastApplyTime(System.currentTimeMillis());

        val update = userContactApplyMapper.update(updateInfo, new LambdaUpdateWrapper<UserContactApply>().eq(UserContactApply::getId, applyId).eq(UserContactApply::getStatus, UserContactApplyStatusEnum.INIT.getStatus()));
        // 检查是否状态由INIT更新
        if (update == 0) {
            throw new BusinessException(ErrorEnum.CONCURRENCY_ERROR, "statusEnum is error");
        }
        // 状态为通过
        if (UserContactApplyStatusEnum.PASS.getStatus().equals(statusEnum.getStatus())) {
            // 添加联系人
            this.addContact(applyInfo.getApplyUserId(), applyInfo.getReceiveUserId(), applyInfo.getContactId(), applyInfo.getContactType(), applyInfo.getApplyInfo());
            return;
        }
        if (UserContactApplyStatusEnum.BLACKLIST.getStatus().equals(statusEnum.getStatus())) {
            Date curDate = new Date();
            UserContact userContact = UserContact.builder()
                    .id(applyInfo.getApplyUserId())
                    .contactId(applyInfo.getContactId())
                    .contactType(applyInfo.getContactType())
                    .createTime(curDate)
                    .status(UserContactStatusEnum.BLACKLIST_BE_FIRST.getStatus())
                    .lastUpdateTime(curDate)
                    .build();
            val i = userContactMapper.updateById(userContact);
            if (i == 0) {
                userContactMapper.insert(userContact);
            }

        }
    }

    /**
     * 添加联系人
     *
     * @param applyUserId
     * @param receiveUserId
     * @param contactId
     * @param contactType
     * @param applyInfo
     */
    @Override
    public void addContact(String applyUserId, String receiveUserId, String contactId, Integer contactType, String applyInfo) {
        // 群聊人数
        if (UserContactTypeEnum.GROUP.getType().equals(contactType)) {
            val count = userContactMapper.selectCount(new LambdaQueryWrapper<UserContact>().eq(UserContact::getContactId, contactId).eq(UserContact::getStatus, UserContactStatusEnum.FRIEND.getStatus()));
            val sysSetting = redisComponent.getSysSetting();
            if (count >= sysSetting.getMaxGroupCount()) {
                throw new BusinessException(ErrorEnum.GROUP_ERROR, "group count exceeds max group count");
            }
        }
        Date curDate = new Date();
        // 同意,双方添加好友
        List<UserContact> contactList = new ArrayList<>();
        // 申请人添加对方
        UserContact userContact = UserContact.builder()
                .id(applyUserId)
                .contactId(contactId)
                .contactType(contactType)
                .createTime(curDate)
                .lastUpdateTime(curDate)
                .status(UserContactStatusEnum.FRIEND.getStatus())
                .build();
        contactList.add(userContact);
        // 如果是申请好友，接收人添加申请人，群组不用添加对方问好友
        if (UserContactTypeEnum.USER.getType().equals(contactType)) {
            userContact = new UserContact();
            userContact = UserContact.builder()
                    .id(receiveUserId)
                    .contactId(applyUserId)
                    .contactType(contactType)
                    .createTime(curDate)
                    .lastUpdateTime(curDate)
                    .status(UserContactStatusEnum.FRIEND.getStatus())
                    .build();
            contactList.add(userContact);
        }
        // 批量插入
        for (UserContact contact : contactList) {
            userContactMapper.insert(contact);
        }

        // 如果是好友，接收人也添加申请人为好友 添加缓存
        if (UserContactTypeEnum.USER.getType().equals(contactType)) {
            redisComponent.addUserContact(receiveUserId,applyUserId);
        }
        redisComponent.addUserContact(applyUserId, contactId);

        // 创建会话
        String sessionId = null;
        if (UserContactTypeEnum.USER.getType().equals(contactType)) {
            sessionId = StringTool.getChatSessionId4User(new String[]{applyUserId, contactId});
        } else {
            sessionId = StringTool.getChatSessionId4Group(contactId);
        }

        List<ChatSessionUser> chatSessionUserList = new ArrayList<>();
        if (UserContactTypeEnum.USER.getType().equals(contactType)) {
            // 创建会话
            ChatSession chatSession = new ChatSession();
            chatSession.setSessionId(sessionId);
            chatSession.setLastMessage(applyInfo);
            chatSession.setLastReceiveTime(curDate.getTime());
            // todo OrUpdate
            chatSessionMapper.insert(chatSession);

            // 申请人session
            ChatSessionUser applySessionUser = new ChatSessionUser();
            applySessionUser.setUserId(applyUserId);
            applySessionUser.setContactId(contactId);
            applySessionUser.setSessionId(sessionId);
            UserInfo contactUser = userInfoMapper.selectById(contactId);
            applySessionUser.setContactName(contactUser.getNickName());
            chatSessionUserList.add(applySessionUser);

            // 接收人session
            ChatSessionUser contactSessionUser = new ChatSessionUser();
            contactSessionUser.setUserId(contactId);
            contactSessionUser.setContactId(applyUserId);
            contactSessionUser.setSessionId(sessionId);
            UserInfo appltUserInfo = userInfoMapper.selectById(applyUserId);
            contactSessionUser.setContactName(appltUserInfo.getNickName());
            chatSessionUserList.add(contactSessionUser);
            // todo OrUpdate
            chatSessionUserList.forEach(chatSessionUser -> chatSessionUserMapper.insert(chatSessionUser));

            // 记录消息表
            ChatMessage chatMessage = ChatMessage.builder()
                    .sessionId(sessionId)
                    .messageType(MessageTypeEnum.ADD_FRIEND.getType())
                    .messageContent(applyInfo)
                    .sendUserId(applyUserId)
                    .sendUserNickName(appltUserInfo.getNickName())
                    .sendTime(curDate.getTime())
                    .contactType(UserContactTypeEnum.USER.getType())
                    .contactId(contactId)
                    .build();
            chatSessionMapper.insert(chatSession);

            MessageSendDto messageSendDto = CopyUtil.copy(chatMessage, MessageSendDto.class);
            // 发送给接受好友申请的人
            messageHandle.sendMessage(messageSendDto);
            // 发送给申请人，发送人就是接受人，联系人就是申请人
            messageSendDto.setMessageType(MessageTypeEnum.ADD_FRIEND_SELF.getType());
            messageSendDto.setContactId(applyUserId);
            messageSendDto.setExtendData(contactUser);
            messageHandle.sendMessage(messageSendDto);
        } else {
            // 加入群组
            GroupInfo groupInfo = groupInfoMapper.selectById(contactId);
            ChatSessionUser chatSessionUser = ChatSessionUser.builder()
                    .userId(applyUserId)
                    .contactId(contactId)
                    .contactName(groupInfo.getGroupName())
                    .sessionId(sessionId)
                    .build();
            chatSessionUserMapper.insert(chatSessionUser);

            UserInfo applyUserInfo = userInfoMapper.selectById(applyUserId);
            String sendMessage = String.format(MessageTypeEnum.ADD_GROUP.getInitMessage(), applyUserInfo.getNickName());

            // 增加session信息
            ChatSession chatSession = new ChatSession();
            chatSession.setSessionId(sessionId);
            chatSession.setLastMessage(sendMessage);
            chatSession.setLastReceiveTime(curDate.getTime());
            // todo OrUpdate
            chatSessionMapper.insert(chatSession);

            // 增加聊天消息
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(sessionId);
            chatMessage.setMessageType(MessageTypeEnum.ADD_GROUP.getType());
            chatMessage.setMessageContent(sendMessage);
            chatMessage.setSendTime(curDate.getTime());
            chatMessage.setContactId(contactId);
            chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
            chatMessage.setStatus(MessageStatusEnum.SENT.getStatus());
            chatMessageMapper.insert(chatMessage);

            // 发送群消息
            MessageSendDto messageSendDto = CopyUtil.copy(chatMessage, MessageSendDto.class);
            messageSendDto.setContactId(contactId);
            messageSendDto.setContactName(groupInfo.getGroupName());

            // 获取群人数
            Integer count = Math.toIntExact(userContactMapper.selectCount(new LambdaQueryWrapper<UserContact>()
                    .eq(UserContact::getContactId, contactId)
                    .eq(UserContact::getStatus, UserContactStatusEnum.FRIEND.getStatus())));
            messageSendDto.setMemberCount(count);

            // 发消息
            messageHandle.sendMessage(messageSendDto);

            // 将群组添加到联系人
            redisComponent.addUserContact(applyUserId, groupInfo.getId());

            // 将联系人通道添加到群组通道
            channelContextUtil.addUser2Group(applyUserId, groupInfo.getId());
        }


    }

    @Override
    public List<UserContact> loadUserContacts(String userId, String contactType) {
//        val typeEnum = UserContactTypeEnum.getByName(contactType);
//        if (typeEnum == null) {
//            throw new BusinessException(ErrorEnum.PARAM_ERROR, "contactType is error");
//        }
//        if (typeEnum == UserContactTypeEnum.USER) {
//            return userContactMapper.selectUserContactsAndUserInfo(userId, typeEnum.getType());
//        } else if (typeEnum == UserContactTypeEnum.GROUP) {
//            return userContactMapper.selectUserContactsAndGroupInfo(userId, typeEnum.getType());
//        }
        return null;
    }

}
