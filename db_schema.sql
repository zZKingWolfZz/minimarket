-- Script de inicialización para la base de datos de MiniMarket
-- Ajustado exactamente al diagrama de base de datos provisto

CREATE DATABASE IF NOT EXISTS minimarket_yuly;
USE minimarket_yuly;

-- Eliminar tablas en orden inverso para evitar conflictos de claves foráneas
DROP TABLE IF EXISTS venta;
DROP TABLE IF EXISTS stock;
DROP TABLE IF EXISTS producto;
DROP TABLE IF EXISTS categoria;
DROP TABLE IF EXISTS cliente;
DROP TABLE IF EXISTS usuario_rol;
DROP TABLE IF EXISTS usuario;
DROP TABLE IF EXISTS rol;

-- 1. Crear tabla: rol
CREATE TABLE rol (
    Id_rol INT PRIMARY KEY,
    nombre_rol VARCHAR(50) NOT NULL
);

-- 2. Crear tabla: usuario
CREATE TABLE usuario (
    Id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    estado TINYINT(1) DEFAULT 1,
    nombre VARCHAR(100),
    apellido_paterno VARCHAR(100),
    apellido_materno VARCHAR(100)
);

-- 3. Crear tabla intermedia: usuario_rol
CREATE TABLE usuario_rol (
    Id_usuario INT NOT NULL,
    Id_rol INT NOT NULL,
    PRIMARY KEY (Id_usuario, Id_rol),
    FOREIGN KEY (Id_usuario) REFERENCES usuario(Id_usuario) ON DELETE CASCADE,
    FOREIGN KEY (Id_rol) REFERENCES rol(Id_rol) ON DELETE CASCADE
);

-- 4. Crear tabla: categoria
CREATE TABLE categoria (
    Id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre_categoria VARCHAR(100) NOT NULL
);

-- 5. Crear tabla: producto
CREATE TABLE producto (
    Id_producto INT AUTO_INCREMENT PRIMARY KEY,
    nombre_Producto VARCHAR(100) NOT NULL,
    Precio_unitario DECIMAL(10, 2) NOT NULL,
    Id_categoria INT NOT NULL,
    codigo_barras VARCHAR(50),
    FOREIGN KEY (Id_categoria) REFERENCES categoria(Id_categoria) ON DELETE RESTRICT
);

-- 6. Crear tabla: stock
CREATE TABLE stock (
    Id_stock INT AUTO_INCREMENT PRIMARY KEY,
    Cantidad INT NOT NULL DEFAULT 0,
    Id_Producto INT NOT NULL UNIQUE,
    FOREIGN KEY (Id_Producto) REFERENCES producto(Id_producto) ON DELETE CASCADE
);

-- 7. Crear tabla: cliente
CREATE TABLE cliente (
    Id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido_paterno VARCHAR(100) NOT NULL,
    apellido_materno VARCHAR(100) NOT NULL,
    DNI_RUC VARCHAR(20) NOT NULL UNIQUE
);

-- 8. Crear tabla: venta
CREATE TABLE venta (
    Id_venta INT AUTO_INCREMENT PRIMARY KEY,
    Id_producto INT NOT NULL,
    cantidad INT NOT NULL,
    Precio_total DECIMAL(10, 2) NOT NULL,
    Fecha DATE NOT NULL,
    Id_cliente INT NOT NULL,
    FOREIGN KEY (Id_producto) REFERENCES producto(Id_producto) ON DELETE RESTRICT,
    FOREIGN KEY (Id_cliente) REFERENCES cliente(Id_cliente) ON DELETE RESTRICT
);

-- ==========================================
-- INSERTAR DATOS POR DEFECTO PARA EL INICIO
-- ==========================================

-- Insertar roles
INSERT INTO rol (Id_rol, nombre_rol) VALUES 
(1, 'Administrador'),
(2, 'Vendedor');

-- Insertar categorías iniciales
INSERT INTO categoria (nombre_categoria) VALUES 
('Abarrotes'),
('Bebidas'),
('Lácteos'),
('Limpieza'),
('Cuidado Personal'),
('Snacks y Golosinas'),
('Panadería');
