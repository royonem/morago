package com.roy.morago.entity.user;

import com.roy.morago.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "languages")
public class Language extends BaseEntity {
    @ManyToMany(mappedBy = "languages")
    private Set<User> users;

    @Column(nullable = false, unique = true)
    private String name;
}
