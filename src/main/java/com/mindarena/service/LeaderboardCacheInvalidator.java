package com.mindarena.service;

import com.mindarena.config.PlatformConfig;
import com.mindarena.event.LeaderboardChangedEvent;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class LeaderboardCacheInvalidator {

    private final CacheManager cacheManager;

    public LeaderboardCacheInvalidator(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onLeaderboardChanged(LeaderboardChangedEvent event) {
        invalidate(event);
    }

    public void invalidate(LeaderboardChangedEvent event) {
        clear(PlatformConfig.GLOBAL_LEADERBOARD_CACHE);
        if (event.arenaId() != null) {
            evict(PlatformConfig.ARENA_LEADERBOARD_CACHE, event.arenaId());
        }
        if (event.challengeId() != null) {
            evict(PlatformConfig.CHALLENGE_LEADERBOARD_CACHE, event.challengeId());
        }
    }

    private void clear(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    private void evict(String cacheName, Long key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }
}
