package fun.whitea.easychatbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import fun.whitea.easychatbackend.entity.po.UserContact;
import fun.whitea.easychatbackend.entity.po.UserContactApply;

import java.util.List;

public interface UserContactApplyService extends IService<UserContactApply> {
    Page<UserContactApply> loadApply(String userId, Integer pageNo);

    void dealWithApply(String userId, Integer applyId, Integer status);

    /**
     * 添加联系人
     * @param applyUserId
     * @param receiveUserId
     * @param contactId
     * @param contactType
     * @param applyInfo
     */
    void addContact(String applyUserId, String receiveUserId, String contactId, Integer contactType, String applyInfo);

    List<UserContact> loadUserContacts(String userId, String contactType);

}
