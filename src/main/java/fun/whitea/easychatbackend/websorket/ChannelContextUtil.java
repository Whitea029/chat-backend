package fun.whitea.easychatbackend.websorket;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import fun.whitea.easychatbackend.entity.constants.Constants;
import fun.whitea.easychatbackend.entity.dto.MessageSendDto;
import fun.whitea.easychatbackend.entity.dto.WsInitData;
import fun.whitea.easychatbackend.entity.enums.MessageTypeEnum;
import fun.whitea.easychatbackend.entity.enums.UserContactApplyStatusEnum;
import fun.whitea.easychatbackend.entity.enums.UserContactTypeEnum;
import fun.whitea.easychatbackend.entity.po.*;
import fun.whitea.easychatbackend.mapper.*;
import fun.whitea.easychatbackend.utils.JsonUtils;
import fun.whitea.easychatbackend.utils.RedisComponent;
import fun.whitea.easychatbackend.utils.StringTool;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChannelContextUtil {

    private static final Logger logger = LoggerFactory.getLogger(ChannelContextUtil.class);

    private static final ConcurrentHashMap<String, Channel> USER_CONTEXT_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ChannelGroup> GROUP_CONTEXT_MAP = new ConcurrentHashMap<>();

    @Resource
    RedisComponent redisComponent;
    @Resource
    UserInfoMapper userInfoMapper;
    @Resource
    ChatSessionMapper chatSessionMapper;
    @Resource
    ChatMessageMapper chatMessageMapper;
    @Resource
    ChatSessionUserMapper chatSessionUserMapper;
    @Resource
    UserContactApplyMapper userContactApplyMapper;

    public void addContext(String userId, Channel channel) {
        String channelId = channel.id().toString();
        logger.info("channelId: {}", channelId);
        AttributeKey attributeKey = null;
        if (!AttributeKey.exists(channelId)) {
            attributeKey = AttributeKey.newInstance(channelId);
        } else {
            attributeKey = AttributeKey.valueOf(channelId);
        }
        channel.attr(attributeKey).set(userId);

        List<String> userContactIds = redisComponent.getUserContactIds(userId);
        userContactIds.forEach(contactId -> {
            if (contactId.startsWith(UserContactTypeEnum.GROUP.getPrefix())) {
                add2Group(contactId, channel);
            }
        });
        USER_CONTEXT_MAP.put(userId, channel);
        redisComponent.saveUserHeartBeat(userId);

        // 更新用户最后连接时间
        userInfoMapper.update(new UpdateWrapper<UserInfo>()
                .eq("id", userId)
                .set("last_login_time", new Date()));

        // 给用户发消息
        UserInfo userInfo = userInfoMapper.selectById(userId);
        Long sourceLastOfTime = userInfo.getLastOffTime();
        Long lastOffTime = sourceLastOfTime;
        if (sourceLastOfTime != null && System.currentTimeMillis() - sourceLastOfTime > Constants.MILLIS_SECONDS_3_DAYS_AGO) {
            sourceLastOfTime = Constants.MILLIS_SECONDS_3_DAYS_AGO;
        }

        // 查询会话信息
        List<ChatSessionUser> chatSessionUsers = chatSessionUserMapper.selectListByUserId(userId);
        WsInitData wsInitData = new WsInitData();
        wsInitData.setChatSessionList(chatSessionUsers);

        // 查询聊天消息
        // 查询所有的联系人
        List<String> groupIds = userContactIds.stream().filter(e -> e.startsWith(UserContactTypeEnum.GROUP.getPrefix())).toList();
        groupIds.add(userId);
        List<ChatMessage> chatMessageList = chatMessageMapper.selectBatchContactIds(groupIds, lastOffTime);
        wsInitData.setChatMessageList(chatMessageList);

        // 查询好友申请
        Integer applyCount = Math.toIntExact(userContactApplyMapper.selectCount(
                new QueryWrapper<UserContactApply>()
                        .eq("receive_user_id", userId)
                        .eq("status", UserContactApplyStatusEnum.INIT.getStatus())
                        .gt("last_apply_time", lastOffTime)));
        wsInitData.setApplyCount(applyCount);

        // 发送消息
        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setMessageType(MessageTypeEnum.INIT.getType());
        messageSendDto.setContactId(userId);
        messageSendDto.setExtendData(wsInitData);
        sendMsg(messageSendDto,userId);
    }

    /**
     * 发送消息
     */
    public void sendMsg(MessageSendDto messageSendDto, String receiveId) {
        if (receiveId == null) {
            return;
        }
        Channel sendChannel = USER_CONTEXT_MAP.get(receiveId);
        if (sendChannel == null) {
            return;
        }

        // 相对于客户端而言，联系人就是发送人，所以这里转换一下在发送
        messageSendDto.setContactId(messageSendDto.getSendUserId());
        messageSendDto.setContactName(messageSendDto.getSendUserNickName());
        sendChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.convertObj2Json(messageSendDto)));

    }

    private void add2Group(String userId, Channel channel) {
        ChannelGroup channelGroup = GROUP_CONTEXT_MAP.get(userId);
        if (channelGroup == null) {
            channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            GROUP_CONTEXT_MAP.put(userId, channelGroup);
        }
        if (channel == null) {
            return;
        }
        channelGroup.add(channel);
    }

    public void removeContext(Channel channel) {
        String userId = (String) channel.attr(AttributeKey.valueOf(channel.id().toString())).get();
        if (StringUtils.isEmpty(userId)) {
            USER_CONTEXT_MAP.remove(userId);
        }
        redisComponent.removeUserHeartBeat(userId);
        // 更新用户最后离线时间
        userInfoMapper.update(new UpdateWrapper<UserInfo>()
                .eq("id", userId)
                .set("last_off_time", System.currentTimeMillis()));

    }

    public void sendMessage(MessageSendDto messageSendDto) {
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByPrefix(messageSendDto.getContactId());
        switch (contactTypeEnum) {
            case USER:
                send2User(messageSendDto);
                break;
            case GROUP:
                send2Group(messageSendDto);
                break;
        }
    }

    private void send2User(MessageSendDto messageSendDto) {
        String contactId = messageSendDto.getContactId();
        if (StringTool.isEmpty(contactId)){
            return;
        }
        sendMsg(messageSendDto, contactId);
        // 强制下线
        if (messageSendDto.getMessageType().equals(MessageTypeEnum.FORCE_OFF_LINE.getType())) {
            // 关闭通道
            closeContext(contactId);
        }
    }

    public void closeContext(String userId) {
        if (StringTool.isEmpty(userId)) {
            return;
        }
        redisComponent.cleanUserTokenByUserId(userId);
        Channel channel = USER_CONTEXT_MAP.get(userId);
        if (channel == null) {
            return;
        }
        channel.close();
    }

    private void send2Group(MessageSendDto messageSendDto) {
        if (StringTool.isEmpty(messageSendDto.getContactId())) {
            return;
        }
        ChannelGroup channelGroup = GROUP_CONTEXT_MAP.get(messageSendDto.getContactId());
        if (channelGroup == null) {
            return;
        }
        channelGroup.writeAndFlush(new TextWebSocketFrame(JsonUtils.convertObj2Json(messageSendDto)));
    }
}
