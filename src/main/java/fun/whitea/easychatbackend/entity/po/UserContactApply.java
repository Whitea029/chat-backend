package fun.whitea.easychatbackend.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import fun.whitea.easychatbackend.entity.enums.UserContactApplyStatusEnum;
import lombok.*;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserContactApply {

    private Integer id;
    private String applyUserId;
    private String receiveUserId;
    private Integer contactType;
    private String contactId;
    private Long lastApplyTime;
    private Integer status;
    private String applyInfo;
    @TableField(exist = false)
    private String contactName;
    @TableField(exist = false)
    private String statusName;

    public String getStatusName() {
        val statusEnum = UserContactApplyStatusEnum.getByStatus(status);
        return statusEnum == null ? null : statusEnum.name();
    }

}
