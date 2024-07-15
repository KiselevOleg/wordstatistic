/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.user.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

/**
 * @author Kiselev Oleg
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Validated
@Entity
@Table(name = "user_role", schema = "public")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") private Integer id;

    @Length(min = 1, max = 50)
    @Column(name = "name", length = 50, nullable = false, unique = true) private String name;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {
        CascadeType.PERSIST,
        CascadeType.MERGE
    })
    @JoinTable(name = "user_role_permission",
        joinColumns = @JoinColumn(name = "role"),
        inverseJoinColumns = @JoinColumn(name = "permission")
    )
    private Set<Permission> permissions;
}
