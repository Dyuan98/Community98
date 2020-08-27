package com.yuan.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * AOP切面示例
 */
//@Component
//@Aspect
public class AlphaAspect {

    @Pointcut("execution(* com.yuan.community.service.*.*(..))")
    public void pointcut() {

    }

    // 方法执行前执行
    @Before("pointcut()")
    public void before() {
        System.out.println("before");
    }

    // 方法执行后执行
    @After("pointcut()")
    public void after() {
        System.out.println("after");
    }

    // 返回值返回之后执行
    @AfterReturning("pointcut()")
    public void afterRetuning() {
        System.out.println("afterRetuning");
    }

    // 抛出异常后执行
    @AfterThrowing("pointcut()")
    public void afterThrowing() {
        System.out.println("afterThrowing");
    }

    // 环绕执行
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("around before");
        Object obj = joinPoint.proceed();
        System.out.println("around after");
        return obj;
    }

}
