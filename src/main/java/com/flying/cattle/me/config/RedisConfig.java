package com.flying.cattle.me.config;

import org.redisson.Redisson;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class RedisConfig {
    
    @Bean
    Redisson redisson(RedisProperties redisProperties) {
        String prefix = "redis://";
        String[] nodes = redisProperties.getSentinel().getNodes().stream().map(n -> n.startsWith("redis") ? n : prefix + n).toArray(String[]::new);

        Config config = new Config();
        SentinelServersConfig sentinel = config.useSentinelServers()
                .addSentinelAddress(nodes)
                .setDatabase(redisProperties.getDatabase())
                .setMasterName(redisProperties.getSentinel().getMaster());
        if (redisProperties.getPassword() != null) {
            sentinel.setPassword(redisProperties.getPassword());
        }
        log.info("初始化 Redisson 哨兵模式，参与节点：{}", redisProperties.getSentinel().getNodes());
        return (Redisson) Redisson.create(config);
    }
	
    @Bean
   	public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
   		ReactiveRedisTemplate<String, String> reactiveRedisTemplate = new ReactiveRedisTemplate<>(factory,RedisSerializationContext.string());
   		return reactiveRedisTemplate;
   	}
}
