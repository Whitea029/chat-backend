package fun.whitea.easychatbackend.utils;

import fun.whitea.easychatbackend.entity.constants.Constants;
import fun.whitea.easychatbackend.entity.enums.UserContactTypeEnum;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.util.DigestUtils;

public class StringTool {

    public static String getUserId() {
        return UserContactTypeEnum.USER.getPrefix() + getRandomNumber(Constants.LENGTH_11);
    }

    public static String getGroupId() {
        return UserContactTypeEnum.GROUP.getPrefix() + getRandomNumber(Constants.LENGTH_11);
    }

    public static String getRandomNumber(int length) {
        return RandomStringUtils.random(length,false,true);
    }

    public static String getRandomString(int length) {
        return RandomStringUtils.random(length,false,true);
    }

    public static String encodeMd5(String str) {
        return StringTool.isEmpty(str) ? null : DigestUtils.md5DigestAsHex(str.getBytes());
    }

    public static Boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
