/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.model;

import com.example.wordstatistic.localstatistic.dto.TextEntityDTO;
import com.example.wordstatistic.localstatistic.dto.TextListDTO;
import jakarta.persistence.*;
import lombok.*;

/**
 * @author Kiselev Oleg
 */
@Entity
@Table(name = "text", schema = "public")
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Text {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "topic", nullable = false, unique = false)
    private Topic topic;
    @Column(name = "name", nullable = false, unique = false)
    private String name;
    @Column(name = "text", nullable = false, unique = false)
    private String text;

    /**
     * transfer into a dto for getting a text list.
     * @return a dto object
     */
    public TextListDTO toListDTO() {
        return new TextListDTO(this.name);
    }

    /**
     * transfer into a dto for getting a text entity.
     * @return a dto object
     */
    public TextEntityDTO toEntityDTO() {
        return new TextEntityDTO(this.topic.getName(), this.name, this.text);
    }
}
