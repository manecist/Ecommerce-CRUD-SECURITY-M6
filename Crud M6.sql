CREATE DATABASE crud_ecommerce_m6;

USE crud_ecommerce_m6;

-- Roles (Necesaria para los Usuarios)
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

-- Clientes (Ordenada según tu preferencia)
CREATE TABLE clientes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    apellido VARCHAR(255) NOT NULL,
    rut VARCHAR(20) NOT NULL UNIQUE, -- Usada como referencia para FKs
    fecha_nacimiento DATE NOT NULL,
    telefono VARCHAR(20),
    email VARCHAR(255)               
);

-- Usuarios (Conectada al Cliente por RUT)
CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    fecha_registro DATE DEFAULT (CURRENT_DATE),
    rol_id BIGINT,
    cliente_rut VARCHAR(20),
    CONSTRAINT fk_usuario_rol FOREIGN KEY (rol_id) REFERENCES roles(id),
    CONSTRAINT fk_usuario_cliente FOREIGN KEY (cliente_rut) REFERENCES clientes(rut) ON DELETE CASCADE
);

-- Direcciones (Conectada al Cliente por RUT)
CREATE TABLE direcciones_cliente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    direccion VARCHAR(255) NOT NULL,
    ciudad VARCHAR(100) NOT NULL,
    estado_region VARCHAR(100),
    pais VARCHAR(100) NOT NULL,
    codigo_postal VARCHAR(20),
    es_principal BOOLEAN DEFAULT FALSE,
    cliente_rut VARCHAR(20),
    CONSTRAINT fk_direccion_cliente FOREIGN KEY (cliente_rut) REFERENCES clientes(rut) ON DELETE CASCADE
);

-- Categorías 
CREATE TABLE categorias (
    idCategoria INT AUTO_INCREMENT PRIMARY KEY,
    nombreCategoria VARCHAR(100) NOT NULL,
    imagenBanner VARCHAR(255) DEFAULT 'default-banner.jpg'
);

-- Subcategorías 
CREATE TABLE subcategorias (
    idSubcategoria INT AUTO_INCREMENT PRIMARY KEY,
    idCategoriaAsociada INT NOT NULL,
    nombreSubcategoria VARCHAR(100) NOT NULL,
    CONSTRAINT fk_sub_cat FOREIGN KEY (idCategoriaAsociada) 
        REFERENCES categorias(idCategoria) ON DELETE CASCADE
);

-- Productos 
CREATE TABLE productos (
    idProducto INT AUTO_INCREMENT PRIMARY KEY,
    idSubcategoriaAsociada INT NOT NULL,
    nombreProducto VARCHAR(150) NOT NULL,
    descripcionProducto TEXT,
    precioProducto DOUBLE NOT NULL DEFAULT 0.0,
    stockProducto INT NOT NULL DEFAULT 0,
    imagenProducto VARCHAR(255) DEFAULT 'default-prod.jpg',
    CONSTRAINT fk_prod_sub FOREIGN KEY (idSubcategoriaAsociada) 
        REFERENCES subcategorias(idSubcategoria) ON DELETE RESTRICT
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