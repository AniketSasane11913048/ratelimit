package com.example.myapp.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.TimeUnit;


@Retention(RetentionPolicy.RUNTIME)
public @interface Ratelimiter {
  String key() default "ratelimit";
  TimeUnit timeUnit() default TimeUnit.MINUTES;
  int time() default 100;
  int limit() default 1000;
}
