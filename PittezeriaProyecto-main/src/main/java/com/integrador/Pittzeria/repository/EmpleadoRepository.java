package com.integrador.Pittzeria.repository;

import com.integrador.Pittzeria.model.entity.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {
    // Este método es CRÍTICO para el RF01 (Login).
    // Spring Security buscará al empleado en la base de datos usando su nombre o usuario.
    Optional<Empleado> findByNombre(String nombre);
}