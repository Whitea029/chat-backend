package fun.whitea.easychatbackend.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterDto {
    private String checkCodeKey;
    private String email;
    private String password;
    private String nickName;
    private String checkCode;
}
