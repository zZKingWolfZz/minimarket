-- Script para poblar la base de datos de MiniMarket con datos reales
-- Configurado para la base de datos: minimarket_yuly

USE minimarket_yuly;

-- Desactivar temporalmente revisión de claves foráneas para truncar tablas de manera segura
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE venta;
TRUNCATE TABLE stock;
TRUNCATE TABLE producto;
TRUNCATE TABLE categoria;
TRUNCATE TABLE cliente;
SET FOREIGN_KEY_CHECKS = 1;

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
