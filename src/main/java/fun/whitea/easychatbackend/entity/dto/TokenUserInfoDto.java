package fun.whitea.easychatbackend.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenUserInfoDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -2183791264786213978L;

    private String token;
    private String userId;
    private String nickName;
    private Boolean admin;
}
