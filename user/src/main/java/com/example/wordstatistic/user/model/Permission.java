/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

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
@Table(name = "user_permission", schema = "public")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") private Integer id;

    @Length(min = 1, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "incorrect value")
    @Column(name = "name", length = 50, nullable = false, unique = true) private String name;

    //@ManyToMany(fetch=FetchType.LAZY, mappedBy = "permissions")
    //private Set<Role> roles;
}
