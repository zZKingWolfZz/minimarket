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

-- Insertar usuarios por defecto (Administrador y Vendedor)
-- Las contraseñas están encriptadas usando la función SHA2 de MySQL (SHA-256)

-- 1. Administrador (Usuario: admin, Contraseña: admin)
INSERT INTO usuario (Id_usuario, username, password, nombre, apellido_paterno, apellido_materno, estado) VALUES 
(1, 'admin', SHA2('admin', 256), 'Administrador', 'Sistema', '', 1);

-- 2. Vendedor (Usuario: vendedor, Contraseña: vendedor)
INSERT INTO usuario (Id_usuario, username, password, nombre, apellido_paterno, apellido_materno, estado) VALUES 
(2, 'vendedor', SHA2('vendedor', 256), 'Vendedor', 'Minimarket', '', 1);

-- Asignar roles a los usuarios
-- admin tiene rol de Administrador (Id_rol = 1)
INSERT INTO usuario_rol (Id_usuario, Id_rol) VALUES (1, 1);

-- vendedor tiene rol de Vendedor (Id_rol = 2)
INSERT INTO usuario_rol (Id_usuario, Id_rol) VALUES (2, 2);

-- 1. Insertar Categorías del Minimarket
INSERT INTO categoria (Id_categoria, nombre_categoria) VALUES 
(1, 'Abarrotes'),
(2, 'Bebidas'),
(3, 'Lácteos'),
(4, 'Limpieza'),
(5, 'Cuidado Personal'),
(6, 'Snacks y Golosinas'),
(7, 'Panadería');

-- 2. Insertar Productos de Consumo Cotidiano con Códigos de Barras Reales (EAN-13)

-- Categoría: Abarrotes (Id_categoria = 1)
INSERT INTO producto (Id_producto, nombre_Producto, Precio_unitario, Id_categoria, codigo_barras) VALUES 
(1, 'Arroz Costeño Extra 1kg', 4.70, 1, '7750172000075'),
(2, 'Aceite Vegetal Primor Premium 1L', 9.80, 1, '7750243000676'),
(3, 'Azúcar Rubia Cartavio 1kg', 4.20, 1, '7751036000045'),
(4, 'Fideos Tallarín Don Vittorio 1kg', 4.80, 1, '7750243000102'),
(5, 'Atún en Trozos de Jurel Campomar 170g', 5.50, 1, '7750730000062');

-- Categoría: Bebidas (Id_categoria = 2)
INSERT INTO producto (Id_producto, nombre_Producto, Precio_unitario, Id_categoria, codigo_barras) VALUES 
(6, 'Gaseosa Coca Cola Sin Azúcar 1.5L', 6.50, 2, '7861001804712'),
(7, 'Gaseosa Inca Kola Original 1.5L', 6.20, 2, '7861001800073'),
(8, 'Agua Mineral San Mateo Sin Gas 600ml', 2.00, 2, '7750106001094'),
(9, 'Cerveza Pilsen Callao Lata 355ml', 4.50, 2, '7750066000358'),
(10, 'Jugo Frugos del Valle Durazno 1L', 4.50, 2, '7861001822037');

-- Categoría: Lácteos (Id_categoria = 3)
INSERT INTO producto (Id_producto, nombre_Producto, Precio_unitario, Id_categoria, codigo_barras) VALUES 
(11, 'Leche Evaporada Gloria Azul 400g', 4.30, 3, '7750090000881'),
(12, 'Yogurt Gloria de Fresa 1kg', 6.80, 3, '7750090001710'),
(13, 'Mantequilla con Sal Gloria 200g', 7.50, 3, '7750090001604'),
(14, 'Queso Edam Gloria Rodajas 200g', 10.90, 3, '7750090003004');

-- Categoría: Limpieza (Id_categoria = 4)
INSERT INTO producto (Id_producto, nombre_Producto, Precio_unitario, Id_categoria, codigo_barras) VALUES 
(15, 'Detergente Bolívar Activo Flores 800g', 8.20, 4, '7750243026027'),
(16, 'Jabón Líquido Aval Aloe Vera 400ml', 5.90, 4, '7750529000965'),
(17, 'Lava Vajillas Líquido Ayudín Limón 650ml', 6.20, 4, '7750808000628'),
(18, 'Limpiador Líquido Poett Bebé 900ml', 5.80, 4, '7750808001021');

-- Categoría: Cuidado Personal (Id_categoria = 5)
INSERT INTO producto (Id_producto, nombre_Producto, Precio_unitario, Id_categoria, codigo_barras) VALUES 
(19, 'Pasta Dental Colgate Triple Acción 150g', 5.50, 5, '7501035911475'),
(20, 'Champú Head & Shoulders Limpieza Renovadora 375ml', 16.90, 5, '7501006721522'),
(21, 'Jabón de Tocador Nivea Creme Care 3x90g', 8.90, 5, '4005900299661'),
(22, 'Desodorante Rexona Clinical Men 48g', 15.50, 5, '7791290791054');

-- Categoría: Snacks y Golosinas (Id_categoria = 6)
INSERT INTO producto (Id_producto, nombre_Producto, Precio_unitario, Id_categoria, codigo_barras) VALUES 
(23, 'Papas Lays Clásicas Familiares 160g', 7.50, 6, '7750134000353'),
(24, 'Galletas Casino Menta Paquete x6', 4.50, 6, '7750134005082'),
(25, 'Chocolate Sublime Extremo 50g', 2.50, 6, '7750001002577'),
(26, 'Tortees Picantes Inka Crops 150g', 5.20, 6, '7750267000140');

-- Categoría: Panadería (Id_categoria = 7)
INSERT INTO producto (Id_producto, nombre_Producto, Precio_unitario, Id_categoria, codigo_barras) VALUES 
(27, 'Pan de Molde Blanco Bimbo Grande 480g', 8.50, 7, '7501030424505'),
(28, 'Tostadas Bimbo Clásicas 210g', 5.20, 7, '7501000111206');

-- 3. Insertar Stock Inicial para los productos
INSERT INTO stock (Cantidad, Id_Producto) VALUES 
(80, 1),
(60, 2),
(120, 3),
(90, 4),
(45, 5),
(150, 6),
(150, 7),
(200, 8),
(72, 9),
(110, 10),
(130, 11),
(55, 12),
(40, 13),
(35, 14),
(50, 15),
(65, 16),
(85, 17),
(90, 18),
(105, 19),
(40, 20),
(60, 21),
(35, 22),
(90, 23),
(140, 24),
(180, 25),
(75, 26),
(45, 27),
(50, 28);

-- 4. Insertar Clientes iniciales
INSERT INTO cliente (nombre, apellido_paterno, apellido_materno, DNI_RUC) VALUES 
('Cliente', 'General', '', '00000000'),
('Juan', 'Pérez', 'Pérez', '12345678'),
('María', 'Gómez', 'Rodríguez', '87654321'),
('Carlos', 'Sánchez', 'López', '45678901');
