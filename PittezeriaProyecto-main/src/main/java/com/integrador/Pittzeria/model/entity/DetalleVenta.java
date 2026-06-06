package com.integrador.Pittzeria.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "detalle_venta")
@Data
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Integer idDetalle;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    // Relación ManyToOne: Muchos detalles de producto pertenecen a una sola Venta maestra
    @ManyToOne
    @JoinColumn(name = "id_venta", nullable = false)
    private Venta venta;

    // Relación ManyToOne: Muchos detalles pueden referenciar al mismo Producto (ej: varias mesas piden Pepperoni)
    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;
}