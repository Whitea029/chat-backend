package fun.whitea.easychatbackend.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfoVo {
    private String id;
    private Integer sex;
    private String personalSignature;
    private String areaCode;
    private String areaName;
    private String token;
    private Boolean admin;
    private Integer contactStatus;

}
