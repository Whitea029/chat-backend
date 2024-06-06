package fun.whitea.easychatbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.whitea.easychatbackend.entity.constants.Constants;
import fun.whitea.easychatbackend.entity.enums.ErrorEnum;
import fun.whitea.easychatbackend.entity.enums.UserContactApplyStatusEnum;
import fun.whitea.easychatbackend.entity.enums.UserContactStatusEnum;
import fun.whitea.easychatbackend.entity.enums.UserContactTypeEnum;
import fun.whitea.easychatbackend.entity.po.UserContact;
import fun.whitea.easychatbackend.entity.po.UserContactApply;
import fun.whitea.easychatbackend.exception.BusinessException;
import fun.whitea.easychatbackend.mapper.UserContactApplyMapper;
import fun.whitea.easychatbackend.mapper.UserContactMapper;
import fun.whitea.easychatbackend.service.UserContactApplyService;
import fun.whitea.easychatbackend.service.UserContactService;
import fun.whitea.easychatbackend.utils.RedisComponent;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class UserContactApplyServiceImpl implements UserContactApplyService {

    @Resource
    UserContactApplyMapper userContactApplyMapper;
    @Resource
    UserContactMapper userContactMapper;
    @Resource
    RedisComponent redisComponent;


    @Override
    public Page<UserContactApply> loadApply(String userId, Integer pageNo) {
        Page<UserContactApply> page = new Page<>(pageNo, Constants.PAGE_SIZE15);
        return (Page<UserContactApply>) userContactApplyMapper.selectUserContactApplyWithContactName(page, userId);
    }

    /**
     * 处理申请
     *
     * @param userId
     * @param applyId
     * @param status
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dealWithApply(String userId, Integer applyId, Integer status) {
        val statusEnum = UserContactApplyStatusEnum.getByStatus(status);
        if (statusEnum == null || statusEnum == UserContactApplyStatusEnum.INIT) {
            throw new BusinessException(ErrorEnum.PARAM_ERROR, "UserContactApplyStatus is error");
        }
        val applyInfo = userContactApplyMapper.selectById(applyId);
        // 校验操作者
        if (applyInfo == null || !userId.equals(applyInfo.getReceiveUserId())) {
            throw new BusinessException(ErrorEnum.PARAM_ERROR, "userId is error");
        }
        // 准备更新数据库
        val updateInfo = new UserContactApply();
        updateInfo.setStatus(statusEnum.getStatus());
        updateInfo.setLastApplyTime(System.currentTimeMillis());

        val update = userContactApplyMapper.update(updateInfo, new LambdaUpdateWrapper<UserContactApply>().eq(UserContactApply::getId, applyId).eq(UserContactApply::getStatus, UserContactApplyStatusEnum.INIT.getStatus()));
        // 检查是否状态由INIT更新
        if (update == 0) {
            throw new BusinessException(ErrorEnum.CONCURRENCY_ERROR, "statusEnum is error");
        }
        // 状态为通过
        if (UserContactApplyStatusEnum.PASS.getStatus().equals(statusEnum.getStatus())) {
            // 添加联系人
            this.addContact(applyInfo.getApplyUserId(), applyInfo.getReceiveUserId(), applyInfo.getContactId(), applyInfo.getContactType(), applyInfo.getApplyInfo());
            return;
        }
        if (UserContactApplyStatusEnum.BLACKLIST.getStatus().equals(statusEnum.getStatus())) {
            Date curDate = new Date();
            UserContact userContact = UserContact.builder()
                    .id(applyInfo.getApplyUserId())
                    .contactId(applyInfo.getContactId())
                    .contactType(applyInfo.getContactType())
                    .createTime(curDate)
                    .status(UserContactStatusEnum.BLACKLIST_BE_FIRST.getStatus())
                    .lastUpdateTime(curDate)
                    .build();
            val i = userContactMapper.updateById(userContact);
            if (i == 0) {
                userContactMapper.insert(userContact);
            }

        }
    }

    /**
     * 添加联系人
     *
     * @param applyUserId
     * @param receiveUserId
     * @param contactId
     * @param contactType
     * @param applyInfo
     */
    @Override
    public void addContact(String applyUserId, String receiveUserId, String contactId, Integer contactType, String applyInfo) {
        // 群聊人数
        if (UserContactTypeEnum.GROUP.getType().equals(contactType)) {
            val count = userContactMapper.selectCount(new LambdaQueryWrapper<UserContact>().eq(UserContact::getContactId, contactId).eq(UserContact::getStatus, UserContactStatusEnum.FRIEND.getStatus()));
            val sysSetting = redisComponent.getSysSetting();
            if (count >= sysSetting.getMaxGroupCount()) {
                throw new BusinessException(ErrorEnum.GROUP_ERROR, "group count exceeds max group count");
            }
        }
        Date curDate = new Date();
        // 同意,双方添加好友
        List<UserContact> contactList = new ArrayList<>();
        // 申请人添加对方
        UserContact userContact = UserContact.builder()
                .id(applyUserId)
                .contactId(contactId)
                .contactType(contactType)
                .createTime(curDate)
                .lastUpdateTime(curDate)
                .status(UserContactStatusEnum.FRIEND.getStatus())
                .build();
        contactList.add(userContact);
        // 如果是申请好友，接收人添加申请人，群组不用添加对方问好友
        if (UserContactTypeEnum.USER.getType().equals(contactType)) {
            userContact = new UserContact();
            userContact = UserContact.builder()
                    .id(receiveUserId)
                    .contactId(applyUserId)
                    .contactType(contactType)
                    .createTime(curDate)
                    .lastUpdateTime(curDate)
                    .status(UserContactStatusEnum.FRIEND.getStatus())
                    .build();
            contactList.add(userContact);
        }
        // 批量插入
        for (UserContact contact : contactList) {
            userContactMapper.insert(contact);
        }
        // TODO 如果是好友，接收人也添加申请人为好友 添加缓存

        // TODO 创建会话


    }

    @Override
    public List<UserContact> loadUserContacts(String userId, String contactType) {
//        val typeEnum = UserContactTypeEnum.getByName(contactType);
//        if (typeEnum == null) {
//            throw new BusinessException(ErrorEnum.PARAM_ERROR, "contactType is error");
//        }
//        if (typeEnum == UserContactTypeEnum.USER) {
//            return userContactMapper.selectUserContactsAndUserInfo(userId, typeEnum.getType());
//        } else if (typeEnum == UserContactTypeEnum.GROUP) {
//            return userContactMapper.selectUserContactsAndGroupInfo(userId, typeEnum.getType());
//        }
        return null;
    }

}
