package com.roy.morago.dto.topic;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopicRequest {
    private Long categoryId;
    private Long iconId;
    @NotBlank
    private String name;
    private Boolean active;
}
