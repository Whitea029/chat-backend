package fun.whitea.easychatbackend.websorket;

import fun.whitea.easychatbackend.utils.RedisComponent;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.catalina.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChannelContextUtil {

    private static final Logger logger = LoggerFactory.getLogger(ChannelContextUtil.class);

    private static final ConcurrentHashMap<String, Channel> USER_CONTEXT_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ChannelGroup> GROUP_CONTEXT_MAP = new ConcurrentHashMap<>();

    @Resource
    RedisComponent redisComponent;

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

        USER_CONTEXT_MAP.put(userId, channel);
        redisComponent.saveUserHeartBeat(userId);



        String groupId = "1000000";
        add2Group(groupId, channel);
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

    public void send2Group(String message) {
        ChannelGroup group = GROUP_CONTEXT_MAP.get("1000000");
        group.writeAndFlush(new TextWebSocketFrame(message));

    }
}
