package com.roy.morago.entity.topic;

import com.roy.morago.entity.BaseEntity;
import com.roy.morago.entity.file.File;
import com.roy.morago.entity.user.User;
import jakarta.persistence.*;
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
@Table(name = "topics")
public class Topic extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "topic_icon_id")
    private File icon;

    @ManyToMany(mappedBy = "topics")
    private Set<User> users;

    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false)
    private Boolean active;
}
