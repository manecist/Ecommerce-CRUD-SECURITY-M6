CREATE DATABASE crud_ecommerce_m6;

USE crud_ecommerce_m6;

-- ROLES
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255)
);

-- CLIENTES
CREATE TABLE clientes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rut VARCHAR(255) NOT NULL UNIQUE, 
    nombre VARCHAR(255) NOT NULL,
    apellido VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    telefono VARCHAR(255),
    fecha_nacimiento DATE NOT NULL
);

-- USUARIOS (Relación con Cliente por RUT)
CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    fecha_registro DATE,
    rol_id BIGINT,
    cliente_rut VARCHAR(255),
    CONSTRAINT fk_usuario_rol FOREIGN KEY (rol_id) REFERENCES roles(id),
    CONSTRAINT fk_usuario_cliente FOREIGN KEY (cliente_rut) REFERENCES clientes(rut) ON DELETE CASCADE ON UPDATE CASCADE
);

-- DIRECCIONES CLIENTE
CREATE TABLE direcciones_cliente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    direccion VARCHAR(255) NOT NULL,
    ciudad VARCHAR(255) NOT NULL,
    estado_region VARCHAR(255),
    pais VARCHAR(255) NOT NULL,
    codigo_postal VARCHAR(255),
    es_principal BOOLEAN DEFAULT FALSE,
    cliente_rut VARCHAR(255),
    CONSTRAINT fk_direccion_cliente FOREIGN KEY (cliente_rut) REFERENCES clientes(rut) ON DELETE CASCADE ON UPDATE CASCADE
);

-- CATEGORIAS
CREATE TABLE categorias (
    id_categoria BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_categoria VARCHAR(100) NOT NULL,
    imagen_banner VARCHAR(255) DEFAULT 'banner-simple.jpg'
);

-- SUBCATEGORIAS
CREATE TABLE subcategorias (
    id_subcategoria BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_subcategoria VARCHAR(100) NOT NULL,
    id_categoria BIGINT NOT NULL,
    CONSTRAINT fk_sub_cat FOREIGN KEY (id_categoria) REFERENCES categorias(id_categoria) ON DELETE CASCADE
);

-- PRODUCTOS
CREATE TABLE productos (
    id_producto BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_producto VARCHAR(150) NOT NULL,
    descripcion_producto TEXT,
    precio_producto DOUBLE NOT NULL DEFAULT 0.0,
    stock_producto INT NOT NULL DEFAULT 0,
    imagen_producto VARCHAR(255),
    id_subcategoria BIGINT NOT NULL,
    CONSTRAINT fk_prod_sub FOREIGN KEY (id_subcategoria) REFERENCES subcategorias(id_subcategoria) ON DELETE CASCADE
);

CREATE TABLE contactos (
    id_contacto BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    mensaje VARCHAR(1000) NOT NULL,
    fecha_envio DATETIME NOT NULL
);

-- Tabla para la entidad Suscriptor
CREATE TABLE suscriptores (
    id_suscriptor BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    fecha_suscripcion DATETIME NOT NULL
);


-- Insertamos los roles base para que no te de error el sistema
INSERT INTO roles (nombre) VALUES ('ROLE_ADMIN'), ('ROLE_CLIENT');

--  Ficha de Cliente
INSERT INTO clientes (nombre, apellido, rut, fecha_nacimiento, telefono, email) 
VALUES ('Administrador', 'Principal', '111111111', '1990-01-01', '+56900000000', 'admin@magical.cl');

--  Cuenta de Usuario (Contraseña: Admin.2026!)
INSERT INTO usuarios (email, password, fecha_registro, rol_id, cliente_rut) 
VALUES (
    'admin@magical.cl', 
    '$2a$10$D.A0AZmHgplJztrrS4PFcejqCmPQ7mPclgzZ8z4K26k0jdg.iUVKK', -- Hash real para Admin.2026!
	CURRENT_DATE, 
    1, 
    '111111111'
);


SELECT * FROM roles;

SELECT * FROM usuarios;

SELECT * FROM clientes;

SELECT * FROM direcciones_cliente;

SELECT * FROM categorias;

SELECT * FROM subcategorias;

SELECT * FROM productos;

SELECT * FROM contactos;

SELECT * FROM suscriptores;