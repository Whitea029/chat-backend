package fun.whitea.easychatbackend.entity.vo;

import fun.whitea.easychatbackend.entity.po.GroupInfo;
import fun.whitea.easychatbackend.entity.po.UserContact;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupInfoVo {

    private GroupInfo groupInfo;
    private List<UserContact> userContactList;
}
