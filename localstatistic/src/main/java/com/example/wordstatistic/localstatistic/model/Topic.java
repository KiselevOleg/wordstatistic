/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.model;

import com.example.wordstatistic.localstatistic.dto.TopicDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

/**
 * @author Kiselev Oleg
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Validated
@Entity
@Table(name = "topic", schema = "public")
public class Topic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "user_id", nullable = false, unique = false)
    private UUID userId;
    @Column(name = "user_name", length = 50, nullable = false, unique = false)
    @Length(min = 1, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "incorrect value")
    private String userName;
    @Column(name = "name", length = 50, nullable = false, unique = false)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "incorrect value")
    @Length(min = 1, max = 50)
    private String name;

    /**
     * transfer into a dto for getting a topic list.
     * @return a dto object
     */
    public TopicDTO toDTO() {
        return new TopicDTO(this.name);
    }
}
