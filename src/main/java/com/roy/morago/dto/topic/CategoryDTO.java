package com.roy.morago.dto.topic;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Long id;
    @NotNull(message = "Category name is required.")
    private String name;
}
