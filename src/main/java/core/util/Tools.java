package core.util;

import cn.hutool.core.lang.Filter;
import cn.hutool.core.util.ReflectUtil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

public class Tools {
    /**
     * 获得指定类过滤后的方法<br>
     * 返回满足条件的第一个, 如果没有符合条件的, 则返回 null
     *
     * @param clazz  查找方法的类
     * @param filter 过滤器
     * @return 过滤后的方法
     */
    public static Method getMethod(Class<?> clazz, Filter<Method> filter) {
        if (null == clazz) {
            return null;
        }

        final Method[] methods = ReflectUtil.getMethods(clazz);
        if (null != filter) {
            for (Method method : methods) {
                if (filter.accept(method)) {
                    return method;
                }
            }
        }
        return null;
    }

    /**
     * 获取父路径
     */
    public static String getParent(String path) {
        if (path.endsWith("/") || path.endsWith("\\")) {
            path = path.substring(0, path.length() - 1);
        }
        int endIndex = Math.max(path.lastIndexOf("/"), path.lastIndexOf("\\"));
        return endIndex > -1 ? path.substring(0, endIndex) : null;
    }

    /**
     * 合并路径
     */
    public static String join(String... paths) {
        StringBuilder sb = new StringBuilder();
        for (String path : paths) {
            String left = sb.toString();
            boolean leftHas = left.endsWith("/") || left.endsWith("\\");
            boolean rightHas = path.startsWith("/") || path.startsWith("\\");

            if (leftHas && rightHas) {
                sb.append(path.substring(1));
            } else if (!left.isEmpty() && !leftHas && !rightHas) {
                sb.append("/").append(path);
            } else {
                sb.append(path);
            }
        }
        return sb.toString();
    }

    /**
     * 类型转换
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object object) {
        return (T) object;
    }

    /**
     * 获取流的大小（长度）
     */
    public static long getSize(InputStream in) throws IOException {
        long size = 0;
        while (in.read() != -1) size++;
        return size;
    }

    /**
     * 按照参数从前往后进行判断，返回第一个不为 null 的参数
     */
    @SafeVarargs
    public static <T> T getNotNull(T... args) {
        for (T t : args) if (t != null) return t;
        throw new NullPointerException();
    }
}
