package com.roy.morago.repository.notification;

import com.roy.morago.entity.notification.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {
    Page<Notification> findByUserId(Long userId, Pageable pageable);
    Page<Notification> findByUserIdAndIsReadFalse(Long userId, Pageable pageable);
    void deleteByIdInAndUserId(List<Long> ids, Long userId);
    void deleteByUserIdAndIsReadTrue(Long userId);
    void deleteByIdInAndSentAtIsNull(List<Long> ids);
    Long countByUserIdAndIsReadFalse(Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = CASE WHEN n.isRead = true THEN false ELSE true END, " +
            "n.readAt = CASE WHEN n.isRead = true THEN null ELSE CURRENT_TIMESTAMP END " +
            "WHERE n.id IN :ids AND n.user.id = :userId")
    void toggleReadByIdInAndUserId(@Param("ids") List<Long> ids, @Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.id IN :ids AND n.user.id = :userId")
    void markAsReadByIdInAndUserId(List<Long> notificationIds, Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.user.id = :userId")
    void markAsReadByUserId(Long userId);
}
