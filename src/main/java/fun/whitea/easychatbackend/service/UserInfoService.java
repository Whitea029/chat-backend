package fun.whitea.easychatbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import fun.whitea.easychatbackend.entity.po.UserInfo;
import fun.whitea.easychatbackend.entity.vo.UserInfoVo;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

public interface UserInfoService {

    /**
     * getUserInfo
     * @param pageNo
     * @param pageSize
     * @return
     */
    Page<UserInfo> getUserInfo(Integer pageNo, Integer pageSize);

    /**
     * get checkCode
     * @return
     */
    Map<String, String> genCode();

    /**
     * register
     *
     * @param email
     * @param nickName
     * @param password
     * @param checkCodeKey
     * @param checkCode
     */
    void register(String email, String nickName, String password, String checkCodeKey, String checkCode);

    /**
     * login
     *
     * @param email
     * @param password
     * @param checkCodeKey
     * @param checkCode
     * @return
     */
    UserInfoVo login(String email, String password, @NotEmpty String checkCodeKey, @NotEmpty String checkCode);
}
