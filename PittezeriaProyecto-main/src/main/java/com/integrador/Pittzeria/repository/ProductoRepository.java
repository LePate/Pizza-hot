package com.integrador.Pittzeria.repository;

import com.integrador.Pittzeria.model.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // Para el RF08: Alertas de stock mínimo (ejemplo: traer productos con stock menor a 5)
    List<Producto> findByStockLessThan(Integer limiteMinimo);
}