package com.example.myapp.aspect;

import com.example.myapp.annotation.Ratelimiter;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class RatelimitingAspect2 {

  @Qualifier("redisTemplate")
  private final RedisTemplate redisTemplate;

  @Around("@annotation(com.example.myapp.annotation.Ratelimiter)")
  public Object slidingWindowLogCounterRatelimit(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    Method method = methodSignature.getMethod();
    Ratelimiter ratelimiter = method.getAnnotation(Ratelimiter.class);

    long currentTimestamp = System.currentTimeMillis();
    int limit = ratelimiter.limit();
    long windowInMillis = TimeUnit.MILLISECONDS.convert(ratelimiter.time(), ratelimiter.timeUnit());

    String key = AOPUtil.getKeyVal(joinPoint, "ratelimiter", ratelimiter.key());
    long currentWindowTimeStamp = currentTimestamp - (currentTimestamp % windowInMillis);
    long prevWindowTimeStamp = currentWindowTimeStamp - windowInMillis;

    String currentWindowKey = key + "_" + currentWindowTimeStamp;
    String prevWindowKey = key + "_" + prevWindowTimeStamp;

    long currentWindowCount = redisTemplate.opsForValue().increment(currentWindowKey);

    // if not present then what happens
    if (currentWindowCount == 1L) {
      redisTemplate.expire(
          currentWindowKey,
          currentWindowTimeStamp + 2 * windowInMillis - currentTimestamp,
          TimeUnit.MILLISECONDS);
    }

    long prevWindowCount =
        Objects.isNull(redisTemplate.opsForValue().get(prevWindowKey))
            ? 0L
            : Long.valueOf((String) redisTemplate.opsForValue().get(prevWindowKey));

    long totalHits =
        getTotalHitsUsingWindowContribution(
            currentWindowTimeStamp,
            currentTimestamp,
            windowInMillis,
            currentWindowCount,
            prevWindowCount);

    if (totalHits > limit) {
      log.info("Rate limit exceeded for key: {}", key);
      throw new RuntimeException("Rate limit exceeded");
    }

    // Proceed with method execution
    return joinPoint.proceed();
  }

  long getTotalHitsUsingWindowContribution(
      long currentWindowStartTime,
      long currentTimeStamp,
      long windowInMillis,
      long currentWindowCount,
      long prevWindowCount) {

    long elapsed = currentTimeStamp - currentWindowStartTime;
    double weight = (double) (windowInMillis - elapsed) / windowInMillis;
    return Long.valueOf((long) Math.ceil(currentWindowCount + weight * prevWindowCount));
  }
}
