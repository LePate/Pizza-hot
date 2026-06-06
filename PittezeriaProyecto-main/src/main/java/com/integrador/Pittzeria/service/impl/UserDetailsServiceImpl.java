package com.integrador.Pittzeria.service.impl;

import com.integrador.Pittzeria.model.entity.Empleado;
import com.integrador.Pittzeria.repository.EmpleadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Buscamos al empleado por su nombre (o usuario) en la BD
        Empleado empleado = empleadoRepository.findByNombre(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // Retornamos un objeto User de Spring Security con sus credenciales y su Rol
        return User.builder()
                .username(empleado.getNombre())
                .password(empleado.getContraseña()) // Debe estar encriptada en la BD con BCrypt
                .roles(empleado.getRol().getNombreRol().toUpperCase()) // Ej: "ADMINISTRADOR" o "CAJERO"
                .build();
    }
}