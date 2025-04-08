//package com.example.myapp.aspect;
//
//import com.example.myapp.annotation.Ratelimiter;
//import com.example.myapp.dto.RatelimiterDto;
//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.MapperFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;
//import java.lang.reflect.Method;
//import java.util.concurrent.TimeUnit;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.reflect.MethodSignature;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Component;
//
//@Aspect
//@Component
//@Slf4j
//@RequiredArgsConstructor
//public class RatelimitingAspect {
//
//  @Qualifier("redisTemplate")
//  private final RedisTemplate redisTemplate;
//
//  private static final ObjectMapper MAPPER =
//      new ObjectMapper()
//          .setSerializationInclusion(JsonInclude.Include.NON_NULL)
//          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//          .configure(MapperFeature.USE_GETTERS_AS_SETTERS, false)
//          .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
//
//  @Around("@annotation(com.example.myapp.annotation.Ratelimiter)")
//  public Object slidingWindowLogCounterRatelimit(ProceedingJoinPoint joinPoint) throws Throwable {
//    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
//    Method method = methodSignature.getMethod();
//    Ratelimiter ratelimiter = method.getAnnotation(Ratelimiter.class);
//
//    long currentTimestamp = System.currentTimeMillis();
//    int limit = ratelimiter.limit();
//    long windowInMillis = TimeUnit.MILLISECONDS.convert(ratelimiter.time(), ratelimiter.timeUnit());
//
//    String key = AOPUtil.getKeyVal(joinPoint, "ratelimiter", ratelimiter.key());
//    String redisKey = AOPUtil.getMethod(joinPoint).getName() + "_" + key;
//
//    String json = (String) redisTemplate.opsForValue().get(redisKey);
//    RatelimiterDto ratelimiterDto;
//
//    if (!redisTemplate.hasKey(redisKey)) {
//      // No existing record, create a fresh window state
//      ratelimiterDto =
//          RatelimiterDto.builder()
//              .prevWindow(
//                  RatelimiterDto.RatelimiterWindowDto.builder()
//                      .startTime(currentTimestamp - windowInMillis)
//                      .count(0)
//                      .build())
//              .currWindow(
//                  RatelimiterDto.RatelimiterWindowDto.builder()
//                      .startTime(currentTimestamp)
//                      .count(1)
//                      .build())
//              .build();
//    } else {
//
//      ratelimiterDto = MAPPER.readValue(json, RatelimiterDto.class);
//      long currWindowStart = ratelimiterDto.getCurrWindow().getStartTime();
//      int currCount = ratelimiterDto.getCurrWindow().getCount();
//
//      if (currentTimestamp >= currWindowStart + 2 * windowInMillis) {
//        // Entire window expired, reset both
//        ratelimiterDto.setPrevWindow(
//            RatelimiterDto.RatelimiterWindowDto.builder()
//                .startTime(currentTimestamp - windowInMillis)
//                .count(0)
//                .build());
//        ratelimiterDto.setCurrWindow(
//            RatelimiterDto.RatelimiterWindowDto.builder()
//                .startTime(currentTimestamp)
//                .count(1)
//                .build());
//      } else if (currentTimestamp >= currWindowStart + windowInMillis) {
//        // Slide window
//        ratelimiterDto.setPrevWindow(ratelimiterDto.getCurrWindow());
//        ratelimiterDto.setCurrWindow(
//            RatelimiterDto.RatelimiterWindowDto.builder()
//                .startTime(currentTimestamp)
//                .count(1)
//                .build());
//
//        double totalHits =
//            getTotalHitsUsingWindowContribution(ratelimiterDto, currentTimestamp, windowInMillis);
//
//        if (totalHits > limit) {
//            throw new RuntimeException("Rate limit exceeded");
//        }
//
//      } else {
//        // Within current window, apply weighted count
//        double totalHits =
//            getTotalHitsUsingWindowContribution(ratelimiterDto, currentTimestamp, windowInMillis);
//
//        if (totalHits > limit) {
//            throw new RuntimeException("Rate limit exceeded");
//        }
//        ratelimiterDto.getCurrWindow().setCount(currCount + 1);
//      }
//    }
//
//    redisTemplate
//        .opsForValue()
//        .set(
//            redisKey,
//            MAPPER.writeValueAsString(ratelimiterDto),
//            2 * windowInMillis,
//            TimeUnit.MILLISECONDS);
//
//    // Proceed with method execution
//    return joinPoint.proceed();
//  }
//
//  double getTotalHitsUsingWindowContribution(
//      RatelimiterDto ratelimiterDto, long currentTimeStamp, long windowInMillis) {
//
//    long elapsed = currentTimeStamp - ratelimiterDto.getCurrWindow().getStartTime();
//    double weight = (double) (windowInMillis - elapsed) / windowInMillis;
//    return ratelimiterDto.getCurrWindow().getCount()
//        + weight * ratelimiterDto.getPrevWindow().getCount();
//  }
//}
