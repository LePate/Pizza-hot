package com.integrador.Pittzeria.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "rol")
@Data // Lombok genera getters, setters, toString de forma automática
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Integer idRol;

    @Column(name = "nombre_rol", nullable = false, length = 50)
    private String nombreRol;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
}