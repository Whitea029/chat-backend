package fun.whitea.easychatbackend.websorket;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import fun.whitea.easychatbackend.entity.constants.Constants;
import fun.whitea.easychatbackend.entity.dto.MessageSendDto;
import fun.whitea.easychatbackend.entity.dto.WsInitData;
import fun.whitea.easychatbackend.entity.enums.MessageTypeEnum;
import fun.whitea.easychatbackend.entity.enums.UserContactTypeEnum;
import fun.whitea.easychatbackend.entity.po.ChatSessionUser;
import fun.whitea.easychatbackend.entity.po.UserInfo;
import fun.whitea.easychatbackend.mapper.ChatSessionMapper;
import fun.whitea.easychatbackend.mapper.ChatSessionUserMapper;
import fun.whitea.easychatbackend.mapper.UserInfoMapper;
import fun.whitea.easychatbackend.utils.JsonUtils;
import fun.whitea.easychatbackend.utils.RedisComponent;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.catalina.User;
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
    ChatSessionUserMapper chatSessionUserMapper;

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
        wsInitData.setChatSessionUserList(chatSessionUsers);

        // 查询聊天消息

        // 查询好友申请

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
    public static void sendMsg(MessageSendDto messageSendDto, String receiveId) {
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


}
