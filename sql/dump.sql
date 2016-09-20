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
) ENGINE=InnoDB AUTO_INCREMENT=55031 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `logs`
--

LOCK TABLES `logs` WRITE;
/*!40000 ALTER TABLE `logs` DISABLE KEYS */;
/*!40000 ALTER TABLE `logs` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `parameters`
--

LOCK TABLES `parameters` WRITE;
/*!40000 ALTER TABLE `parameters` DISABLE KEYS */;
INSERT INTO `parameters` VALUES (1,'Internal Temperature','INTERNAL_TEMP','temperature on inetrnal sensor','%2.1f','°C','DOUBLE',NULL),(2,'Internal Humidity','INTERNAL_HUMIDITY','Влажность','%2.1f','%','DOUBLE',NULL),(3,'Lumiosity','LUMIOSITY',NULL,'%2f','lx','DOUBLE',NULL),(4,'Battery Voltage','VBAT_SENSOR',NULL,'%2.2f','V','DOUBLE',0.00974478),(5,'AC Voltage','VAC_SENSOR',NULL,'%3.1f','V','DOUBLE',0.3085554),(6,'PIR Sensor','PIR_SENSOR',NULL,NULL,NULL,'BOOLEAN',NULL),(7,'Gas Sensor','GAS_SENSOR',NULL,NULL,NULL,'BOOLEAN',NULL),(8,'Gas Sensor Signal','GAS_SENSOR_ANALOG',NULL,NULL,'V','DOUBLE',0.004887586),(9,'Battery Temperature','BATT_TEMP',NULL,NULL,'°C','DOUBLE',NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=112 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sensors_data`
--

LOCK TABLES `sensors_data` WRITE;
/*!40000 ALTER TABLE `sensors_data` DISABLE KEYS */;
/*!40000 ALTER TABLE `sensors_data` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `timer`
--

LOCK TABLES `timer` WRITE;
/*!40000 ALTER TABLE `timer` DISABLE KEYS */;
INSERT INTO `timer` VALUES (1,'internal_sensors_poll',NULL,10,1),(2,'write_db_data',NULL,300,1),(3,'db_clearing','2014-01-05 01:00:00',86400,1);
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
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8;
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

-- Dump completed on 2016-09-20 17:28:12
