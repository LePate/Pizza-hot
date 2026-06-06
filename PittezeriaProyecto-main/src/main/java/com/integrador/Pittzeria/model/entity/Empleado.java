package com.integrador.Pittzeria.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "empleado")
@Data
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empleado")
    private Integer idEmpleado;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String apellido;

    // Aquí Spring Security validará las credenciales encriptadas
    @Column(nullable = false, length = 100)
    private String contraseña;

    @Column(length = 20)
    private String telefono;

    // Relación ManyToOne: Muchos empleados pertenecen a un solo Rol
    @ManyToOne
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;
}