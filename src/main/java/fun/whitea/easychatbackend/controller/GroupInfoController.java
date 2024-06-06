package fun.whitea.easychatbackend.controller;


import fun.whitea.easychatbackend.entity.po.GroupInfo;
import fun.whitea.easychatbackend.entity.po.UserContact;
import fun.whitea.easychatbackend.entity.vo.GroupInfoVo;
import fun.whitea.easychatbackend.service.GroupInfoService;
import fun.whitea.easychatbackend.service.UserContactService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.val;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/group")
@Validated
class GroupInfoController extends BaseController {

    @Resource
    GroupInfoService groupInfoService;
    @Resource
    UserContactService userContactService;


    /**
     * 新增群聊
     * @param request
     * @param id
     * @param groupName
     * @param groupNotice
     * @param joinType
     * @param avatarFile
     * @param avatarCover
     */
    @PostMapping()
    void saveGroup(HttpServletRequest request,
                          String id,
                          @NotEmpty String groupName,
                          String groupNotice,
                          @NotNull Integer joinType,
                          MultipartFile avatarFile,
                          MultipartFile avatarCover) {
        val userId = getTokenUserInfoDto(request).getUserId();
        groupInfoService.saveGroup(userId, id, groupName, groupNotice, joinType, avatarFile, avatarCover);
    }

    /**
     * 获取群聊列表
     * @param request
     * @return
     */
    @GetMapping()
    List<GroupInfo> getGroups(HttpServletRequest request) {
        val userId = getTokenUserInfoDto(request).getUserId();
        return groupInfoService.getGroups(userId);
    }

    /**
     * 获取群聊详情
     * @param request
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    GroupInfo getGroup(HttpServletRequest request, @PathVariable String id) {
        val userId = getTokenUserInfoDto(request).getUserId();
        return groupInfoService.getGroup(userId, id);
    }

    /**
     * 获取聊天会话详情
     * @param request
     * @param id
     * @return
     */
    @GetMapping("/chat/{id}")
    GroupInfoVo getGroup4Chat(HttpServletRequest request, @PathVariable String id) {
        val groupInfoVo = new GroupInfoVo();
        val userId = getTokenUserInfoDto(request).getUserId();
        val groupInfo = groupInfoService.getGroup(userId, id);
        List<UserContact> userContactList = userContactService.getUserContactList(id);
        groupInfoVo.setUserContactList(userContactList);
        groupInfoVo.setGroupInfo(groupInfo);
        return groupInfoVo;
    }
}
