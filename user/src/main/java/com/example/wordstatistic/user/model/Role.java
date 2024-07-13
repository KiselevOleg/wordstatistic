/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.user.model;

import jakarta.persistence.*;
import lombok.*;

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
@Entity
@Table(name = "user_role", schema = "public")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") private Integer id;

    @Column(name = "name", length = 20, nullable = false, unique = true) private String name;

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
