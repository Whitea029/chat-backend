package fun.whitea.easychatbackend.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

/**
 * Json序列化工具类
 */
public class JsonUtil {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
    }

    @SneakyThrows
    public static String object2Json(Object object) {
        return objectMapper.writeValueAsString(object);
    }

    @SneakyThrows
    public static <T> T json2Object(String json, Class<T> clazz) {
        return json == null? null : objectMapper.readValue(json, clazz);
    }
}