
package com.resourcebooking.booking.event;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class SlotCacheInvalidationListener {

    private final CacheManager cacheManager;

    public SlotCacheInvalidationListener(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void invalidateSlotCache(SlotCacheInvalidationEvent event) {

        Cache cache = cacheManager.getCache("slots");

        if (cache != null && event.resourceId() != null) {
            cache.evict(event.resourceId());
        }
    }
}
