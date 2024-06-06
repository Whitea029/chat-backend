package fun.whitea.easychatbackend.utils;

import fun.whitea.easychatbackend.entity.constants.Constants;
import fun.whitea.easychatbackend.entity.dto.SysSettingDto;
import fun.whitea.easychatbackend.entity.dto.TokenUserInfoDto;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

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

    public void saveTokenUserInfo(TokenUserInfoDto tokenUserInfoDto) {
        redisUtil.set(Constants.REDIS_KEY_WS_TOKEN + tokenUserInfoDto.getToken(), tokenUserInfoDto, Constants.REDIS_TIME_DAY * 2);
        redisUtil.set(Constants.REDIS_KEY_WS_TOKEN_USERID + tokenUserInfoDto.getToken(), tokenUserInfoDto.getToken(), Constants.REDIS_TIME_DAY * 2);
    }

    public SysSettingDto getSysSetting() {
        SysSettingDto sysSettingDto = (SysSettingDto) redisUtil.get(Constants.REDIS_KEY_SYS_SETTING);
        sysSettingDto = sysSettingDto == null ? new SysSettingDto() : sysSettingDto;
        return sysSettingDto;
    }

}
