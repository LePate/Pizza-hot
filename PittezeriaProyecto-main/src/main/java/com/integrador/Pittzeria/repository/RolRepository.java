package com.integrador.Pittzeria.repository;

import com.integrador.Pittzeria.model.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {
    // Aquí puedes buscar un rol por su nombre si lo necesitas para la seguridad
    Rol findByNombreRol(String nombreRol);
}