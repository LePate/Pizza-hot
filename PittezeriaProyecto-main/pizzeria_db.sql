-- Creación de la base de datos
CREATE DATABASE IF NOT EXISTS pizzeria_db;
USE pizzeria_db;

-- 1. Tabla: categoria
CREATE TABLE categoria (
    id_categoria INT AUTO_INCREMENT,
    nombre_categoria VARCHAR(50) NOT NULL,
    descripcion TEXT,
    PRIMARY KEY (id_categoria)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Tabla: producto
CREATE TABLE producto (
    id_producto INT AUTO_INCREMENT,
    nombre_producto VARCHAR(50) NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL,
    id_categoria INT NOT NULL,
    PRIMARY KEY (id_producto),
    FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria) 
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Tabla: rol
CREATE TABLE rol (
    id_rol INT AUTO_INCREMENT,
    nombre_rol VARCHAR(50) NOT NULL,
    descripcion TEXT,
    PRIMARY KEY (id_rol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Tabla: empleado
CREATE TABLE empleado (
    id_empleado INT AUTO_INCREMENT,
    nombre VARCHAR(50) NOT NULL,
    apellido VARCHAR(50) NOT NULL,
    contraseña VARCHAR(255) NOT NULL, -- Se amplió a 255 por seguridad para contraseñas cifradas (hash)
    telefono VARCHAR(20),
    id_rol INT NOT NULL,
    PRIMARY KEY (id_empleado),
    FOREIGN KEY (id_rol) REFERENCES rol(id_rol) 
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Tabla: venta
CREATE TABLE venta (
    id_venta INT AUTO_INCREMENT,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    tipo_entrega ENUM('Local', 'Delivery') NOT NULL, -- Valores lógicos para el ENUM de una pizzería
    id_empleado INT NOT NULL,
    PRIMARY KEY (id_venta),
    FOREIGN KEY (id_empleado) REFERENCES empleado(id_empleado) 
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Tabla: detalle_venta
CREATE TABLE detalle_venta (
    id_detalle INT AUTO_INCREMENT,
    cantidad INT NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    id_venta INT NOT NULL,
    id_producto INT NOT NULL,
    PRIMARY KEY (id_detalle),
    FOREIGN KEY (id_venta) REFERENCES venta(id_venta) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (id_producto) REFERENCES producto(id_producto) 
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
