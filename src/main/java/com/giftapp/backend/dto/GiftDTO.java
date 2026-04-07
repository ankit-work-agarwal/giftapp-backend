package com.giftapp.backend.dto;

import lombok.Data;
import java.util.Set;

@Data
public class GiftDTO {

    private Long id;
    private String name;
    private String category;
    private String colour;
    private Double price;
    private String gender;     // MALE / FEMALE / UNISEX
    private String occasion;   // BIRTHDAY / ANNIVERSARY
    private Integer minAge;
    private Integer maxAge;
    private Set<String> tags;
}