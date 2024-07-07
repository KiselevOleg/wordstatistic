/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.model;

import com.example.wordstatistic.localstatistic.dto.TopicDTO;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * @author Kiselev Oleg
 */
@Entity
@Table(name = "topic", schema = "public")
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Topic {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Column(name = "user_id", nullable = false, unique = false)
    private UUID userId;
    @Column(name = "user_name", nullable = false, unique = false)
    private String userName;
    @Column(name = "name", nullable = false, unique = false)
    private String name;

    /**
     * transfer into a dto for getting a topic list.
     * @return a dto object
     */
    public TopicDTO toDTO() {
        return new TopicDTO(this.name);
    }
}
