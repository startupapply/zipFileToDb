package com.startup.cities.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Obec")
public class Obec {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "nazev")
    private String nazev;

    @Column(name = "kod")
    private Integer kod;
}
