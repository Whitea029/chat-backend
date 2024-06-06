package fun.whitea.easychatbackend.controller;

import fun.whitea.easychatbackend.annotation.GlobalInterceptor;
import fun.whitea.easychatbackend.entity.dto.SysSettingDto;
import fun.whitea.easychatbackend.entity.dto.UserRegisterDto;
import fun.whitea.easychatbackend.entity.vo.UserInfoVo;
import fun.whitea.easychatbackend.service.UserInfoService;
import fun.whitea.easychatbackend.utils.RedisComponent;
import fun.whitea.easychatbackend.utils.RedisUtil;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.script.ScriptException;
import java.util.Map;


@RestController
@RequestMapping("/account")
@Validated
@Slf4j
class AccountController {

    @Resource
    RedisUtil redisUtil;
    @Resource
    UserInfoService userInfoService;
    @Resource
    RedisComponent redisComponent;


    /**
     * get checkCode
     *
     * @return
     * @throws ScriptException
     */
    @GetMapping("/checkCode")
    Map<String, String> checkCode() throws ScriptException {
        return userInfoService.genCode();
    }

    /**
     * register
     *

     * @return
     */
    @PostMapping("/register")
    void register(UserRegisterDto userRegisterDto) {
        userInfoService.register(userRegisterDto.getEmail(), userRegisterDto.getNickName(), userRegisterDto.getPassword(), userRegisterDto.getCheckCodeKey(), userRegisterDto.getCheckCode());
    }

    /**
     * login
     *
     * @param checkCodeKey
     * @param email
     * @param password
     * @param checkCode
     * @return
     */
    @PostMapping("/login")
    UserInfoVo login(@NotEmpty String checkCodeKey,
                            @NotEmpty String email,
                            @NotEmpty String password,
                            @NotEmpty String checkCode
    ) {
        return userInfoService.login(email, password, checkCodeKey, checkCode);
    }

    @GetMapping("/sysSetting")
    @GlobalInterceptor
    SysSettingDto getSysSetting() {
        return redisComponent.getSysSetting();
    }
}
