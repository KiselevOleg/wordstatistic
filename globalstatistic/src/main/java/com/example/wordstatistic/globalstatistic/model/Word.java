/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.globalstatistic.model;

import com.example.wordstatistic.globalstatistic.dto.WordDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
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
@Table(name = "word", schema = "public")
public class Word {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "name", nullable = false, unique = true)
    @Length(min = 1, max = 50)
    private String name;
    @Column(name = "count", nullable = false, unique = false)
    @Min(0)
    private Integer count;

    /**
     * get a dto for sending.
     * @return a dto object
     */
    public WordDTO toDTO() {
        return new WordDTO(this.name, this.count);
    }
}
