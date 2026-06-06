package com.integrador.Pittzeria.service.impl;

import com.integrador.Pittzeria.model.entity.Empleado;
import com.integrador.Pittzeria.repository.EmpleadoRepository;
import com.integrador.Pittzeria.service.IEmpleadoService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmpleadoServiceImpl implements IEmpleadoService {

    // Logback: logger para operaciones sobre empleados
    private static final Logger log = LoggerFactory.getLogger(EmpleadoServiceImpl.class);

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Override
    public Empleado buscarPorNombre(String nombre) {
        return empleadoRepository.findByNombre(nombre)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado: " + nombre));
    }

    @Override
    public List<Empleado> listarTodos() {
        return empleadoRepository.findAll();
    }

    @Override
    public Empleado guardar(Empleado empleado) {

        // Commons Lang StringUtils: normalizar el nombre antes de guardar
        // Ejemplo: "JUAN" o " juan " → "Juan"
        if (StringUtils.isNotBlank(empleado.getNombre())) {
            empleado.setNombre(StringUtils.capitalize(empleado.getNombre().trim().toLowerCase()));
        }

        // Commons Lang StringUtils: normalizar el apellido
        if (StringUtils.isNotBlank(empleado.getApellido())) {
            empleado.setApellido(StringUtils.capitalize(empleado.getApellido().trim().toLowerCase()));
        }

        Empleado guardado = empleadoRepository.save(empleado);

        // Logback: confirmar creación/actualización del empleado
        log.info("Empleado guardado | Nombre: '{}' | Rol: {}",
                guardado.getNombre(), guardado.getRol().getNombreRol());

        return guardado;
    }

    // ─────────────────────────────────────────────
    // Commons Lang RandomStringUtils: contraseña temporal
    // ─────────────────────────────────────────────

    /**
     * Genera una contraseña temporal alfanumérica de 8 caracteres
     * para nuevos empleados creados desde el panel de administración.
     * Ejemplo de salida: "aZ3kP9mQ"
     */
    public String generarContraseñaTemporal() {
        String temporal = RandomStringUtils.randomAlphanumeric(8);
        log.info("Contraseña temporal generada para nuevo empleado (no se loguea el valor por seguridad)");
        return temporal;
    }
}