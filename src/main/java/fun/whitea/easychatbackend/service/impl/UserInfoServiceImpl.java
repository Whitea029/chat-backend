package fun.whitea.easychatbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wf.captcha.ArithmeticCaptcha;
import fun.whitea.easychatbackend.config.AppConfig;
import fun.whitea.easychatbackend.entity.constants.Constants;
import fun.whitea.easychatbackend.entity.dto.TokenUserInfoDto;
import fun.whitea.easychatbackend.entity.enums.*;
import fun.whitea.easychatbackend.entity.po.UserInfo;
import fun.whitea.easychatbackend.entity.po.UserInfoBeauty;
import fun.whitea.easychatbackend.entity.vo.UserInfoVo;
import fun.whitea.easychatbackend.exception.BusinessException;
import fun.whitea.easychatbackend.mapper.UserInfoBeautyMapper;
import fun.whitea.easychatbackend.mapper.UserInfoMapper;
import fun.whitea.easychatbackend.service.UserInfoService;
import fun.whitea.easychatbackend.utils.CopyUtil;
import fun.whitea.easychatbackend.utils.RedisComponent;
import fun.whitea.easychatbackend.utils.RedisUtil;
import fun.whitea.easychatbackend.utils.StringTool;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;

@Service
@Slf4j
public class UserInfoServiceImpl implements UserInfoService {

    @Resource
    UserInfoMapper userInfoMapper;
    @Resource
    UserInfoBeautyMapper userInfoBeautyMapper;
    @Resource
    RedisUtil redisUtil;
    @Resource
    AppConfig appConfig;
    @Resource
    private RedisComponent redisComponent;

    @Override
    public Page<UserInfo> getUserInfo(Integer pageNo, Integer pageSize) {
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        Page<UserInfo> page = new Page<>(pageNo, pageSize);
        userInfoMapper.selectPage(page, queryWrapper);
        return page;
    }

