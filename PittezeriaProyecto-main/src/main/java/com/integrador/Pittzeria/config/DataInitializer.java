package com.integrador.Pittzeria.config;

import com.integrador.Pittzeria.model.entity.Categoria;
import com.integrador.Pittzeria.model.entity.Empleado;
import com.integrador.Pittzeria.model.entity.Rol;
import com.integrador.Pittzeria.repository.CategoriaRepository;
import com.integrador.Pittzeria.repository.EmpleadoRepository;
import com.integrador.Pittzeria.repository.RolRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    // Logback: logger para el arranque del sistema
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {

        // -1. Modificar columna tipo_entrega de la tabla venta para soportar métodos de pago en vez del ENUM restrictivo
        try {
            jdbcTemplate.execute("ALTER TABLE venta MODIFY COLUMN tipo_entrega VARCHAR(50) NOT NULL");
            log.info("Columna tipo_entrega modificada de ENUM a VARCHAR(50) con éxito.");
        } catch (Exception e) {
            log.info("Nota: No se modificó la columna tipo_entrega (probablemente ya es VARCHAR(50)): {}", e.getMessage());
        }

        // 0. Insertar las Categorías si no existen en la base de datos
        if (categoriaRepository.count() == 0) {
            Categoria pizzasCat = new Categoria();
            pizzasCat.setNombreCategoria("Pizzas");
            pizzasCat.setDescripcion("Pizzas tradicionales y gourmet");
            categoriaRepository.save(pizzasCat);

            Categoria bebidasCat = new Categoria();
            bebidasCat.setNombreCategoria("Bebidas");
            bebidasCat.setDescripcion("Refrescos, gaseosas y bebidas frías");
            categoriaRepository.save(bebidasCat);

            Categoria combosCat = new Categoria();
            combosCat.setNombreCategoria("Combos");
            combosCat.setDescripcion("Combos y promociones de la pizzería");
            categoriaRepository.save(combosCat);

            log.info("Categorías iniciales creadas: Pizzas, Bebidas, Combos");
        }

        // 1. Insertar los Roles si no existen en la base de datos
        if (rolRepository.count() == 0) {
            Rol adminRol = new Rol();
            adminRol.setNombreRol("ADMINISTRADOR");
            rolRepository.save(adminRol);

            Rol cajeroRol = new Rol();
            cajeroRol.setNombreRol("CAJERO");
            rolRepository.save(cajeroRol);

            log.info("Roles iniciales creados: ADMINISTRADOR, CAJERO");
        }

        // 2. Insertar los Empleados de prueba con claves ENCRIPTADAS
        if (empleadoRepository.count() == 0) {

            Rol adminRol  = rolRepository.findByNombreRol("ADMINISTRADOR");
            Rol cajeroRol = rolRepository.findByNombreRol("CAJERO");

            // ── Crear el usuario Administrador ───────────────────────
            Empleado admin = new Empleado();
            // Commons Lang StringUtils: capitalizar nombre antes de guardar
            admin.setNombre(StringUtils.capitalize("admin"));
            admin.setContraseña(passwordEncoder.encode("admin123"));
            admin.setRol(adminRol);
            admin.setApellido(StringUtils.capitalize("chavez"));
            admin.setTelefono("98456321");
            empleadoRepository.save(admin);

            log.info("Empleado creado | Nombre: '{}' | Rol: ADMINISTRADOR", admin.getNombre());

            // ── Crear el usuario Cajero ───────────────────────────────
            Empleado cajero = new Empleado();
            cajero.setNombre(StringUtils.capitalize("cajero"));
            cajero.setContraseña(passwordEncoder.encode("cajero123"));
            cajero.setRol(cajeroRol);
            cajero.setApellido(StringUtils.capitalize("Gomez"));
            cajero.setTelefono("912345678");
            empleadoRepository.save(cajero);

            log.info("Empleado creado | Nombre: '{}' | Rol: CAJERO", cajero.getNombre());

            // ── Commons Lang RandomStringUtils ───────────────────────
            // Genera una contraseña temporal de 8 caracteres alfanuméricos
            // para mostrar/enviar al nuevo empleado en entornos reales.
            // En desarrollo solo se loguea como referencia.
            String contrasenaTemporal = RandomStringUtils.randomAlphanumeric(8);
            log.info("[ Pittzeria ] Usuarios iniciales creados correctamente en la base de datos.");
            log.debug("Contraseña temporal de ejemplo generada (solo desarrollo): {}", contrasenaTemporal);
        }
    }
}