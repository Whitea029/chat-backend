package fun.whitea.easychatbackend.entity.po;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoBeauty {
    private Integer id;
    private String email;
    private String userId;
    private Integer status;
}
