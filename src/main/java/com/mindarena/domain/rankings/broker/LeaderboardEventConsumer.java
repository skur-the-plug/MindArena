package com.mindarena.domain.rankings.broker;

import com.mindarena.config.PlatformConfig;
import com.mindarena.domain.rankings.event.LeaderboardChangedEvent;
import com.mindarena.domain.rankings.service.LeaderboardCacheInvalidator;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class LeaderboardEventConsumer {

    private final LeaderboardCacheInvalidator leaderboardCacheInvalidator;

    public LeaderboardEventConsumer(LeaderboardCacheInvalidator leaderboardCacheInvalidator) {
        this.leaderboardCacheInvalidator = leaderboardCacheInvalidator;
    }

    @RabbitListener(queues = PlatformConfig.LEADERBOARD_EVENTS_QUEUE)
    public void onLeaderboardChanged(LeaderboardChangedEvent event) {
        leaderboardCacheInvalidator.invalidate(event);
    }
}
