package fun.whitea.easychatbackend.controller;

import fun.whitea.easychatbackend.entity.constants.Constants;
import fun.whitea.easychatbackend.entity.dto.TokenUserInfoDto;
import fun.whitea.easychatbackend.utils.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.val;

import javax.annotation.Resource;

abstract class BaseController {

    @Resource
    RedisUtil redisUtil;

    protected TokenUserInfoDto getTokenUserInfoDto(HttpServletRequest request) {
        val token = request.getHeader("TOKEN");
        return (TokenUserInfoDto) redisUtil.get(Constants.REDIS_KEY_WS_TOKEN + token);
    }
}
