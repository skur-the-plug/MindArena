package com.mindarena.broker;

import com.mindarena.config.PlatformConfig;
import com.mindarena.event.LeaderboardChangedEvent;
import com.mindarena.event.NotificationCreatedEvent;
import com.mindarena.event.XpAwardedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class DomainEventBrokerPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomainEventBrokerPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public DomainEventBrokerPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void publish(LeaderboardChangedEvent event) {
        publish(PlatformConfig.LEADERBOARD_CHANGED_ROUTING_KEY, event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void publish(NotificationCreatedEvent event) {
        publish(PlatformConfig.NOTIFICATION_CREATED_ROUTING_KEY, event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void publish(XpAwardedEvent event) {
        publish(PlatformConfig.XP_AWARDED_ROUTING_KEY, event);
    }

    private void publish(String routingKey, Object event) {
        try {
            rabbitTemplate.convertAndSend(PlatformConfig.DOMAIN_EVENTS_EXCHANGE, routingKey, event);
        } catch (AmqpException exception) {
            LOGGER.warn("Could not publish {} to RabbitMQ.", routingKey, exception);
        }
    }
}
