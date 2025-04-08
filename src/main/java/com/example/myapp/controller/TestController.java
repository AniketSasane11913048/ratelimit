package com.example.myapp.controller;

import com.example.myapp.annotation.Ratelimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/v1/test/")
@Slf4j
public class TestController {

  @Ratelimiter(key = "#uid", timeUnit = TimeUnit.MINUTES, time = 1, limit = 5)
  @GetMapping(value = "/health")
  public String health(@RequestParam String uid) {
    log.info("ssss");
    return "OK";
  }
}
