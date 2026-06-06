package com.integrador.Pittzeria.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "venta")
@Data
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Integer idVenta;

    // Se asignará automáticamente la fecha y hora actual del servidor al registrarse
    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    // Aquí guardaremos la forma de entrega o el método de pago según los RF (Efectivo/Yape/Plin)
    @Column(name = "tipo_entrega", length = 50)
    private String tipoEntrega;

    // Relación ManyToOne: Muchas ventas pueden ser registradas por un solo Empleado (Cajero)
    @ManyToOne
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleado empleado;

    // Relación OneToMany: Una venta contiene múltiples detalles (líneas de la boleta)
    // El 'cascade = CascadeType.ALL' permite que si guardas la Venta, automáticamente guarde sus detalles.
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetalleVenta> detalles;
}