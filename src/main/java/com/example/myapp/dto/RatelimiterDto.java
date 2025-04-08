package com.example.myapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RatelimiterDto {
  private RatelimiterWindowDto prevWindow;
  private RatelimiterWindowDto currWindow;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class RatelimiterWindowDto {
    private long startTime;
    private int count;
  }
}
