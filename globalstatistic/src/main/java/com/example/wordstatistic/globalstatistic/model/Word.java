/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.globalstatistic.model;

import com.example.wordstatistic.globalstatistic.dto.WordDTO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;

/**
 * @author Kiselev Oleg
 */
@Entity
@Table(name = "word", schema = "public")
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Word {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    @Column(name = "count", nullable = false, unique = false)
    @Check(constraints = "count>=0")
    private Integer count;

    /**
     * get a dto for sending.
     * @return a dto object
     */
    public WordDTO toDTO() {
        return new WordDTO(this.name, this.count);
    }
}
