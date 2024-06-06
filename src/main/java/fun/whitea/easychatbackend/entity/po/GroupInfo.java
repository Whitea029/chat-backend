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
public class GroupInfo {

    private String id;
    private String groupName;
    private String groupOwnerId;
    private Date createTime;
    private String groupNotice;
    private Integer joinType;
    private Integer status;
    @TableField(exist = false)
    public Long memberCount;

}
