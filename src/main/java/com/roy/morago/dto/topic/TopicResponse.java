package com.roy.morago.dto.topic;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopicResponse {
    private Long id;
    private Long categoryId;
    private Long iconId;
    private String name;
    private Boolean active;
}
