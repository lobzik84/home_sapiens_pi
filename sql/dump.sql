-- MySQL dump 10.13  Distrib 5.5.44, for debian-linux-gnu (armv7l)
--
-- Host: localhost    Database: hs
-- ------------------------------------------------------
-- Server version	5.5.52-0+deb8u1

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
  `alias` varchar(100) DEFAULT NULL,
  `module` varchar(50) NOT NULL,
  `notification_text` varchar(1023) DEFAULT NULL,
  `condition_id` int(11) NOT NULL,
  `severity` varchar(25) DEFAULT NULL,
  `box_mode` varchar(25) DEFAULT NULL,
  `condition_state` int(11) DEFAULT NULL,
  `event_name` varchar(25) DEFAULT NULL,
  `enabled` int(11) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=139 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `actions`
--

LOCK TABLES `actions` WRITE;
/*!40000 ALTER TABLE `actions` DISABLE KEYS */;
INSERT INTO `actions` VALUES (1,'VAC_SENSOR_UNSTABLE_ARMED_SMS','ModemModule','Опасный скачок напряжения электросети: %VALUE% V',1,'ALERT','ARMED',1,'send_sms',1),(3,'VAC_SENSOR_UNSTABLE_WEB','WebNotificationsModule','Опасный скачок напряжения электросети: %VALUE% V',1,'ALERT',NULL,1,'web_notification',1),(4,'VAC_SENSOR_UNSTABLE_LOG','LogModule','Опасный скачок напряжения электросети: %VALUE% V',1,'ALERT',NULL,1,'log_record',1),(11,'VAC_SENSOR_POWER_LOSS_SMS','ModemModule','Более 5 минут отсутствует напряжение в электросети',2,'ALARM',NULL,1,'send_sms',1),(12,'VAC_SENSOR_POWER_LOSS_DISPLAY','DisplayModule','Более 5 минут отсутствует напряжение в электросети',2,'ALARM',NULL,NULL,'display_notification',1),(13,'VAC_SENSOR_POWER_LOSS_LOG','LogModule','Более 5 минут отсутствует напряжение в электросети',2,'ALARM',NULL,1,'log_record',1),(16,'VAC_SENSOR_POWER_LOSS_ARMED_EMAIL','TunnelClientModule','Более 5 минут отсутствует напряжение в электросети',2,'ALARM','ARMED',1,'send_email_notification',1),(17,'VAC_SENSOR_POWER_RECOVERED_SMS','ModemModule','Восстановилось напряжение в электросети',3,'OK',NULL,1,'send_sms',1),(19,'VAC_SENSOR_POWER_RECOVERED_LOG','LogModule','Восстановилось напряжение в электросети',3,'OK',NULL,1,'log_record',1),(33,'BAT_CHARGE_LESS_30_ARMED_SMS','ModemModule','Тревога! Уровень заряда аккумуляторов %VALUE% %',10,'ALARM',NULL,1,'send_sms',1),(34,'BAT_CHARGE_LESS_30_ARMED_WEB','WebNotificationsModule','Уровень заряда аккумуляторов %VALUE% %',10,'ALARM',NULL,1,'web_notification',1),(35,'BAT_CHARGE_LESS_30_ARMED_EMAIL','TunnelClientModule','Уровень заряда аккумуляторов упал до %VALUE% %. Видеонаблюдение выключено. Резервная копия записана. ',10,'ALARM',NULL,1,'send_email_notification',1),(36,'BAT_CHARGE_LESS_30_ARMED_LOG','LogModule','Уровень заряда аккумуляторов упал до %VALUE% %. Видеонаблюдение выключено. Резервная копия записана. ',10,'ALARM',NULL,1,'log_record',1),(44,'BAT_CHARGE_BETWEEN_30_50_ARMED_WEB','WebNotificationsModule','Уровень заряда аккумуляторов %VALUE% %. ',12,'INFO',NULL,1,'web_notification',1),(45,'BAT_CHARGE_BETWEEN_30_50_ARMED_LOG','LogModule','Уровень заряда аккумуляторов %VALUE% %. ',12,'INFO',NULL,1,'log_record',1),(50,'BATT_TEMP_OVERHEAT_ARMED_SMS','ModemModule','Температура аккумуляторов превысила %VALUE%’С. Устройство сохранит все данные и будет выключено. Обратитесь к производителю.',16,'ALARM',NULL,1,'send_sms',1),(51,'BATT_TEMP_OVERHEAT_ARMED_WEB','WebNotificationsModule','Температура аккумуляторов превысила %VALUE%’С. Устройство сохранит все данные и будет выключено. Обратитесь к производителю.',16,'ALARM',NULL,1,'web_notification',1),(52,'BATT_TEMP_OVERHEAT_ARMED_EMAIL','TunnelClientModule','Температура аккумуляторов превысила %VALUE%’С. Устройство сохранит все данные и будет выключено. Обратитесь к производителю.',16,'ALARM',NULL,1,'send_email_notification',1),(53,'BATT_TEMP_OVERHEAT_ARMED_LOG','LogModule','Температура аккумуляторов превысила %VALUE%’С. Устройство сохранит все данные и будет выключено. Обратитесь к производителю.',16,'ALARM',NULL,1,'log_record',1),(56,'INTERNAL_TEMP_SENSOR_FAILURE_ARMED_WEB','WebNotificationsModule','Отказ датчика температуры. Обратитесь к производителю',18,'INFO',NULL,1,'web_notification',1),(57,'INTERNAL_TEMP_SENSOR_FAILURE_ARMED_LOG','LogModule','Отказ датчика температуры. Обратитесь к производителю',18,'INFO',NULL,1,'log_record',1),(61,'INTERNAL_TEMP_FAST_FALLING_ARMED_SMS','ModemModule','Быстрое повышение температуры воздуха',20,'INFO',NULL,1,'send_sms',1),(62,'INTERNAL_TEMP_FAST_FALLING_ARMED_WEB','WebNotificationsModule','Быстрое повышение температуры воздуха',20,'INFO',NULL,1,'web_notification',1),(63,'INTERNAL_TEMP_FAST_FALLING_ARMED_LOG','LogModule','Быстрое повышение температуры воздуха',20,'INFO',NULL,1,'log_record',1),(67,'INTERNAL_TEMP_FAST_RISING_ARMED_SMS','ModemModule','Быстрое повышение температуры воздуха',22,'INFO',NULL,1,'send_sms',1),(68,'INTERNAL_TEMP_FAST_RISING_ARMED_WEB','WebNotificationsModule','Быстрое повышение температуры воздуха',22,'INFO',NULL,1,'web_notification',1),(69,'INTERNAL_TEMP_FAST_RISING_ARMED_LOG','LogModule','Быстрое повышение температуры воздуха',22,'INFO',NULL,1,'log_record',1),(74,'INTERNAL_TEMP_OUT_OF_BOUNDS_ARMED_SMS','ModemModule','Температура воздуха %VALUE%’С',24,'ALARM',NULL,1,'send_sms',1),(75,'INTERNAL_TEMP_OUT_OF_BOUNDS_ARMED_DISPLAY','DisplayModule','Температура воздуха %VALUE%’С',24,'ALARM',NULL,NULL,'display_notification',1),(76,'INTERNAL_TEMP_OUT_OF_BOUNDS_ARMED_EMAIL','TunnelClientModule','Температура воздуха %VALUE%’С',24,'ALARM',NULL,1,'send_email_notification',1),(77,'INTERNAL_TEMP_OUT_OF_BOUNDS_ARMED_LOG','LogModule','Температура воздуха %VALUE%’С',24,'ALARM',NULL,1,'log_record',1),(80,'INTERNAL_TEMP_BACK_TO_NORMAL_ARMED_SMS','ModemModule','Температура воздуха в норме',26,'OK',NULL,1,'send_sms',1),(81,'INTERNAL_TEMP_BACK_TO_NORMAL_ARMED_WEB','WebNotificationsModule','Температура воздуха в норме',26,'OK',NULL,1,'web_notification',1),(82,'INTERNAL_TEMP_BACK_TO_NORMAL_ARMED_LOG','LogModule','Температура воздуха в норме',26,'OK',NULL,1,'log_record',1),(86,'INTERNAL_HUMIDITY_OUT_OF_BOUNDS_ARMED_WEB','WebNotificationsModule','Влажность водуха %VALUE% %',28,'INFO',NULL,1,'web_notification',1),(87,'INTERNAL_HUMIDITY_OUT_OF_BOUNDS_ARMED_LOG','LogModule','Влажность водуха %VALUE% %',28,'INFO',NULL,1,'log_record',1),(95,'INTERNAL_HUMIDITY_BACK_TO_NORMAL_ARMED_WEB','WebNotificationsModule','Влажность водуха  в норме',32,'OK',NULL,1,'web_notification',1),(96,'INTERNAL_HUMIDITY_BACK_TO_NORMAL_ARMED_LOG','LogModule','Влажность водуха  в норме',32,'OK',NULL,1,'log_record',1),(101,'GAS_SENSOR_ALARM_SMS','ModemModule','Тревога! Опасная концентрация газа / дыма',34,'ALARM',NULL,1,'send_sms',1),(102,'GAS_SENSOR_ALARM_WEB','WebNotificationsModule','Опасная концентрация газа / дыма',34,'ALARM',NULL,1,'web_notification',1),(103,'GAS_SENSOR_ALARM_EMAIL','TunnelClientModule','Опасная концентрация газа / дыма',34,'ALARM',NULL,1,'send_email_notification',1),(104,'GAS_SENSOR_ALARM_LOG','LogModule','Опасная концентрация газа / дыма',34,'ALARM',NULL,1,'log_record',1),(108,'GAS_SENSOR_CLEARED_SMS','ModemModule','Концентрация газа в норме',34,'OK',NULL,0,'send_sms',1),(109,'GAS_SENSOR_CLEARED_WEB','WebNotificationsModule','Концентрация газа в норме',34,'OK',NULL,0,'web_notification',1),(110,'GAS_SENSOR_CLEARED_EMAIL','TunnelClientModule','Концентрация газа в норме',34,'OK',NULL,0,'send_email_notification',1),(111,'GAS_SENSOR_CLEARED_LOG','LogModule','Концентрация газа в норме',34,'OK',NULL,0,'log_record',1),(113,'PIR_SENSOR_ALARM_ARMED_SMS','ModemModule','Тревога! Сработал датчик движения',38,'ALARM','ARMED',1,'send_sms',1),(114,'PIR_SENSOR_ALARM_ARMED_WEB','WebNotificationsModule','Сработал датчик движения',38,'ALARM','ARMED',1,'web_notification',1),(115,'PIR_SENSOR_ALARM_ARMED_EMAIL','TunnelClientModule','Сработал датчик движения',38,'ALARM','ARMED',1,'send_email_notification',1),(116,'PIR_SENSOR_ALARM_ARMED_LOG','LogModule','Сработал датчик движения',38,'ALARM','ARMED',1,'log_record',1),(118,'PIR_SENSOR_CLEARED_ARMED_WEB','WebNotificationsModule','Датчик перестал фиксировать движения ',38,'OK','ARMED',0,'web_notification',1),(119,'PIR_SENSOR_CLEARED_ARMED_EMAIL','TunnelClientModule','Датчик перестал фиксировать движения ',38,'OK','ARMED',0,'send_email_notification',1),(120,'PIR_SENSOR_CLEARED_ARMED_LOG','LogModule','Датчик перестал фиксировать движения ',38,'OK','ARMED',0,'log_record',1),(121,'MIC_NOISE_ALARM_ARMED_WEB','WebNotificationsModule','Микрофон фиксирует шум',42,'INFO','ARMED',1,'web_notification',1),(122,'MIC_NOISE_ALARM_ARMED_LOG','LogModule','Микрофон фиксирует шум',42,'INFO','ARMED',1,'log_record',1),(123,'MIC_NOISE_CLEARED_ARMED_WEB','WebNotificationsModule','Шум прекратился',42,'OK','ARMED',0,'web_notification',1),(124,'MIC_NOISE_CLEARED_ARMED_LOG','LogModule','Шум прекратился',42,'OK','ARMED',0,'log_record',1),(133,'DOOR_SENSOR_OPEN_ARMED_SMS','ModemModule','Тревога! Сработал датчик открытия двери!',7,'INFO',NULL,1,'send_sms',1),(134,'DOOR_SENSOR_OPEN_ARMED_WEB','WebNotificationsModule','Сработал датчик открытия двери',7,'ALARM',NULL,1,'web_notification',1),(135,'DOOR_SENSOR_OPEN_ARMED_EMAIL','TunnelClientModule','Сработал датчик открытия двери',7,'ALARM',NULL,1,'send_email_notification',1),(136,'DOOR_SENSOR_OPEN_ARMED_LOG','LogModule','Сработал датчик открытия двери',7,'ALARM',NULL,1,'log_record',1),(137,'VAC_SENSOR_UNSTABLE_DISPLAY','DisplayModule','Опасный скачок напряжения электросети: %VALUE% V',1,'ALERT',NULL,NULL,'display_notification',1),(138,'GAS_SENSOR_ALARM_DISPLAY','DisplayModule','Опасная концентрация газа / дыма',34,'ALARM',NULL,NULL,'display_notification',1);
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
  `alias` varchar(100) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `state` int(11) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conditions`
--

LOCK TABLES `conditions` WRITE;
/*!40000 ALTER TABLE `conditions` DISABLE KEYS */;
INSERT INTO `conditions` VALUES (1,5,'VAC_SENSOR_UNSTABLE','Опасный для электроприборов скачок напряжения электросети\n(в т.ч. отключение напряжения)',0),(2,5,'VAC_SENSOR_POWER_LOSS','более 5 минут отсутствует напряжение в сети ',0),(3,5,'VAC_SENSOR_POWER_RECOVERED','напряжение более 5 минут после отказа в норме ',1),(7,21,'DOOR_SENSOR_OPEN','Сработал датчик открывания двери',0),(10,19,'BAT_CHARGE_LESS_30','Заряд аккумуляторов меньше 30%',0),(12,19,'BAT_CHARGE_BETWEEN_30_50','Заряд аккумуляторов < 50% и > 30%',0),(14,19,'BAT_CHARGE_NORMAL','Заряд аккумуляторов в норме',1),(16,9,'BATT_TEMP_OVERHEAT','Перегрев аккумулятора',0),(18,1,'INTERNAL_TEMP_SENSOR_FAILURE','Отказ датчика температуры',0),(20,1,'INTERNAL_TEMP_FAST_FALLING','Быстрое повышение температуры воздуха',0),(22,1,'INTERNAL_TEMP_FAST_RISING','Быстрое повышение температуры воздуха ',0),(24,1,'INTERNAL_TEMP_OUT_OF_BOUNDS','Вышла и более 5 минут подряд находится вне установленных пределов',0),(26,1,'INTERNAL_TEMP_BACK_TO_NORMAL','Температура воздуха вернулась в норму',1),(28,2,'INTERNAL_HUMIDITY_OUT_OF_BOUNDS','Вышла и более 5 минут подряд находится вне установленных пределов',1),(32,2,'INTERNAL_HUMIDITY_BACK_TO_NORMAL','Влажность воздуха вернулась в норму',0),(34,7,'GAS_SENSOR_ALARM','Сработал датчик газа',0),(38,6,'PIR_SENSOR_ALARM','Сработал датчик движения',0),(42,12,'MIC_NOISE_ALARM','Сработал датчик шума',0);
/*!40000 ALTER TABLE `conditions` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `parameters`
--

LOCK TABLES `parameters` WRITE;
/*!40000 ALTER TABLE `parameters` DISABLE KEYS */;
INSERT INTO `parameters` VALUES (1,'Internal Temperature','INTERNAL_TEMP','Температура в помещении','%2.1f','°C','DOUBLE',NULL),(2,'Internal Humidity','INTERNAL_HUMIDITY','Влажность','%2.1f','%','DOUBLE',NULL),(3,'Lumiosity','LUMIOSITY',NULL,'%2.1f','lx','DOUBLE',NULL),(4,'Battery Voltage','VBAT_SENSOR',NULL,'%2.1f','V','DOUBLE',0.011612447),(5,'AC Voltage','VAC_SENSOR',NULL,'%3.1f','V','DOUBLE',0.358064516),(6,'PIR Sensor','PIR_SENSOR',NULL,NULL,NULL,'BOOLEAN',NULL),(7,'Gas Sensor','GAS_SENSOR',NULL,NULL,NULL,'BOOLEAN',NULL),(8,'Gas Sensor Signal','GAS_SENSOR_ANALOG',NULL,NULL,'V','DOUBLE',0.001075269),(9,'Battery Temperature','BATT_TEMP',NULL,NULL,'°C','DOUBLE',NULL),(10,'Modem RSSI','MODEM_RSSI','уровень сигнала сотовой сети','%2.1f','dBm','DOUBLE',NULL),(11,'Outside Temperature','OUTSIDE_TEMP','температура на улице','%2.1f','°C','DOUBLE',NULL),(12,'MIC Noise','MIC_NOISE','Шум в помещении',NULL,NULL,'BOOLEAN',NULL),(13,'Lamp1','LAMP_1','Лампа 1',NULL,NULL,'BOOLEAN',NULL),(14,'Lamp2','LAMP_2','Лампа 2',NULL,NULL,'BOOLEAN',NULL),(15,'Socket','SOCKET','Розетка',NULL,NULL,'BOOLEAN',NULL),(16,'Charge Enabled','CHARGE_ENABLED','Включена зарядка батареи',NULL,NULL,'BOOLEAN',NULL),(17,'Rain','RAIN','Осадки','%2.1f','mm','DOUBLE',NULL),(18,'Clouds','CLOUDS','Облачность','%2.1f','%','DOUBLE',NULL),(19,'Battery Charge','BATT_CHARGE','Заряд батареи','%2d','%','INTEGER',NULL),(20,'Wet Sensor','WET_SENSOR','Датчик протечки',NULL,NULL,'BOOLEAN',NULL),(21,'Door Sensor','DOOR_SENSOR','Датчик открытия двери',NULL,NULL,'BOOLEAN',NULL),(22,'Night Time','NIGHTTIME','?????? ?????',NULL,NULL,'BOOLEAN',NULL);
/*!40000 ALTER TABLE `parameters` ENABLE KEYS */;
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
  `value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `settings`
