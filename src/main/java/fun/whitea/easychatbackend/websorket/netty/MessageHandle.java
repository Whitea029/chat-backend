package fun.whitea.easychatbackend.websorket.netty;

import com.alibaba.druid.support.json.JSONUtils;
import fun.whitea.easychatbackend.entity.dto.MessageSendDto;
import fun.whitea.easychatbackend.utils.JsonUtils;
import fun.whitea.easychatbackend.websorket.ChannelContextUtil;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component
public class MessageHandle {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandle.class);

    private static final String MESSAGE_TOPIC = "message.topic";

    @Resource
    RedissonClient redissonClient;

    @Resource
    ChannelContextUtil channelContextUtil;

    @PostConstruct
    public void lisMessage() {
        RTopic rTopic = redissonClient.getTopic(MESSAGE_TOPIC);
        rTopic.addListener(MessageSendDto.class, (MessageSendDto,sendDto) -> {
            logger.info("收到广播消息{}", JsonUtils.convertObj2Json(sendDto));
            channelContextUtil.sendMessage(sendDto);
        });
    }

    public void sendMessage(MessageSendDto messageSendDto) {
        RTopic rTopic = redissonClient.getTopic(MESSAGE_TOPIC);
        rTopic.publish(messageSendDto);
    }

}
