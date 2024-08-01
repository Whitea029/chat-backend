package fun.whitea.easychatbackend.utils;

import fun.whitea.easychatbackend.entity.constants.Constants;
import fun.whitea.easychatbackend.entity.dto.SysSettingDto;
import fun.whitea.easychatbackend.entity.dto.TokenUserInfoDto;
import io.netty.channel.Channel;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component("redisComponent")
public class RedisComponent {

    @Resource
    RedisUtil redisUtil;

    /**
     * 获取心跳
     * @param userId
     * @return
     */
    public Long getUserHeartBeat(String userId) {
        return (Long) redisUtil.get(Constants.REDIS_KEY_WS_USER_HEART_BEAT + userId);
    }

    /**
     * 设置心跳
     */
    public void saveUserHeartBeat(String userId) {
        redisUtil.setnx(Constants.REDIS_KEY_WS_USER_HEART_BEAT + userId, System.currentTimeMillis(), Constants.REDIS_KEY_EXPIRES_HEART_BEAT, TimeUnit.SECONDS);
    }

    public void removeUserHeartBeat(String userId) {
        redisUtil.del(Constants.REDIS_KEY_WS_USER_HEART_BEAT + userId);
    }


    public void saveTokenUserInfo(TokenUserInfoDto tokenUserInfoDto) {
        // token 存 tokenUserInfoDto
        redisUtil.set(Constants.REDIS_KEY_WS_TOKEN + tokenUserInfoDto.getToken(), tokenUserInfoDto, Constants.REDIS_TIME_DAY * 2);
        // userId 存 token
        redisUtil.set(Constants.REDIS_KEY_WS_TOKEN_USERID + tokenUserInfoDto.getUserId(), tokenUserInfoDto.getToken(), Constants.REDIS_TIME_DAY * 2);
    }

    public TokenUserInfoDto getTokenUserInfoDto(String token) {
        return (TokenUserInfoDto) redisUtil.get(Constants.REDIS_KEY_WS_TOKEN + token);
    }

    public SysSettingDto getSysSetting() {
        SysSettingDto sysSettingDto = (SysSettingDto) redisUtil.get(Constants.REDIS_KEY_SYS_SETTING);
        sysSettingDto = sysSettingDto == null ? new SysSettingDto() : sysSettingDto;
        return sysSettingDto;
    }

    // 清空联系人
    public void cleanUserContact(String userId) {
        redisUtil.del(Constants.REDIS_KEY_USER_CONTACT + userId);
    }

    // 批量添加联系人
    public void addUserContactBatch(String userId, List<String> contactIds) {
        redisUtil.lSet(Constants.REDIS_KEY_USER_CONTACT + userId, contactIds, Constants.REDIS_TIME_DAY * 2);
    }

    public List<String> getUserContactIds(String userId) {
        return (List<String>) redisUtil.get(Constants.REDIS_KEY_USER_CONTACT + userId);
    }

}
