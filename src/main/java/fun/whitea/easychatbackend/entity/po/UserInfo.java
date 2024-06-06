package fun.whitea.easychatbackend.entity.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Date;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfo {
    private String id;
    private String email;
    private String nickName;
    private Integer joinType;
    private Integer sex;
    private String password;
    private String personalSignature;
    private Integer status;
    private Date createTime;
    private Date lastLoginTime;
    private String areaName;
    private String areaCode;
    private Long lastOffTime;
}
