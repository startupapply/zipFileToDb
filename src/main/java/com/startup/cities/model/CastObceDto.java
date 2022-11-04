package com.startup.cities.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CastObceDto {
    private String id;
    private String nazev;
    private Integer kod;
    private Integer obec;
}
