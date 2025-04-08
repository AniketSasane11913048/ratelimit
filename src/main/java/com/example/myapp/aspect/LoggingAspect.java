package com.example.myapp.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

  @Around("@annotation(com.example.myapp.annotation.LogExecutionTime)") // Pointcut targeting the custom
  // annotation
  public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
    long start = System.currentTimeMillis();
    Object proceed = joinPoint.proceed(); // Execute the method
    long executionTime = System.currentTimeMillis() - start;

    log.info("{} executed in {} ms", joinPoint.getSignature(), executionTime);
    return proceed;
  }
}