    @Override
    public Map<String, String> genCode() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);
        String code = captcha.text();
        log.info("code is {}", code);
        String checkCodeKey = UUID.randomUUID().toString();
        redisUtil.set(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey, code, Constants.REDIS_TIME_1MIN * 10);
        val base64 = captcha.toBase64();
        Map<String, String> result = new HashMap<>();
        result.put("checkCode", base64);
        result.put("checkCodeKey", checkCodeKey);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(String email, String nickName, @Pattern(regexp = Constants.REGEX_PASSWORD)  String password, String checkCodeKey, String checkCode) {
        try {
            if (!checkCode.equals(redisUtil.get(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey))) {
                throw new BusinessException(ErrorEnum.PARAM_ERROR, "check code key error");
            }
        } finally {
            redisUtil.del(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
        }
        val userInfo = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getEmail, email));
        if (userInfo != null) {
            throw new BusinessException(ErrorEnum.EMAIL_ERROR, "email already exist");
        }
        String userId = StringTool.getUserId();
        val beautyAccount = userInfoBeautyMapper.selectOne(new LambdaQueryWrapper<UserInfoBeauty>().eq(UserInfoBeauty::getEmail, email));
        Boolean useBeautyAccount = beautyAccount != null && BeautyAccountStatusEnum.NO_USE.getStatus().equals(beautyAccount.getStatus());
        if (useBeautyAccount) {
            userId = UserContactTypeEnum.USER.getPrefix() + beautyAccount.getUserId();
        }
        val curDate = new Date();
        UserInfo userInfoRegister = UserInfo.builder()
                .id(userId)
                .nickName(nickName)
                .email(email)
                .password(StringTool.encodeMd5(password))
                .status(UserStatusEnum.ENABLE.getStatus())
                .createTime(curDate)
                .joinType(JoinTypeEnum.APPLY.getType())
                .lastOffTime(curDate.getTime())
                .build();
        userInfoMapper.insert(userInfoRegister);
        if (useBeautyAccount) {
            UserInfoBeauty userInfoBeauty = new UserInfoBeauty();
            userInfoBeauty.setStatus(BeautyAccountStatusEnum.USED.getStatus());
            userInfoBeautyMapper.insert(userInfoBeauty);
        }
        // TODO 创建机器人好友
    }

    @Override
    public UserInfoVo login(String email, String password, @NotEmpty String checkCodeKey, @NotEmpty String checkCode) {
        try {
            if (!checkCode.equals(redisUtil.get(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey))) {
                throw new BusinessException(ErrorEnum.PARAM_ERROR, "check code error");
            }
        } finally {
            redisUtil.del(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
        }
        // 查表
        val userInfo = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getEmail, email));
        // 验证数据有效性
        if (userInfo == null) {
            throw new BusinessException(ErrorEnum.LOGIN_ERROR, "Please register firstly");
        } else if (Objects.equals(userInfo.getStatus(), UserStatusEnum.UNABLE.getStatus())) {
            throw new BusinessException(ErrorEnum.LOGIN_ERROR, "Account is unable");
        } else if (!Objects.equals(StringTool.encodeMd5(password), userInfo.getPassword())) {
            throw new BusinessException(ErrorEnum.LOGIN_ERROR, "Wrong password");
        }
        // TODO 查询我的群组
        // TODO 查询我的联系人

        // 获取TokenUserInfoDto
        val tokenUserInfoDto = getTokenUserInfoDto(userInfo);
        Long lastHeartBeat = redisComponent.getUserHeartBeat(userInfo.getId());
        if (lastHeartBeat != null) {
            throw new BusinessException(ErrorEnum.LOGIN_ERROR, "account has been logged in elsewhere, please login after logou t");
        }

        // 保存登录信息到redis中
        String token = StringTool.encodeMd5(tokenUserInfoDto.getUserId() + StringTool.getRandomString(Constants.LENGTH_20));
        tokenUserInfoDto.setToken(token);
        redisComponent.saveTokenUserInfo(tokenUserInfoDto);

        // 封装VO
        UserInfoVo userInfoVo = CopyUtil.copy(userInfo, UserInfoVo.class);
        userInfoVo.setToken(tokenUserInfoDto.getToken());
        userInfoVo.setAdmin(tokenUserInfoDto.getAdmin());

        return userInfoVo;
    }

    @Override
    public UserInfo getUserInfoByUserId(String userId) {
        return userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getId, userId));
    }

    @Override
    @SneakyThrows
    @Transactional
    public void updateUserInfo(UserInfo userInfo, MultipartFile avatarCover, MultipartFile avatarFile) {
        if (avatarFile != null) {
            String baseFolder = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
            File targetFileFolder = new File(baseFolder + Constants.FILE_FOLDER_AVATAR_NAME);
            if (!targetFileFolder.exists()) {
                targetFileFolder.mkdirs();
            }
            String filePath = targetFileFolder.getPath() + "/" + userInfo.getId() + Constants.IMAGE_SUFFIX;
            avatarFile.transferTo(new File(filePath));
            avatarCover.transferTo(new File(filePath + Constants.COVER_IMAGE_SUFFIX));
        }
        UserInfo dbUser = userInfoMapper.selectOne(new QueryWrapper<UserInfo>().eq("id", userInfo));
        userInfoMapper.updateById(userInfo);
        String contactNameUpdate = null;
        if (!dbUser.getNickName().equals(userInfo.getNickName())) {
            contactNameUpdate = userInfo.getNickName();
        }
        // todo 更新会话中的昵称信息

    }

    @Override
    public void updatePassword(String userId, String password) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(userId);
        userInfo.setPassword(StringTool.encodeMd5(password));
        userInfoMapper.updateById(userInfo);
    }

    private TokenUserInfoDto getTokenUserInfoDto(UserInfo userInfo) {
        TokenUserInfoDto tokenUserInfoDto = new TokenUserInfoDto();
        tokenUserInfoDto.setUserId(userInfo.getId());
        tokenUserInfoDto.setNickName(userInfo.getNickName());
        String emails = appConfig.getAdminEmails();
        tokenUserInfoDto.setAdmin(!StringTool.isEmpty(emails) && ArrayUtils.contains(emails.split(","), userInfo.getEmail()));
        return tokenUserInfoDto;
    }
}
