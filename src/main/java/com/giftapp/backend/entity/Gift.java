package com.giftapp.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(
    uniqueConstraints = @UniqueConstraint(
        name = "uq_gift_category_colour_name",
        columnNames = {"category", "colour", "name"}
    )
)
public class Gift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String category;    // e.g. FASHION, ELECTRONICS
    private String colour;
    private Double price;
    private String gender;     // MALE / FEMALE / UNISEX
    private String occasion;   // BIRTHDAY / ANNIVERSARY
    private Integer minAge;
    private Integer maxAge;
}