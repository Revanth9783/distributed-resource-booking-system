package com.resourcebooking.booking.config;

import com.resourcebooking.booking.dto.ResourceResponse;
import com.resourcebooking.booking.entity.ResourceStatus;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;

class RedisCacheConfigTest {

    @Test
    @SuppressWarnings("unchecked")
    void resourceCachePreservesResponseTypeOnRead() {
        RedisCacheManager manager = (RedisCacheManager) new RedisCacheConfig()
                .cacheManager(mock(RedisConnectionFactory.class));
        manager.afterPropertiesSet();
        manager.getCache("resources");
        var config = manager.getCacheConfigurations().get("resources");
        var serializer = (RedisSerializationContext.SerializationPair<Object>)
                config.getValueSerializationPair();
        var expected = List.of(new ResourceResponse(
                1L, 1L, "Test resource", "Description", 100,
                ResourceStatus.ACTIVE, "Local test"));

        Object restored = serializer.read(serializer.write(expected));

        List<?> restoredList = assertInstanceOf(List.class, restored);
        assertInstanceOf(ResourceResponse.class, restoredList.getFirst());
        assertEquals(expected, restoredList);
        assertEquals("resources:v2::", config.getKeyPrefixFor("resources"));
    }
}
