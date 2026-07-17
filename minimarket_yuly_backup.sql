-- MySQL dump 10.13  Distrib 8.4.9, for Win64 (x86_64)
--
-- Host: localhost    Database: minimarket_yuly
-- ------------------------------------------------------
-- Server version	8.4.9

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `minimarket_yuly`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `minimarket_yuly` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `minimarket_yuly`;

--
-- Table structure for table `categoria`
--

DROP TABLE IF EXISTS `categoria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categoria` (
  `Id_categoria` int NOT NULL AUTO_INCREMENT,
  `nombre_categoria` varchar(100) NOT NULL,
  PRIMARY KEY (`Id_categoria`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categoria`
--

LOCK TABLES `categoria` WRITE;
/*!40000 ALTER TABLE `categoria` DISABLE KEYS */;
INSERT INTO `categoria` VALUES (1,'Abarrotes'),(2,'Bebidas'),(3,'Lácteos'),(4,'Limpieza'),(5,'Cuidado Personal'),(6,'Snacks y Golosinas'),(7,'Panadería');
/*!40000 ALTER TABLE `categoria` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cliente`
--

DROP TABLE IF EXISTS `cliente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cliente` (
  `Id_cliente` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `apellido_paterno` varchar(100) NOT NULL,
  `apellido_materno` varchar(100) NOT NULL,
  `DNI_RUC` varchar(20) NOT NULL,
  PRIMARY KEY (`Id_cliente`),
  UNIQUE KEY `DNI_RUC` (`DNI_RUC`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cliente`
--

LOCK TABLES `cliente` WRITE;
/*!40000 ALTER TABLE `cliente` DISABLE KEYS */;
INSERT INTO `cliente` VALUES (1,'Cliente','General','','00000000'),(2,'Juan','Pérez','Pérez','12345678'),(3,'María','Gómez','Rodríguez','87654321'),(4,'Carlos','Sánchez','López','45678901');
/*!40000 ALTER TABLE `cliente` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `producto`
--

DROP TABLE IF EXISTS `producto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `producto` (
  `Id_producto` int NOT NULL AUTO_INCREMENT,
  `nombre_Producto` varchar(100) NOT NULL,
  `Precio_unitario` decimal(10,2) NOT NULL,
  `Id_categoria` int NOT NULL,
  `codigo_barras` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`Id_producto`),
  KEY `Id_categoria` (`Id_categoria`),
  CONSTRAINT `producto_ibfk_1` FOREIGN KEY (`Id_categoria`) REFERENCES `categoria` (`Id_categoria`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=151 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `producto`
--

LOCK TABLES `producto` WRITE;
/*!40000 ALTER TABLE `producto` DISABLE KEYS */;
INSERT INTO `producto` VALUES (1,'Arroz Costeño Extra 1kg',4.70,1,'7750172000075'),(2,'Aceite Vegetal Primor Premium 1L',9.80,1,'7750243000676'),(3,'Azúcar Rubia Cartavio 1kg',4.20,1,'7751036000045'),(4,'Fideos Tallarín Don Vittorio 1kg',4.80,1,'7750243000102'),(5,'Atún en Trozos de Jurel Campomar 170g',5.50,1,'7750730000062'),(6,'Fideos Canuto Molitalia 500g',3.20,1,'7750106000042'),(7,'Salsa de Tomate Pomarola Molitalia 150g',2.10,1,'7750106002954'),(8,'Mayonesa Alacena Receta Casera Doypack 190g',5.50,1,'7750243002670'),(9,'Ketchup Alacena Doypack 190g',4.80,1,'7750243002687'),(10,'Salsa de Ají Alacena Doypack 85g',2.50,1,'7750243004056'),(11,'Sal Yodada Emsal Bolsa 1kg',1.80,1,'7750878000030'),(12,'Arroz Costeño Integración Familiar 5kg',21.50,1,'7750172000211'),(13,'Frijol Canario Costeño Bolsa 500g',5.80,1,'7750172000556'),(14,'Garbanzo Costeño Bolsa 500g',5.20,1,'7750172000570'),(15,'Avena Tres Ositos Familiar Bolsa 135g',1.80,1,'7750529000125'),(16,'Harina Preparada Blanca Flor Bolsa 1kg',6.90,1,'7750243000188'),(17,'Harina Sin Preparar Blanca Flor Bolsa 1kg',6.20,1,'7750243000195'),(18,'Vinagre Blanco Primor Botella 500ml',2.50,1,'7750243001802'),(19,'Sillao Kikko Botella 250ml',3.50,1,'7750190000071'),(20,'Sazonador Ajinomoto Bolsa 100g',3.80,1,'7896510600570'),(21,'Café Instantáneo Nescafé Tradición 200g',18.50,1,'7613035848248'),(22,'Café Instantáneo Kirma Frasco 190g',14.50,1,'7613035613310'),(23,'Trozos de Atún Real en Aceite 170g',5.20,1,'7861025700014'),(24,'Salsa de Ocopa Alacena Doypack 85g',2.80,1,'7750243005879'),(25,'Salsa Huancaina Alacena Doypack 85g',2.80,1,'7750243005862'),(26,'Gaseosa Coca Cola Sin Azúcar 1.5L',6.50,2,'7861001804712'),(27,'Gaseosa Inca Kola Original 1.5L',6.20,2,'7861001800073'),(28,'Agua Mineral San Mateo Sin Gas 600ml',2.00,2,'7750106001094'),(29,'Cerveza Pilsen Callao Lata 355ml',4.50,2,'7750066000358'),(30,'Jugo Frugos del Valle Durazno 1L',4.50,2,'7861001822037'),(31,'Gaseosa Inca Kola Original Botella 3L',11.50,2,'7750001002348'),(32,'Gaseosa Coca Cola Sabor Original Botella 3L',11.80,2,'7750001002249'),(33,'Gaseosa Inca Kola Original Botella 500ml',2.50,2,'7750001001150'),(34,'Gaseosa Coca Cola Sabor Original Botella 500ml',2.60,2,'7750001001235'),(35,'Agua Mineral Cielo Sin Gas Botella 625ml',1.50,2,'7750739002074'),(36,'Agua Mineral San Luis Sin Gas Botella 1L',2.20,2,'7750001002737'),(37,'Bebida Isotónica Sporade Mandarina Botella 500ml',2.20,2,'7750739002227'),(38,'Bebida Energizante Volt Ginseng Blue Botella 300ml',2.50,2,'7750739007420'),(39,'Cerveza Pilsen Callao Botella Retornable 630ml',6.00,2,'7750066000150'),(40,'Cerveza Cristal Botella Retornable 650ml',5.80,2,'7750066000105'),(41,'Té Negro Mc Colin\'s Caja 100 Sobres',7.50,2,'7750198000578'),(42,'Infusión Manzanilla Mc Colin\'s Caja 100 Sobres',8.00,2,'7750198000592'),(43,'Gaseosa Pepsi Sabor Original Botella 1.5L',5.20,2,'7750120003078'),(44,'Gaseosa Fanta Naranja Botella 1.5L',5.50,2,'7861001800530'),(45,'Gaseosa Sprite Limón Botella 1.5L',5.50,2,'7861001800639'),(46,'Cerveza Cusqueña Dorada Botella 620ml',7.20,2,'7750066000518'),(47,'Bebida Energizante Red Bull Lata 250ml',7.90,2,'9002490100070'),(48,'Agua Mineral Cielo Sin Gas Bidón 7L',7.50,2,'7750739002135'),(49,'Jugo Frugos del Valle Naranja 1L',4.50,2,'7861001822013'),(50,'Jugo en Caja Pulp Durazno 150ml',1.20,2,'7750739004504'),(51,'Leche Evaporada Gloria Azul 400g',4.30,3,'7750090000881'),(52,'Yogurt Gloria de Fresa 1kg',6.80,3,'7750090001710'),(53,'Mantequilla con Sal Gloria 200g',7.50,3,'7750090001604'),(54,'Queso Edam Gloria Rodajas 200g',10.90,3,'7750090003004'),(55,'Leche Evaporada Gloria Light Lata 400g',4.50,3,'7750090000959'),(56,'Leche Evaporada Gloria Sin Lactosa Lata 400g',4.70,3,'7750090002724'),(57,'Leche UHT Entera Gloria Caja 1L',5.50,3,'7750090003073'),(58,'Yogurt Laive Fresa Botella 1L',6.50,3,'7750190003461'),(59,'Yogurt Laive Vainilla French Botella 1L',6.50,3,'7750190003485'),(60,'Mantequilla con Sal Laive Pote 200g',7.20,3,'7750190000163'),(61,'Queso Crema Philadelphia Clásico Caja 190g',8.90,3,'7622210719875'),(62,'Leche Evaporada Ideal Cremosita Lata 395g',4.00,3,'7750106003265'),(63,'Leche Evaporada Ideal Amanecer Lata 395g',3.60,3,'7750106003241'),(64,'Yogurt Gloria Griego Natural Pote 120g',2.80,3,'7750090004209'),(65,'Margarina Manty Pote 200g',3.80,3,'7750243000621'),(66,'Queso Mozzarella Bonlé Rallado Bolsa 200g',9.50,3,'7750090002878'),(67,'Jamonada Especial San Fernando Paquete 150g',5.80,3,'7750570000303'),(68,'Hot Dog de Pollo San Fernando Paquete 250g',6.50,3,'7750570000259'),(69,'Salchicha Huachana La Segoviana Bolsa 250g',7.50,3,'7750570002048'),(70,'Tocino Ahumado San Fernando Paquete 150g',9.20,3,'7750570000457'),(71,'Detergente Bolívar Activo Flores 800g',8.20,4,'7750243026027'),(72,'Jabón Líquido Aval Aloe Vera 400ml',5.90,4,'7750529000965'),(73,'Lava Vajillas Líquido Ayudín Limón 650ml',6.20,4,'7750808000628'),(74,'Limpiador Líquido Poett Bebé 900ml',5.80,4,'7750808001021'),(75,'Jabón Bolívar Limón Barra 350g',3.50,4,'7750243003080'),(76,'Detergente Opal Fuerza Activa Bolsa 800g',7.90,4,'7750243034084'),(77,'Lejía Clorox Original Botella 930g',4.20,4,'7750808000574'),(78,'Limpiador Multiuso Sapolio Lavanda Botella 900ml',5.20,4,'7750243026362'),(79,'Lustramuebles Sapolio Limón Aerosol 360ml',9.50,4,'7750106002732'),(80,'Papel Higiénico Suave Gold Doble Hoja Paquete x4',5.80,4,'7750310001858'),(81,'Papel Toalla Elite Clásica Paquete x2',4.80,4,'7750116002573'),(82,'Suavizante Downy Concentrado Romance Botella 800ml',14.50,4,'7501006560831'),(83,'Lava Vajillas Pasta Ayudín Limón Pote 350g',3.80,4,'7750808000307'),(84,'Limpia Vidrios Sapolio Repuesto Doypack 500ml',3.50,4,'7750106002015'),(85,'Insecticida Sapolio Mata Moscas y Mosquitos 360ml',8.50,4,'7750106002626'),(86,'Detergente Ariel Líquido Concentrado Botella 800ml',16.50,4,'7500435133609'),(87,'Desinfectante Sapolio Pino Botella 900ml',4.80,4,'7750106003739'),(88,'Papel Higiénico Elite Doble Hoja Paquete x4',5.50,4,'7750116000210'),(89,'Jabón de Lavar Ropa Pepita Barra 350g',3.00,4,'7750106001223'),(90,'Esponja Multiuso Scotch-Brite Paquete x1',1.80,4,'7702008000208'),(91,'Pasta Dental Colgate Triple Acción 150g',5.50,5,'7501035911475'),(92,'Champú Head & Shoulders Limpieza Renovadora 375ml',16.90,5,'7501006721522'),(93,'Jabón de Tocador Nivea Creme Care 3x90g',8.90,5,'4005900299661'),(94,'Desodorante Rexona Clinical Men 48g',15.50,5,'7791290791054'),(95,'Champú Sedal Ceramidas Botella 340ml',12.90,5,'7702006263593'),(96,'Champú Pantene Control Caída Botella 400ml',18.50,5,'7501006721119'),(97,'Jabón de Tocador Protex Avena Barra 110g',3.20,5,'7501035911901'),(98,'Pasta Dental Kolynos Triple Limpieza Tubo 90g',3.50,5,'7501035911666'),(99,'Desodorante Barra Old Spice Leña 50g',14.50,5,'7501006727289'),(100,'Toallas Higiénicas Nosotras Normal con Alas Paquete x10',4.50,5,'7751122002342'),(101,'Toallitas Húmedas Huggies Limpieza Efectiva Paquete x48',6.80,5,'7750310004552'),(102,'Pasta Dental Dento Triple Acción Tubo 90g',2.80,5,'7750529002228'),(103,'Champú Savital Biotina Botella 530ml',14.90,5,'7702006297055'),(104,'Enjuague Bucal Colgate Plax Mentol Botella 250ml',9.80,5,'7501035911451'),(105,'Jabón de Tocador Dove Original Barra 90g',4.50,5,'7891150005722'),(106,'Máquina de Afeitar Gillette Prestobarba 3 Paquete x2',8.50,5,'7500435192453'),(107,'Crema Corporal Nivea Milk Nutritiva Botella 250ml',15.80,5,'4005808701989'),(108,'Pañales Huggies Active Sec Talla G Paquete x30',32.50,5,'7750310008543'),(109,'Desodorante Aerosol Rexona Odorono 150ml',10.50,5,'7791290790170'),(110,'Alcohol Gel Antibacterial Aval Botella 380ml',6.50,5,'7750529000989'),(111,'Papas Lays Clásicas Familiares 160g',7.50,6,'7750134000353'),(112,'Galletas Casino Menta Paquete x6',4.50,6,'7750134005082'),(113,'Chocolate Sublime Extremo 50g',2.50,6,'7750001002577'),(114,'Tortees Picantes Inka Crops 150g',5.20,6,'7750267000140'),(115,'Galletas Soda Field Paquete x6',3.50,6,'7750193501041'),(116,'Galletas Oreo Regular Paquete x6',4.20,6,'7622300744657'),(117,'Galletas Rellenitas Fresa Field Paquete x6',3.80,6,'7750193001870'),(118,'Chocolate Cua Cua Field Barra 18g',1.20,6,'7750193003058'),(119,'Chocolate Doña Pepa Field Barra 23g',1.30,6,'7750193003065'),(120,'Wafer Sublime Chocolate Barra 30g',1.80,6,'7750001026047'),(121,'Caramelos masticables Skittles Original Bolsa 61.5g',3.20,6,'022000016005'),(122,'Bombones Beso de Moza Ambrosoli Fresa Caja x9',12.00,6,'7750106001148'),(123,'Papas Lay\'s Clásicas Bolsa 35g',1.80,6,'7750134000315'),(124,'Doritos Queso Atrevido Bolsa 42g',1.80,6,'7750134000469'),(125,'Snack Cheese Tris Familiar Bolsa 150g',4.50,6,'7750134001091'),(126,'Chocman Costa Bizcocho Bolsa x6',5.40,6,'7801930005744'),(127,'Galletas Chomp Naranja Paquete x6',4.00,6,'7750193102430'),(128,'Galletas Pícaras Chocolate Paquete x6',4.80,6,'7750193001047'),(129,'Chocolate Princesa Barra 30g',2.00,6,'7750001002591'),(130,'Caramelos Halls Mentol Bolsa 100 unidades',8.50,6,'7622300862085'),(131,'Pringles Original Tarro 124g',9.50,6,'038000138415'),(132,'Snack Cuates Picante Bolsa 45g',1.50,6,'7750134000155'),(133,'Gomitas Ambrosoli Ambrosito Bolsa 100g',3.20,6,'7750106003227'),(134,'Galletas Coronita Vainilla Paquete x6',4.20,6,'7750193001856'),(135,'Habas Tostadas Saladas Inka Crops Bolsa 100g',4.50,6,'7750267000041'),(136,'Pan de Molde Blanco Bimbo Grande 480g',8.50,7,'7501030424505'),(137,'Tostadas Bimbo Clásicas 210g',5.20,7,'7501000111206'),(138,'Queque Bimbo Sabor Vainilla Bolsa 250g',6.50,7,'7501030462002'),(139,'Panetón D\'Onofrio Caja 900g',28.50,7,'7750001004120'),(140,'Panetón Todinno Bolsa 900g',24.90,7,'7750702000038'),(141,'Panetón Gloria Caja 900g',26.50,7,'7750090003875'),(142,'Mermelada de Fresa Gloria Pote 320g',5.50,7,'7750090001116'),(143,'Mermelada de Fresa Fanny Frasco 310g',5.20,7,'7750243000577'),(144,'Cereal Ángel Copix Bolsa 150g',4.80,7,'7750529000033'),(145,'Cereal Ángel Zucaritas Bolsa 150g',4.80,7,'7750529000019'),(146,'Pan de Molde Integral Bimbo Grande 480g',9.20,7,'7501030424604'),(147,'Tostadas Integrales San Jorge Paquete x6',4.50,7,'7750058000458'),(148,'Bizcocho Chancay Unión Bolsa x6',6.00,7,'7751122002014'),(149,'Cereal Kellogg\'s Zucaritas Caja 300g',12.50,7,'7501008023648'),(150,'Panetón Sayón Bolsa 900g',19.90,7,'7750001004144');
/*!40000 ALTER TABLE `producto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rol`
--

DROP TABLE IF EXISTS `rol`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rol` (
  `Id_rol` int NOT NULL,
  `nombre_rol` varchar(50) NOT NULL,
  PRIMARY KEY (`Id_rol`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rol`
--

LOCK TABLES `rol` WRITE;
/*!40000 ALTER TABLE `rol` DISABLE KEYS */;
INSERT INTO `rol` VALUES (1,'Administrador'),(2,'Vendedor');
/*!40000 ALTER TABLE `rol` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stock`
--

DROP TABLE IF EXISTS `stock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock` (
  `Id_stock` int NOT NULL AUTO_INCREMENT,
  `Cantidad` int NOT NULL DEFAULT '0',
  `Id_Producto` int NOT NULL,
  PRIMARY KEY (`Id_stock`),
  UNIQUE KEY `Id_Producto` (`Id_Producto`),
  CONSTRAINT `stock_ibfk_1` FOREIGN KEY (`Id_Producto`) REFERENCES `producto` (`Id_producto`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=151 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stock`
--

LOCK TABLES `stock` WRITE;
/*!40000 ALTER TABLE `stock` DISABLE KEYS */;
INSERT INTO `stock` VALUES (1,80,1),(2,60,2),(3,120,3),(4,90,4),(5,45,5),(6,110,6),(7,85,7),(8,95,8),(9,70,9),(10,65,10),(11,200,11),(12,40,12),(13,55,13),(14,60,14),(15,130,15),(16,75,16),(17,80,17),(18,90,18),(19,100,19),(20,150,20),(21,50,21),(22,45,22),(23,85,23),(24,90,24),(25,95,25),(26,150,26),(27,150,27),(28,200,28),(29,72,29),(30,110,30),(31,80,31),(32,75,32),(33,120,33),(34,115,34),(35,250,35),(36,180,36),(37,140,37),(38,160,38),(39,90,39),(40,95,40),(41,100,41),(42,110,42),(43,85,43),(44,90,44),(45,95,45),(46,70,46),(47,60,47),(48,50,48),(49,120,49),(50,140,50),(51,130,51),(52,55,52),(53,40,53),(54,35,54),(55,90,55),(56,85,56),(57,70,57),(58,65,58),(59,60,59),(60,50,60),(61,45,61),(62,80,62),(63,75,63),(64,110,64),(65,120,65),(66,40,66),(67,65,67),(68,80,68),(69,70,69),(70,50,70),(71,50,71),(72,65,72),(73,85,73),(74,90,74),(75,75,75),(76,80,76),(77,95,77),(78,70,78),(79,40,79),(80,110,80),(81,90,81),(82,35,82),(83,60,83),(84,80,84),(85,45,85),(86,30,86),(87,55,87),(88,75,88),(89,85,89),(90,120,90),(91,105,91),(92,40,92),(93,60,93),(94,35,94),(95,55,95),(96,45,96),(97,80,97),(98,90,98),(99,40,99),(100,70,100),(101,50,101),(102,85,102),(103,40,103),(104,45,104),(105,65,105),(106,30,106),(107,25,107),(108,15,108),(109,35,109),(110,80,110),(111,90,111),(112,140,112),(113,180,113),(114,75,114),(115,150,115),(116,160,116),(117,140,117),(118,220,118),(119,240,119),(120,200,120),(121,130,121),(122,45,122),(123,180,123),(124,190,124),(125,120,125),(126,85,126),(127,110,127),(128,130,128),(129,100,129),(130,70,130),(131,55,131),(132,140,132),(133,150,133),(134,120,134),(135,90,135),(136,45,136),(137,50,137),(138,30,138),(139,40,139),(140,35,140),(141,30,141),(142,40,142),(143,45,143),(144,50,144),(145,50,145),(146,25,146),(147,35,147),(148,40,148),(149,20,149),(150,30,150);
/*!40000 ALTER TABLE `stock` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `Id_usuario` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `estado` tinyint(1) DEFAULT '1',
  `nombre` varchar(100) DEFAULT NULL,
  `apellido_paterno` varchar(100) DEFAULT NULL,
  `apellido_materno` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`Id_usuario`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` VALUES (1,'admin','8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918',1,'Administrador','Sistema',''),(2,'vendedor','e8827f3c0bcc90509b7d6841d446b163a671cac807a5f1bf41218667546ce80b',1,'Vendedor','Minimarket','');
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario_rol`
--

DROP TABLE IF EXISTS `usuario_rol`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario_rol` (
  `Id_usuario` int NOT NULL,
  `Id_rol` int NOT NULL,
  PRIMARY KEY (`Id_usuario`,`Id_rol`),
  KEY `Id_rol` (`Id_rol`),
  CONSTRAINT `usuario_rol_ibfk_1` FOREIGN KEY (`Id_usuario`) REFERENCES `usuario` (`Id_usuario`) ON DELETE CASCADE,
  CONSTRAINT `usuario_rol_ibfk_2` FOREIGN KEY (`Id_rol`) REFERENCES `rol` (`Id_rol`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario_rol`
--

LOCK TABLES `usuario_rol` WRITE;
/*!40000 ALTER TABLE `usuario_rol` DISABLE KEYS */;
INSERT INTO `usuario_rol` VALUES (1,1),(2,2);
/*!40000 ALTER TABLE `usuario_rol` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `venta`
--

DROP TABLE IF EXISTS `venta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `venta` (
  `Id_venta` int NOT NULL AUTO_INCREMENT,
  `Id_producto` int NOT NULL,
  `cantidad` int NOT NULL,
  `Precio_total` decimal(10,2) NOT NULL,
  `Fecha` date NOT NULL,
  `Id_cliente` int NOT NULL,
  PRIMARY KEY (`Id_venta`),
  KEY `Id_producto` (`Id_producto`),
  KEY `Id_cliente` (`Id_cliente`),
  CONSTRAINT `venta_ibfk_1` FOREIGN KEY (`Id_producto`) REFERENCES `producto` (`Id_producto`) ON DELETE RESTRICT,
  CONSTRAINT `venta_ibfk_2` FOREIGN KEY (`Id_cliente`) REFERENCES `cliente` (`Id_cliente`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `venta`
--

LOCK TABLES `venta` WRITE;
/*!40000 ALTER TABLE `venta` DISABLE KEYS */;
/*!40000 ALTER TABLE `venta` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-15 14:16:24
