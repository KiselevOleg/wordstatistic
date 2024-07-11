/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.user.model;

import jakarta.persistence.*;
import lombok.*;

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
@Table(name = "user", schema = "public")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") private Long id;

    @Column(name = "name", length = 50, nullable = false, unique = true) private String name;

    @Column(name = "password", length = 60, nullable = false) private String password;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role", referencedColumnName = "id", nullable = false, unique = false)
    private Role role;
}
