package fun.whitea.easychatbackend.utils;

import fun.whitea.easychatbackend.entity.constants.Constants;
import fun.whitea.easychatbackend.entity.enums.UserContactTypeEnum;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.Arrays;

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

    public static String cleanHtmlTag(String content) {
        if (isEmpty(content)) {
            return content;
        }
        content = content.replaceAll("<", "&lt;");
        content = content.replaceAll("\r\n", "<br>");
        content = content.replaceAll("\n", "<br>");
        return content;
    }

    public static final String getChatSessionId4User(String[] userIds) {
        Arrays.sort(userIds);
        return encodeMd5(StringUtils.join(userIds,""));
    }
}
