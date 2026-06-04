package com.roy.morago.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private String title;
    private String content;
    private String actionUrl;
    private LocalDateTime scheduledFor;
}
