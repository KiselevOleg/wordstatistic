/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.model;

import com.example.wordstatistic.localstatistic.dto.TextEntityDTO;
import com.example.wordstatistic.localstatistic.dto.TextListDTO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

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
@Table(name = "text", schema = "public")
public class Text {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "topic", nullable = false, unique = false)
    private Topic topic;
    @Length(min = 1, max = 50)
    @Column(name = "name", length = 50, nullable = false, unique = false)
    private String name;
    @Column(name = "text", nullable = false, unique = false, columnDefinition = "TEXT")
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
