package com.integrador.Pittzeria.repository;

import com.integrador.Pittzeria.model.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {

    // Para el RF10 y RF11: Historial de transacciones por rango de fechas
    List<Venta> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    // Para el RF11: Consulta personalizada (JPQL) para obtener el ranking de pizzas más vendidas
    @Query("SELECT dv.producto.nombreProducto, SUM(dv.cantidad) FROM DetalleVenta dv " +
            "GROUP BY dv.producto.idProducto " +
            "ORDER BY SUM(dv.cantidad) DESC")
    List<Object[]> obtenerRankingPizzas();
}