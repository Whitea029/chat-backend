package fun.whitea.easychatbackend.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import fun.whitea.easychatbackend.entity.dto.UserContactSearchResultDto;
import fun.whitea.easychatbackend.entity.po.UserContact;
import fun.whitea.easychatbackend.entity.po.UserContactApply;
import fun.whitea.easychatbackend.entity.po.UserInfo;
import fun.whitea.easychatbackend.mapper.UserContactMapper;
import fun.whitea.easychatbackend.service.UserContactApplyService;
import fun.whitea.easychatbackend.service.UserContactService;
import fun.whitea.easychatbackend.service.UserInfoService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@RestController
@RequestMapping("/contact")
class UserContactController extends BaseController {

    @Resource
    UserContactService userContactService;
    @Resource
    UserInfoService userInfoService;
    @Resource
    UserContactApplyService userContactApplyService;


    /**
     * 搜索好友或群聊
     *
     * @param contactId
     * @param request
     * @return
     */
    @GetMapping("/search")
    UserContactSearchResultDto search(@RequestParam @NotEmpty String contactId, HttpServletRequest request) {
        val userId = getTokenUserInfoDto(request).getUserId();
        return userContactService.search(userId, contactId);
    }

    /**
     * 申请好友或入群申请
     *
     * @param contactId
     * @param applyInfo
     * @param request
     * @return
     */
    @PostMapping("/apply")
    Integer apply(@RequestParam @NotEmpty String contactId,
                  @RequestParam(required = false) String applyInfo,
                  HttpServletRequest request) {
        val tokenUserInfoDto = getTokenUserInfoDto(request);
        return userContactService.applyAdd(tokenUserInfoDto, contactId, applyInfo);
    }

    /**
     * 获取好友申请列表
     *
     * @param request
     * @param pageNo
     * @return
     */
    @GetMapping("/apply")
    Page<UserContactApply> loadApply(HttpServletRequest request, Integer pageNo) {
        val userId = getTokenUserInfoDto(request).getUserId();
        return userContactApplyService.loadApply(userId, pageNo);
    }

    /**
     * 处理联系人申请
     *
     * @param request
     * @param applyId
     * @param status
     */
    @PutMapping
    void dealWithApply(HttpServletRequest request, @NotNull Integer applyId, @NotNull Integer status) {
        val userId = getTokenUserInfoDto(request).getUserId();
        userContactApplyService.dealWithApply(userId, applyId, status);
    }

    /**
     * 获取联系人列表
     *
     * @param request
     * @param contactType
     * @return
     */
    @GetMapping
    List<UserContact> loadUserContacts(HttpServletRequest request, @NotNull String contactType) {
        val userId = getTokenUserInfoDto(request).getUserId();
        return userContactApplyService.loadUserContacts(userId, contactType);
    }

//    @GetMapping("/detail")
//    UserContact getUserContactInfo(HttpServletRequest request, @NotNull String contactId) {
//        val userId = getTokenUserInfoDto(request).getUserId();
//        userContactService.getUserContaactInfo(userId, contactId);
//    }


}
