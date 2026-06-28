package com.roy.morago.mapper;

import com.roy.morago.dto.notification.NotificationRequest;
import com.roy.morago.dto.notification.NotificationResponse;
import com.roy.morago.entity.notification.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {
    @Mapping(target = "userId", source = "user.id")
    NotificationResponse toResponse(Notification notification);
    Notification toEntity(NotificationRequest notificationRequest);
}
