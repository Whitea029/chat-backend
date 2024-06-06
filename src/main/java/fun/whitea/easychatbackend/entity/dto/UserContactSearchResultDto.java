package fun.whitea.easychatbackend.entity.dto;

import fun.whitea.easychatbackend.entity.enums.UserContactStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserContactSearchResultDto {

    private String contactId;
    private String contactType;
    private String nickName;
    private Integer status;
    private String statusName;
    private Integer sex;
    private String areaName;

    public String getStatusName() {
        UserContactStatusEnum userContactStatusEnum = UserContactStatusEnum.getByStatus(status);
        return userContactStatusEnum == null ? null : userContactStatusEnum.getDesc();
    }

}
