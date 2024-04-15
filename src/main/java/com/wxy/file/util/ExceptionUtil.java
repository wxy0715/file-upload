package com.wxy.file.util;

import cn.hutool.core.util.ObjectUtil;
import com.wxy.file.result.BusinessException;

public class ExceptionUtil {
    public static void isNull(Object value,String msg) {
        if (ObjectUtil.isEmpty(value)) {
            throw new BusinessException(msg);
        }
    }

    public static void isTrue(boolean flag, String msg) {
        if (flag) {
            throw new BusinessException(msg);
        }
    }

    public static void error(String msg) {
        throw new BusinessException(msg);
    }
}
