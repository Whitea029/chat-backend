package fun.whitea.easychatbackend.entity.constants;

import fun.whitea.easychatbackend.entity.enums.UserContactTypeEnum;

public class Constants {

    public static final Integer REDIS_TIME_1MIN = 60;
    public static final Integer LENGTH_20 = 20;
    public static final Integer LENGTH_11 = 11;
    public static final Integer REDIS_KEY_EXPIRES_HEART_BEAT = 6;

    public static final String REDIS_KEY_WS_USER_HEART_BEAT = "ws:user:heartbeat:";
    public static final String REDIS_KEY_CHECK_CODE = "checkCode:";
    public static final String REDIS_KEY_WS_TOKEN = "ws:token:";
    public static final Integer REDIS_TIME_DAY = 60 * 24 * REDIS_TIME_1MIN;
    public static final String REDIS_KEY_WS_TOKEN_USERID = "ws:token:userid:";
    public static final String ROBOT_UID = UserContactTypeEnum.USER.getPrefix() + "robot";
    public static final String REDIS_KEY_SYS_SETTING = "syssetting:";
    public static final String FILE_FOLDER_FILE = "file/";
    public static final String FILE_FOLDER_AVATAR_NAME = "avatar/";
    public static final String IMAGE_SUFFIX = ".png";
    public static final String COVER_IMAGE_SUFFIX = "_cover.png";
    public static final String APPLY_INFO_TEMPLATE = "My name is s%";
    public static final Integer PAGE_SIZE15 = 15;
    public static final String REGEX_PASSWORD = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$";


    public static final String REDIS_KEY_USER_CONTACT = "ws:user:contact:";
    public static final Long MILLIS_SECONDS_3_DAYS_AGO = 3 * 60 * 24 * 60 * 1000L;
}
