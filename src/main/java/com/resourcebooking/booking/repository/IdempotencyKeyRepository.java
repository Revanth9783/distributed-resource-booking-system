
package com.resourcebooking.booking.repository;

import com.resourcebooking.booking.entity.IdempotencyKey;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IdempotencyKeyRepository
        extends JpaRepository<IdempotencyKey, Long> {

    @Modifying
    @Query(value = """
        INSERT INTO idempotency_keys
            (user_id, key, request_hash, status, expires_at)
        VALUES
            (:userId, :key, :requestHash, 'PROCESSING', :expiresAt)
        ON CONFLICT (user_id, key) DO NOTHING
        """, nativeQuery = true)
    int insertIfAbsent(
            @Param("userId") Long userId,
            @Param("key") String key,
            @Param("requestHash") String requestHash,
            @Param("expiresAt") java.time.LocalDateTime expiresAt
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT i FROM IdempotencyKey i
        WHERE i.userId = :userId AND i.key = :key
        """)
    Optional<IdempotencyKey> findByUserIdAndKeyForUpdate(
            @Param("userId") Long userId,
            @Param("key") String key
    );
}