--

LOCK TABLES `settings` WRITE;
/*!40000 ALTER TABLE `settings` DISABLE KEYS */;
INSERT INTO `settings` VALUES (1,'BoxName','Прототип 23'),(2,'VACAlertMin','100'),(3,'VACAlertMax','250'),(4,'InTempAlertMin','18'),(5,'InTempAlertMax','40'),(6,'Lamp2PIRSensorScript','true'),(7,'Lamp1PIRSensorScript','false'),(8,'Lamp2DarkSensorScript','true'),(9,'Lamp1DarkSensorScript','false'),(10,'Socket1OnCommand433','10044428,200'),(11,'Socket1OffCommand433','10044420,200'),(12,'Lamp1OnCommand433','1222003,350'),(13,'Lamp1OffCommand433','1222004,350'),(14,'Lamp2OnCommand433','2159715,350'),(15,'Lamp2OffCommand433','2159716,350'),(16,'WetSensorAddress433','96789678'),(17,'DoorSensorAddress433','123456793'),(18,'SpeakerNotifications','true'),(19,'StatToEmailScript','false'),(20,'NotificationsEmail','labozin@molnet.ru'),(21,'VBatAlertCritical','30'),(22,'VBatAlertMinor','50'),(23,'VBatTempAlertMax','50'),(24,'SMSNotifications','true');
/*!40000 ALTER TABLE `settings` ENABLE KEYS */;
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
INSERT INTO `timer` VALUES (1,'internal_sensors_poll',NULL,10,1),(2,'write_db_data',NULL,300,1),(3,'db_clearing','2014-01-05 01:00:00',86400,1),(4,'get_forecast',NULL,10800,1),(5,'update_display','2014-01-05 01:00:01',60,1);
/*!40000 ALTER TABLE `timer` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-11-25 13:09:26
