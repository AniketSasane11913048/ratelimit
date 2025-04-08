package com.example.myapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class RedisConfig {
  @Bean
  public JedisConnectionFactory jedisConnectionFactory() {
    JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(10); // Max connections
    poolConfig.setMaxIdle(5); // Max idle connections
    poolConfig.setMinIdle(2); // Min idle connections

    JedisConnectionFactory factory = new JedisConnectionFactory();
    factory.setHostName("localhost");
    factory.setPort(6379);
    factory.setTimeout(6000);
    factory.setUsePool(true);
    factory.setPoolConfig(poolConfig);
    return factory;
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    return template;
  }
}
