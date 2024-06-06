package fun.whitea.easychatbackend.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fun.whitea.easychatbackend.entity.constants.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysSettingDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -216378364921648963L;

    private Integer maxGroupCount = 5;
    private Integer maxGroupMemberCount = 500;
    private Integer maxImageSize = 2;
    private Integer maxVideoSize = 5;
    private Integer maxFileSize = 5;
    private String robotUid = Constants.ROBOT_UID;
    private String robotNickName = "Chat";
    private String robotWelcome = "欢迎使用Chat";


}
