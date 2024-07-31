package fun.whitea.easychatbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import fun.whitea.easychatbackend.entity.constants.Constants;
import fun.whitea.easychatbackend.entity.po.UserInfo;
import fun.whitea.easychatbackend.entity.vo.UserInfoVo;
import fun.whitea.easychatbackend.service.UserInfoService;
import fun.whitea.easychatbackend.utils.CopyUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.val;
import org.apache.catalina.User;
import org.springframework.data.repository.cdi.Eager;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping("/user")
public class UserInfoController {

    @Resource
    UserInfoService userInfoService;

    @GetMapping
    UserInfoVo getUserInfo(HttpServletRequest request) {
        String userId = getUserInfo(request).getId();
        UserInfo userInfo = userInfoService.getUserInfoByUserId(userId);
        UserInfoVo userInfoVo = CopyUtil.copy(userInfo, UserInfoVo.class);
        userInfoVo.setAdmin(getUserInfo(request).getAdmin());
        return userInfoVo;
    }

    @PostMapping
    UserInfoVo saveUserInfo(HttpServletRequest request,
                            UserInfo userInfo,
                            MultipartFile avatarFile,
                            MultipartFile avatarCover) {
        String userId = getUserInfo(request).getId();
        userInfo.setId(userId);
        userInfoService.updateUserInfo(userInfo, avatarCover, avatarFile);
        return getUserInfo(request);
    }

    @PutMapping("/password")
    void updatePassword(HttpServletRequest request,
                        @NotEmpty @Pattern(regexp = Constants.REGEX_PASSWORD) String password) {
        String userId = getUserInfo(request).getId();
        userInfoService.updatePassword(userId, password);
        // todo 强制退出, 重新登录
    }

    @GetMapping("logout")
    void logout(HttpServletRequest request) {
        String userId = getUserInfo(request).getId();
        // todo 退出登录，关闭ws链接
    }
}
