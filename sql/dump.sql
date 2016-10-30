-- MySQL dump 10.13  Distrib 5.6.28, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: hs
-- ------------------------------------------------------
-- Server version	5.6.28-0ubuntu0.15.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `actions`
--

DROP TABLE IF EXISTS `actions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `actions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `alias` varchar(25) DEFAULT NULL,
  `module` varchar(50) NOT NULL,
  `data` varchar(1023) DEFAULT NULL,
  `condition_id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `actions`
--

LOCK TABLES `actions` WRITE;
/*!40000 ALTER TABLE `actions` DISABLE KEYS */;
/*!40000 ALTER TABLE `actions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conditions`
--

DROP TABLE IF EXISTS `conditions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `conditions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `parameter_id` int(11) DEFAULT NULL,
  `alias` varchar(25) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `box_mode` varchar(25) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conditions`
--

LOCK TABLES `conditions` WRITE;
/*!40000 ALTER TABLE `conditions` DISABLE KEYS */;
/*!40000 ALTER TABLE `conditions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `logs`
--

DROP TABLE IF EXISTS `logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `logs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `module_name` varchar(255) NOT NULL,
  `dated` datetime NOT NULL,
  `level` varchar(10) NOT NULL,
  `message` varchar(1000) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=254846 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `logs`
--

LOCK TABLES `logs` WRITE;
/*!40000 ALTER TABLE `logs` DISABLE KEYS */;
/*!40000 ALTER TABLE `logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mode`
--

DROP TABLE IF EXISTS `mode`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mode` (
  `box_mode` varchar(25) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mode`
--

LOCK TABLES `mode` WRITE;
/*!40000 ALTER TABLE `mode` DISABLE KEYS */;
INSERT INTO `mode` VALUES ('IDLE');
/*!40000 ALTER TABLE `mode` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `parameters`
--

DROP TABLE IF EXISTS `parameters`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `parameters` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(25) NOT NULL,
  `alias` varchar(60) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `format_pattern` varchar(25) DEFAULT NULL,
  `unit` varchar(5) DEFAULT NULL,
  `type` varchar(25) DEFAULT NULL,
  `calibration` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `parameters`
--

LOCK TABLES `parameters` WRITE;
/*!40000 ALTER TABLE `parameters` DISABLE KEYS */;
INSERT INTO `parameters` VALUES (1,'Internal Temperature','INTERNAL_TEMP','temperature on inetrnal sensor','%2.1f','°C','DOUBLE',NULL),(2,'Internal Humidity','INTERNAL_HUMIDITY','Влажность','%2.1f','%','DOUBLE',NULL),(3,'Lumiosity','LUMIOSITY',NULL,'%2.1f','lx','DOUBLE',NULL),(4,'Battery Voltage','VBAT_SENSOR',NULL,'%2.1f','V','DOUBLE',0.03861003861003861),(5,'AC Voltage','VAC_SENSOR',NULL,'%3.1f','V','DOUBLE',0.8058608058608059),(6,'PIR Sensor','PIR_SENSOR',NULL,NULL,NULL,'BOOLEAN',NULL),(7,'Gas Sensor','GAS_SENSOR',NULL,NULL,NULL,'BOOLEAN',NULL),(8,'Gas Sensor Signal','GAS_SENSOR_ANALOG',NULL,NULL,'V','DOUBLE',0.004887586),(9,'Battery Temperature','BATT_TEMP',NULL,NULL,'°C','DOUBLE',NULL),(10,'Modem RSSI','MODEM_RSSI','уровень сигнала сотовой сети','%2.1f','dBm','DOUBLE',NULL),(11,'Outside Temperature','OUTSIDE_TEMP','температура на улице','%2.1f','°C','DOUBLE',NULL),(12,'MIC Noise','MIC_NOISE','Шум в помещении',NULL,NULL,'BOOLEAN',NULL),(13,'Lamp1','LAMP_1','Лампа 1',NULL,NULL,'BOOLEAN',NULL),(14,'Lamp2','LAMP_2','Лампа 2',NULL,NULL,'BOOLEAN',NULL),(15,'Socket','SOCKET','Розетка',NULL,NULL,'BOOLEAN',NULL),(16,'Charge Enabled','CHARGE_ENABLED','Включена зарядка батареи',NULL,NULL,'BOOLEAN',NULL),(17,'Rain','RAIN','Осадки','%2.1f','mm','DOUBLE',NULL),(18,'Clouds','CLOUDS','Облачность','%2.1f','%','DOUBLE',NULL),(19,'Battery Charge','BATT_CHARGE','Заряд батареи','%2d','%','INTEGER',NULL),(20,'Wet Sensor','WET_SENSOR','Датчик протечки',NULL,NULL,'BOOLEAN',NULL),(21,'Door Sensor','DOOR_SENSOR','Датчик открытия двери',NULL,NULL,'BOOLEAN',NULL);
/*!40000 ALTER TABLE `parameters` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sensors_data`
--

DROP TABLE IF EXISTS `sensors_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sensors_data` (
  `rec_id` int(11) NOT NULL AUTO_INCREMENT,
  `parameter_id` int(11) NOT NULL,
  `date` datetime NOT NULL,
  `value_d` double DEFAULT NULL,
  `value_s` varchar(255) DEFAULT NULL,
  `value_b` int(11) DEFAULT NULL,
  `value_i` int(11) DEFAULT NULL,
  `value_min` double DEFAULT NULL,
  `date_min` datetime DEFAULT NULL,
  `value_max` double DEFAULT NULL,
  `date_max` datetime DEFAULT NULL,
  `transfer_count` int(11) DEFAULT NULL,
  `grouped` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`rec_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3763 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sensors_data`
--

LOCK TABLES `sensors_data` WRITE;
/*!40000 ALTER TABLE `sensors_data` DISABLE KEYS */;
/*!40000 ALTER TABLE `sensors_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `settings`
--

DROP TABLE IF EXISTS `settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `settings` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(25) NOT NULL,
  `value` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `settings`
--

LOCK TABLES `settings` WRITE;
/*!40000 ALTER TABLE `settings` DISABLE KEYS */;
INSERT INTO `settings` VALUES (1,'BoxName','Дача'),(2,'VACAlertMin','20'),(3,'VACAlertMax','50'),(4,'InTempAlertMin','14'),(5,'InTempAlertMax','30'),(6,'Lamp2PIRSensorScript','true'),(7,'Lamp1PIRSensorScript','true'),(8,'Lamp2DarkSensorScript','false'),(9,'Lamp1DarkSensorScript','false'),(10,'Socket1OnCommand433','10044428,200'),(11,'Socket1OffCommand433','10044420,200'),(12,'Lamp1OnCommand433','15285704,350'),(13,'Lamp1OffCommand433','9635496,350'),(14,'Lamp2OnCommand433','15285764,350'),(15,'Lamp2OffCommand433','9635496,351'),(16,'WetSensorAddress433','96789678'),(17,'DoorSensorAddress433','123456793');
/*!40000 ALTER TABLE `settings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sms_inbox`
--

DROP TABLE IF EXISTS `sms_inbox`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sms_inbox` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `message` varchar(140) NOT NULL,
  `sender` varchar(25) NOT NULL,
  `date` datetime NOT NULL,
  `status` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sms_inbox`
--

LOCK TABLES `sms_inbox` WRITE;
/*!40000 ALTER TABLE `sms_inbox` DISABLE KEYS */;
/*!40000 ALTER TABLE `sms_inbox` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sms_outbox`
--

DROP TABLE IF EXISTS `sms_outbox`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sms_outbox` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `message` varchar(70) NOT NULL,
  `recipient` varchar(25) NOT NULL,
  `date` datetime NOT NULL,
  `status` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sms_outbox`
--

LOCK TABLES `sms_outbox` WRITE;
/*!40000 ALTER TABLE `sms_outbox` DISABLE KEYS */;
/*!40000 ALTER TABLE `sms_outbox` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `test_stages`
--

DROP TABLE IF EXISTS `test_stages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `test_stages` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `stage` int(11) NOT NULL,
  `message` varchar(2048) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `test_stages`
--

LOCK TABLES `test_stages` WRITE;
/*!40000 ALTER TABLE `test_stages` DISABLE KEYS */;
INSERT INTO `test_stages` VALUES (36,2,''),(37,3,''),(38,4,'');
/*!40000 ALTER TABLE `test_stages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `timer`
--

DROP TABLE IF EXISTS `timer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `timer` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `start_date` datetime DEFAULT NULL,
  `period` int(11) DEFAULT NULL,
  `enabled` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `timer`
--

LOCK TABLES `timer` WRITE;
/*!40000 ALTER TABLE `timer` DISABLE KEYS */;
INSERT INTO `timer` VALUES (1,'internal_sensors_poll',NULL,10,1),(2,'write_db_data',NULL,300,1),(3,'db_clearing','2014-01-05 01:00:00',86400,1),(4,'get_forecast',NULL,21600,1),(5,'update_display','2014-01-05 01:00:01',60,1);
/*!40000 ALTER TABLE `timer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `login` varchar(50) NOT NULL,
  `email` varchar(50) DEFAULT NULL,
  `salt` varchar(32) NOT NULL,
  `verifier` varchar(66) NOT NULL,
  `status` int(2) NOT NULL,
  `public_key` varchar(256) NOT NULL,
  `keyfile` text,
  `synced` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (61,'+7(111)111-11-11',NULL,'yJLE8fpuis0Qfyu8','49dcbcdf059e84930684dc499584d21578eb5025c316289124c92fa72b917978',1,'8f30006b290c7837ad43532ce5cc2edb9f4e5a2306dc3d1dbe2648fa25e5d82b0290d6d628348f944a6f5944956b0adbad3112e67c7d5d227f7d4ae25229a6bd2dda9a3eeb812a52e693294f13e7bce85442186d7d9d7910ce198c049054071dfac9e016d00efc8be793bf24639ebfd3b204440e896d4b0b8b65579979d5f5e9','4d08b82b1be78b41ff7bdfe274fd4b635d305176cbc524da2509f380d2200fcd4b036f8958a1528153fee651871b0fd586ca10281c4f8f5a98587a7127674454784fb329b9a9bb87c34cc678337e8281834355b2b40c54cfbb1718629c9f08aacc3a87ae4307ceea75643f98022312c8cd35f3d2f95eff8e0fe18ae89f201dbd5600f7c159744e0a9d833f97ca8ac866b9d11edb071e0dce874d324f3bfca15516188ee975927d330fe05db3b89c8faea1908ca318594410b1086e5e184251edb500ff63fc7c84d122c297647956d11d77e8cc1c38cc6be6b586d978c16b2feea029e9cf00b25d3279862ce4b6840925d7d5f94cc1fe0d829a40f04cc9b784c0d448c93eaf610fd5e8eaa7ef4f0792e061991cac2ef4142ba22378512d85349d2d73f91908d60c7ee6444548ec4c707ed2b3e3d1ce4f0260b876774c4c61e6628eee1d8caaf66f2803bdbe659f5d9ee9fdb9e8098b0a049e1c4e09d34e8f7bc3ca4cefc1562a49d4180fcd9fb68d72b8d91c8ebf8502e6867e6511e3fac5b8492be58d011473a711a6fb94bf04b7e3e5380e23bed2e5ff4a647fd9d54185321e7347d54ffa00a604897e4957fecaada83628ff1ec56b159201ea48a86ad5018b6d667603c081dbad159be25ea1325eff4889812faf7335c2358c7371bbad800f4ac23a3256e03143071c1674eecdcd030086fc9b9458f7e1a421e37f4199821101b26868a8e430ee4cc80623348a645e0af02c74fc5a023bb606d4256780c5a65eb62cf439b15027842811f67846fd521f4df9f6405b39275275f728560529cd85e489aeadc7f0bda60526a33064705906aacdee546e9b47ea61a356739f91bfdea986586f332b51f09afb716c42947b3c507f692c97d7ca9344c8902597af0ec85e1ca6a8a6ddca96057be73735911c8a99abb0e04f62248ab0416228f17242209ce2585f01cd425eb263edec452bb410969499ad4bee9be9f93f6e48416c777e5933d1f7d57325d2bf39c7811982173bff829f468e6d99df63e23f09fa0245e4a47071b1590b35f4776e349b7c50457f69940e9be14f3591c800041e1fdb209340ee867bd55145de0d7384aa7e3049edd569cfe1eb029f03da1dc5c9a80847c2ba224d4da3d65952dd1fba1b5572f5b37c73c2d0e88b5ca6dca1c2d9ed7aa9',1);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-10-30 19:57:56
