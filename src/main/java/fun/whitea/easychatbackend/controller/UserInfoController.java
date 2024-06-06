package fun.whitea.easychatbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import fun.whitea.easychatbackend.entity.po.UserInfo;
import fun.whitea.easychatbackend.service.UserInfoService;
import lombok.val;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/user")
public class UserInfoController{

    @Resource
    UserInfoService userInfoService;

    @GetMapping()
    public Page<UserInfo> getUserInfo(@RequestParam(required = false, defaultValue = "1") Integer pageNo,
                                      @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        return userInfoService.getUserInfo(pageNo, pageSize);
    }
}
