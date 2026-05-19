package com.mindarena.config;

import java.time.Duration;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync(proxyTargetClass = true)
@EnableCaching
public class PlatformConfig {

    public static final String GLOBAL_LEADERBOARD_CACHE = "globalLeaderboard";
    public static final String ARENA_LEADERBOARD_CACHE = "arenaLeaderboard";
    public static final String CHALLENGE_LEADERBOARD_CACHE = "challengeLeaderboard";
    public static final String DOMAIN_EVENTS_EXCHANGE = "mindarena.domain";
    public static final String LEADERBOARD_CHANGED_ROUTING_KEY = "leaderboard.changed";
    public static final String NOTIFICATION_CREATED_ROUTING_KEY = "notification.created";
    public static final String XP_AWARDED_ROUTING_KEY = "xp.awarded";
    public static final String LEADERBOARD_EVENTS_QUEUE = "mindarena.leaderboard.events";
    public static final String NOTIFICATION_EVENTS_QUEUE = "mindarena.notification.events";
    public static final String XP_EVENTS_QUEUE = "mindarena.xp.events";

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .prefixCacheNameWith("mindarena:");

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfiguration)
                .withCacheConfiguration(GLOBAL_LEADERBOARD_CACHE, cacheConfiguration)
                .withCacheConfiguration(ARENA_LEADERBOARD_CACHE, cacheConfiguration)
                .withCacheConfiguration(CHALLENGE_LEADERBOARD_CACHE, cacheConfiguration)
                .transactionAware()
                .build();
    }

    @Bean
    public TopicExchange domainEventsExchange() {
        return new TopicExchange(DOMAIN_EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public Queue leaderboardEventsQueue() {
        return new Queue(LEADERBOARD_EVENTS_QUEUE, true);
    }

    @Bean
    public Queue notificationEventsQueue() {
        return new Queue(NOTIFICATION_EVENTS_QUEUE, true);
    }

    @Bean
    public Queue xpEventsQueue() {
        return new Queue(XP_EVENTS_QUEUE, true);
    }

    @Bean
    public Binding leaderboardEventsBinding(Queue leaderboardEventsQueue, TopicExchange domainEventsExchange) {
        return BindingBuilder.bind(leaderboardEventsQueue)
                .to(domainEventsExchange)
                .with(LEADERBOARD_CHANGED_ROUTING_KEY);
    }

    @Bean
    public Binding notificationEventsBinding(Queue notificationEventsQueue, TopicExchange domainEventsExchange) {
        return BindingBuilder.bind(notificationEventsQueue)
                .to(domainEventsExchange)
                .with(NOTIFICATION_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding xpEventsBinding(Queue xpEventsQueue, TopicExchange domainEventsExchange) {
        return BindingBuilder.bind(xpEventsQueue)
                .to(domainEventsExchange)
                .with(XP_AWARDED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        return factory;
    }
}
