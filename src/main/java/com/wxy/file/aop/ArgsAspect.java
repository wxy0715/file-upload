package com.wxy.file.aop;

import com.wxy.file.util.TUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * 切面把用户请求的数据字符去除前后空格
 * @author wxy
 */
@Slf4j
@Aspect
@Component
public class ArgsAspect implements Ordered {

    @Pointcut("execution(* com.wxy..*.controller.*.*(..))")
    public void point() {
    }

    @Before("point()")
    public void before(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            try {
                TUtils.trimBeanString(arg);
            } catch (Exception e) {
                log.info("参数处理错误:{}",e.getMessage());
            }
        }
    }

    @Override
    public int getOrder() {
        return 1;
    }

}
