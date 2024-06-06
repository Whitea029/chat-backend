package fun.whitea.easychatbackend.utils;

import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class CopyUtil {
    private static final Logger log = LoggerFactory.getLogger(CopyUtil.class);

    public static <T, S> List<T> copyList(List<S> sourceList, Class<T> targetClass) {
        List<T> targetList = new ArrayList<>();
        for (S s : sourceList) {
            val t = copy(s, targetClass);
            targetList.add(t);
        }
        return targetList;
    }

    public static <T, S> T copy(S source, Class<T> targetClass) {
        T t = null;
        try {
            Constructor<T> constructor = targetClass.getConstructor();
            t = constructor.newInstance();
        } catch (Exception e) {
            log.error(TraceUtil.generateTraceMessage(e));
        }
        assert t != null;
        BeanUtils.copyProperties(source, t);
        return t;
    }


}
