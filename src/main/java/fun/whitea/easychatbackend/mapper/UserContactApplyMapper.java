package fun.whitea.easychatbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import fun.whitea.easychatbackend.entity.po.UserContactApply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserContactApplyMapper extends BaseMapper<UserContactApply> {

//    default Page<UserContactApply> selectUserContactApplyPage(Page<UserContactApply> page, String userId) {
//        QueryWrapper<UserContactApply> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("receive_user_id", userId)
//                .orderByDesc("last_apply_time")
//                .apply("left join user_info on user_info.id = user_contact_apply.apply_user_id")
//                .apply("left join group_info on group_info.id = user_contact_apply.contact_id")
//                .select("user_contact_apply.*",
//                        "case when user_contact_apply.contact_type = 0 then u.nick_name " +
//                                "when user_contact_apply.contact_type = 1 then g.group_name end as contactName");
//        return selectPage(page, queryWrapper);
//    }

    IPage<UserContactApply> selectUserContactApplyWithContactName(Page<UserContactApply> page, @Param("receiveUserId") String receiveUserId);



}
