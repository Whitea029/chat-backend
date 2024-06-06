package fun.whitea.easychatbackend.utils;

import java.util.Arrays;

/**
 * @author feellmoose
 * 异常堆栈处理工具类
 */
public final class TraceUtil {

    private TraceUtil() {
    }

    /**
     * 从包装好的的异常内部，获取某类异常诱因
     *
     * @param exception 需处理异常
     * @param clazz     目的异常类
     * @param <T>       目的异常类
     * @return 目的异常
     */
    public static <T extends Throwable> T getCause(Throwable exception, Class<T> clazz) {
        if (clazz.isInstance(exception)) {
            return clazz.cast(exception);
        } else if (exception.getCause() != null) {
            return getCause(exception.getCause(), clazz);
        }
        return null;
    }

    /**
     * 格式化堆栈信息
     *
     * @param exception 需处理异常
     * @return 格式化后异常信息
     */
    public static String generateTraceMessage(Throwable exception) {
        var message = exception.getMessage();
        var sb = new StringBuilder();
        Arrays.stream(exception.getStackTrace())
                .limit(5)
                .forEach(stackTraceElement -> sb.append("   at ").append(stackTraceElement.toString()).append("\n"));
        if (exception.getCause() != null) {
            sb.append("Caused by: ").append(generateTraceMessage(exception.getCause())).append("\n");
        }
        return exception.getClass() + ": " + (message == null ? "\n" : message + "\n") + sb;
    }

}
