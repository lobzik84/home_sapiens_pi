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
) ENGINE=InnoDB AUTO_INCREMENT=311388 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=4572 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `settings`
--

LOCK TABLES `settings` WRITE;
/*!40000 ALTER TABLE `settings` DISABLE KEYS */;
INSERT INTO `settings` VALUES (1,'BoxName','Дача'),(2,'VACAlertMin','20'),(3,'VACAlertMax','50'),(4,'InTempAlertMin','14'),(5,'InTempAlertMax','30'),(6,'Lamp2PIRSensorScript','false'),(7,'Lamp1PIRSensorScript','false'),(8,'Lamp2DarkSensorScript','false'),(9,'Lamp1DarkSensorScript','false'),(10,'Socket1OnCommand433','10044428,200'),(11,'Socket1OffCommand433','10044420,200'),(12,'Lamp1OnCommand433','15285704,350'),(13,'Lamp1OffCommand433','9635496,350'),(14,'Lamp2OnCommand433','15285764,350'),(15,'Lamp2OffCommand433','9635496,351'),(16,'WetSensorAddress433','96789678'),(17,'DoorSensorAddress433','123456793'),(18,'SpeakerNotifications','false'),(19,'StatToEmailScript','false');
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

-- Dump completed on 2016-11-09 17:16:50
