package fun.whitea.easychatbackend.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserContact {

    private String id;
    private String contactId;
    private Integer contactType;
    private Date createTime;
    private Integer status;
    private Date lastUpdateTime;
    @TableField(exist = false)
    private String contactName;
    @TableField(exist = false)
    private String sex;

}
