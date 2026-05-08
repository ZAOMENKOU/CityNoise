/*
SQLyog v10.2 
MySQL - 8.0.30 : Database - city_noise_system
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`city_noise_system` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `city_noise_system`;

/*Table structure for table `complaint_images` */

DROP TABLE IF EXISTS `complaint_images`;

CREATE TABLE `complaint_images` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `complaint_id` bigint NOT NULL COMMENT '投诉ID',
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片URL',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `deleted` int DEFAULT '0' COMMENT '逻辑删除标记 (0:未删除, 1:已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_complaint_id` (`complaint_id`),
  CONSTRAINT `complaint_images_ibfk_1` FOREIGN KEY (`complaint_id`) REFERENCES `complaints` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投诉图片表';

/*Data for the table `complaint_images` */

insert  into `complaint_images`(`id`,`complaint_id`,`image_url`,`create_time`,`deleted`) values (1,1,'/uploads/20260226_133607_43254c85.png','2026-02-26 13:36:07',0),(2,2,'/uploads/20260227_162844_bd7b0c31.png','2026-02-27 16:28:45',0),(3,3,'/uploads/20260301_154155_d3d00da4.png','2026-03-01 15:41:55',0),(4,4,'/uploads/20260318_205612_ff6499f2.png','2026-03-18 20:56:12',0);

/*Table structure for table `complaint_locations` */

DROP TABLE IF EXISTS `complaint_locations`;

CREATE TABLE `complaint_locations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `complaint_id` bigint NOT NULL COMMENT '投诉ID',
  `longitude` decimal(10,7) NOT NULL COMMENT '经度',
  `latitude` decimal(9,7) NOT NULL COMMENT '纬度',
  `district` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '行政区（如：洪山区）',
  `detail_address` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '详细地址描述',
  `deleted` int DEFAULT '0' COMMENT '逻辑删除标记 (0:未删除, 1:已删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `complaint_id` (`complaint_id`),
  KEY `idx_district` (`district`),
  CONSTRAINT `complaint_locations_ibfk_1` FOREIGN KEY (`complaint_id`) REFERENCES `complaints` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投诉位置表';

/*Data for the table `complaint_locations` */

insert  into `complaint_locations`(`id`,`complaint_id`,`longitude`,`latitude`,`district`,`detail_address`,`deleted`) values (1,1,'114.3473251','30.5353342','武昌区','湖北省武汉市武昌区武珞路586-216号',0),(2,2,'114.4756750','30.5977737','青山区','湖北省武汉市青山区S7(武鄂高速)',0),(3,3,'114.2623813','30.5429236','汉阳区','湖北省武汉市汉阳区高龙路',0),(4,4,'114.3118196','30.4877116','洪山区','湖北省武汉市洪山区西苑大道',0),(5,5,'116.4040000','39.9150000','???','??SOHO',0),(6,6,'116.4040000','39.9150000','???','??SOHO',0);

/*Table structure for table `complaints` */

DROP TABLE IF EXISTS `complaints`;

CREATE TABLE `complaints` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '投诉人ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '投诉标题',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '投诉详情',
  `noise_type` enum('CONSTRUCTION','TRAFFIC','LIVING','COMMERCIAL','INDUSTRIAL') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '噪音类型',
  `status` enum('PENDING','ASSIGNED','PROCESSING','RESOLVED','CLOSED') COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT '处理状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` int DEFAULT '0' COMMENT '逻辑删除标记 (0:未删除, 1:已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `complaints_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投诉主表';

/*Data for the table `complaints` */

insert  into `complaints`(`id`,`user_id`,`title`,`description`,`noise_type`,`status`,`create_time`,`update_time`,`deleted`) values (1,5,'噪音投诉','test1','COMMERCIAL','CLOSED','2026-02-26 13:36:07','2026-02-26 15:41:02',0),(2,5,'噪音投诉','test1','CONSTRUCTION','PENDING','2026-02-27 16:28:45','2026-02-27 16:36:03',1),(3,5,'噪音投诉','test','COMMERCIAL','ASSIGNED','2026-03-01 15:41:55','2026-03-01 15:41:55',0),(4,5,'噪音投诉','test3','CONSTRUCTION','PROCESSING','2026-03-18 20:56:12','2026-03-18 20:56:12',0),(5,10,'??????','????????,????','CONSTRUCTION','PENDING','2026-03-29 20:48:25','2026-03-29 20:48:25',0),(6,10,'??????','????????','CONSTRUCTION','PENDING','2026-03-29 21:09:34','2026-03-29 21:09:34',0);

/*Table structure for table `handling_records` */

DROP TABLE IF EXISTS `handling_records`;

CREATE TABLE `handling_records` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `complaint_id` bigint NOT NULL COMMENT '投诉ID',
  `handler_id` bigint DEFAULT NULL COMMENT '处理人ID（系统操作可为NULL）',
  `action` enum('SUBMIT','ASSIGN','START_PROCESS','UPDATE','RESOLVE','CLOSE') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型',
  `remark` text COLLATE utf8mb4_unicode_ci COMMENT '处理备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `deleted` int DEFAULT '0' COMMENT '逻辑删除标记 (0:未删除, 1:已删除)',
  PRIMARY KEY (`id`),
  KEY `handler_id` (`handler_id`),
  KEY `idx_complaint_id` (`complaint_id`),
  CONSTRAINT `handling_records_ibfk_1` FOREIGN KEY (`complaint_id`) REFERENCES `complaints` (`id`) ON DELETE CASCADE,
  CONSTRAINT `handling_records_ibfk_2` FOREIGN KEY (`handler_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投诉处理记录表';

/*Data for the table `handling_records` */

insert  into `handling_records`(`id`,`complaint_id`,`handler_id`,`action`,`remark`,`create_time`,`deleted`) values (1,1,5,'SUBMIT','提交投诉','2026-02-26 13:36:07',0),(2,1,2,'ASSIGN','已分配给处理人员：处理员张三','2026-02-26 13:36:24',0),(3,1,2,'START_PROCESS','开始处理投诉','2026-02-26 14:00:27',0),(4,1,2,'START_PROCESS','test3','2026-02-26 14:12:44',0),(5,1,2,'START_PROCESS','test3','2026-02-26 14:16:19',0),(6,1,2,'UPDATE','test4','2026-02-26 14:21:10',0),(7,1,2,'RESOLVE','投诉处理完成','2026-02-26 14:21:29',0),(8,1,5,'UPDATE','居民标记为未解决：test4','2026-02-26 14:22:33',0),(9,1,2,'RESOLVE','投诉处理完成','2026-02-26 14:27:22',0),(10,1,5,'UPDATE','居民标记为未解决：111','2026-02-26 14:28:50',0),(11,1,2,'RESOLVE','投诉处理完成','2026-02-26 14:31:22',0),(12,1,5,'UPDATE','居民标记为未解决：test1','2026-02-26 14:31:41',0),(13,1,2,'RESOLVE','投诉处理完成','2026-02-26 14:35:29',0),(14,1,5,'UPDATE','居民标记为未解决：‘’','2026-02-26 14:35:43',0),(15,1,2,'RESOLVE','投诉处理完成','2026-02-26 14:38:09',0),(16,1,5,'UPDATE','居民标记为未解决：3123','2026-02-26 14:38:22',0),(17,1,2,'RESOLVE','投诉处理完成','2026-02-26 14:41:47',0),(18,1,5,'UPDATE','居民标记为未解决：11111','2026-02-26 14:42:01',0),(19,1,2,'RESOLVE','投诉处理完成','2026-02-26 14:44:57',0),(20,1,5,'UPDATE','居民标记为未解决：2323','2026-02-26 14:45:13',0),(21,1,2,'RESOLVE','投诉处理完成','2026-02-26 14:54:19',0),(22,1,5,'UPDATE','居民标记为未解决：1231','2026-02-26 14:54:29',0),(23,1,2,'RESOLVE','投诉处理完成','2026-02-26 15:02:03',0),(24,1,5,'UPDATE','居民标记为未解决：1231','2026-02-26 15:02:14',0),(25,1,2,'RESOLVE','投诉处理完成','2026-02-26 15:05:07',0),(26,1,5,'UPDATE','居民标记为未解决：12312','2026-02-26 15:05:42',0),(27,1,2,'RESOLVE','投诉处理完成','2026-02-26 15:27:51',0),(28,1,2,'UPDATE','居民标记为未解决：123','2026-02-26 15:28:03',0),(29,1,2,'RESOLVE','投诉处理完成','2026-02-26 15:28:21',0),(30,1,5,'CLOSE','用户评价后关闭投诉','2026-02-26 15:28:47',0),(31,1,2,'RESOLVE','1','2026-02-26 15:32:06',0),(32,1,2,'CLOSE','用户评价后关闭投诉','2026-02-26 15:41:02',0),(33,2,5,'SUBMIT','提交投诉','2026-02-27 16:28:45',0),(34,2,5,'CLOSE','居民删除投诉：1','2026-02-27 16:36:03',0),(35,3,5,'SUBMIT','提交投诉','2026-03-01 15:41:55',0),(36,4,5,'SUBMIT','提交投诉','2026-03-18 20:56:12',0),(37,4,2,'ASSIGN','已分配给处理人员：处理员张三','2026-03-18 20:57:10',0),(38,4,2,'START_PROCESS','开始处理投诉','2026-03-19 17:07:51',0),(39,3,2,'ASSIGN','已分配给处理人员：处理员张三','2026-03-27 17:38:30',0),(40,5,10,'SUBMIT','提交投诉','2026-03-29 20:48:25',0),(41,6,10,'SUBMIT','提交投诉','2026-03-29 21:09:34',0);

/*Table structure for table `monitor_data` */

DROP TABLE IF EXISTS `monitor_data`;

CREATE TABLE `monitor_data` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `station_id` bigint NOT NULL COMMENT '关联的监测点ID',
  `noise_level` decimal(5,2) NOT NULL COMMENT '等效声级(dB)',
  `monitor_time` datetime NOT NULL COMMENT '监测时间（精确到具体时刻）',
  `monitor_date` date GENERATED ALWAYS AS (cast(`monitor_time` as date)) VIRTUAL COMMENT '监测日期（由monitor_time生成，方便按天统计）',
  `period_type` enum('DAY','NIGHT') COLLATE utf8mb4_unicode_ci GENERATED ALWAYS AS ((case when ((hour(`monitor_time`) >= 6) and (hour(`monitor_time`) < 22)) then _utf8mb3'DAY' else _utf8mb3'NIGHT' end)) VIRTUAL COMMENT '时段类型（昼间/夜间），根据monitor_time小时数自动判断（6:00-22:00为昼间）',
  `data_source` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT 'SIMULATED' COMMENT '数据来源 (SIMULATED: 模拟, OFFICIAL: 官方)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `deleted` int DEFAULT '0' COMMENT '逻辑删除标记 (0:未删除, 1:已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_station_time` (`station_id`,`monitor_time` DESC),
  KEY `idx_date` (`monitor_date`),
  CONSTRAINT `monitor_data_ibfk_1` FOREIGN KEY (`station_id`) REFERENCES `monitor_station` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=631 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='噪声监测实时数据表';

/*Data for the table `monitor_data` */

insert  into `monitor_data`(`id`,`station_id`,`noise_level`,`monitor_time`,`monitor_date`,`period_type`,`data_source`,`create_time`,`deleted`) values (601,1,'57.63','2026-05-08 17:30:00','2026-05-08','DAY','SIMULATED','2026-05-08 17:30:00',0),(602,2,'60.65','2026-05-08 17:30:00','2026-05-08','DAY','SIMULATED','2026-05-08 17:30:00',0),(603,3,'64.75','2026-05-08 17:30:00','2026-05-08','DAY','SIMULATED','2026-05-08 17:30:00',0),(604,4,'65.91','2026-05-08 17:30:00','2026-05-08','DAY','SIMULATED','2026-05-08 17:30:00',0),(605,5,'64.59','2026-05-08 17:30:00','2026-05-08','DAY','SIMULATED','2026-05-08 17:30:00',0),(606,6,'65.64','2026-05-08 17:30:00','2026-05-08','DAY','SIMULATED','2026-05-08 17:30:00',0),(607,7,'64.73','2026-05-08 17:30:00','2026-05-08','DAY','SIMULATED','2026-05-08 17:30:00',0),(608,8,'63.18','2026-05-08 17:30:00','2026-05-08','DAY','SIMULATED','2026-05-08 17:30:00',0),(609,9,'67.19','2026-05-08 17:30:00','2026-05-08','DAY','SIMULATED','2026-05-08 17:30:00',0),(610,10,'65.99','2026-05-08 17:30:00','2026-05-08','DAY','SIMULATED','2026-05-08 17:30:00',0),(611,11,'64.54','2026-05-08 17:30:00','2026-05-08','DAY','SIMULATED','2026-05-08 17:30:00',0),(612,12,'66.29','2026-05-08 17:30:00','2026-05-08','DAY','SIMULATED','2026-05-08 17:30:00',0),(613,13,'62.99','2026-05-08 17:30:00','2026-05-08','DAY','SIMULATED','2026-05-08 17:30:00',0),(614,14,'64.99','2026-05-08 17:30:00','2026-05-08','DAY','SIMULATED','2026-05-08 17:30:00',0),(615,15,'65.60','2026-05-08 17:30:00','2026-05-08','DAY','SIMULATED','2026-05-08 17:30:00',0),(616,16,'69.19','2026-05-08 17:30:00','2026-05-08','DAY','SIMULATED','2026-05-08 17:30:00',0),(617,17,'66.54','2026-05-08 17:30:00','2026-05-08','DAY','SIMULATED','2026-05-08 17:30:00',0),(618,18,'72.20','2026-05-08 17:30:00','2026-05-08','DAY','SIMULATED','2026-05-08 17:30:00',0),(619,19,'72.61','2026-05-08 17:30:00','2026-05-08','DAY','SIMULATED','2026-05-08 17:30:00',0),(620,20,'70.51','2026-05-08 17:30:00','2026-05-08','DAY','SIMULATED','2026-05-08 17:30:00',0),(621,21,'73.39','2026-05-08 17:30:00','2026-05-08','DAY','SIMULATED','2026-05-08 17:30:00',0),(622,22,'74.33','2026-05-08 17:30:00','2026-05-08','DAY','SIMULATED','2026-05-08 17:30:00',0),(623,23,'74.44','2026-05-08 17:30:00','2026-05-08','DAY','SIMULATED','2026-05-08 17:30:00',0),(624,24,'72.80','2026-05-08 17:30:00','2026-05-08','DAY','SIMULATED','2026-05-08 17:30:00',0),(625,25,'71.49','2026-05-08 17:30:00','2026-05-08','DAY','SIMULATED','2026-05-08 17:30:00',0),(626,26,'78.00','2026-05-08 17:30:00','2026-05-08','DAY','SIMULATED','2026-05-08 17:30:00',0),(627,27,'79.13','2026-05-08 17:30:00','2026-05-08','DAY','SIMULATED','2026-05-08 17:30:00',0),(628,28,'81.73','2026-05-08 17:30:00','2026-05-08','DAY','SIMULATED','2026-05-08 17:30:00',0),(629,29,'77.87','2026-05-08 17:30:00','2026-05-08','DAY','SIMULATED','2026-05-08 17:30:00',0),(630,30,'79.58','2026-05-08 17:30:00','2026-05-08','DAY','SIMULATED','2026-05-08 17:30:00',0);

/*Table structure for table `monitor_station` */

DROP TABLE IF EXISTS `monitor_station`;

CREATE TABLE `monitor_station` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `station_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '监测点名称（如：驿山星光里社区）',
  `district` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所在行政区（如：洪山区）',
  `functional_zone` enum('CLASS_1','CLASS_2','CLASS_3','CLASS_4A','CLASS_4B') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '功能区类型 (1类/2类/3类/4a类/4b类)',
  `longitude` decimal(10,7) NOT NULL COMMENT '经度',
  `latitude` decimal(9,7) NOT NULL COMMENT '纬度',
  `address` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '详细地址',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否启用 (1:启用, 0:停用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` int DEFAULT '0' COMMENT '逻辑删除 (0:正常, 1:删除)',
  PRIMARY KEY (`id`),
  KEY `idx_district` (`district`),
  KEY `idx_zone` (`functional_zone`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='监测点基础信息表';

/*Data for the table `monitor_station` */

insert  into `monitor_station`(`id`,`station_name`,`district`,`functional_zone`,`longitude`,`latitude`,`address`,`is_active`,`create_time`,`update_time`,`deleted`) values (1,'驿山星光里社区','东湖新技术开发区','CLASS_1','114.4041450','30.5064870','驿山南路附近',1,'2026-02-16 16:43:27','2026-02-16 16:43:27',0),(2,'植物园小区','洪山区','CLASS_1','114.3899540','30.5351020','鲁磨路植物园旁',1,'2026-02-16 16:43:27','2026-02-16 16:43:27',0),(3,'江汉路后勤大院','江汉区','CLASS_2','114.2905200','30.5798100','江汉路步行街附近',1,'2026-02-16 16:43:27','2026-02-16 16:43:27',0),(4,'新洲卫健执法大队','新洲区','CLASS_2','114.8021770','30.8415910','新洲区卫生健康执法大队',1,'2026-02-16 16:43:27','2026-02-16 16:43:27',0),(5,'龙头街61号','江夏区','CLASS_2','114.3139630','30.3491720','龙头街61号',1,'2026-02-16 16:43:27','2026-02-16 16:43:27',0),(6,'八一路338号','洪山区','CLASS_2','114.3434150','30.5287420','八一路338号',1,'2026-02-16 16:43:27','2026-02-16 16:43:27',0),(7,'解放路255号','武昌区','CLASS_2','114.3076240','30.5354670','解放路255号',1,'2026-02-16 16:43:27','2026-02-16 16:43:27',0),(8,'临空港大道','东西湖区','CLASS_2','114.0817580','30.6228950','临空港大道',1,'2026-02-16 16:43:27','2026-02-16 16:43:27',0),(9,'七色光幼儿园','汉阳区','CLASS_2','114.2172010','30.5536680','七色光幼儿园',1,'2026-02-16 16:43:27','2026-02-16 16:43:27',0),(10,'汉阳大街42号','蔡甸区','CLASS_2','114.0293420','30.4563540','汉阳大街42号',1,'2026-02-16 16:43:27','2026-02-16 16:43:27',0),(11,'消防人家','江岸区','CLASS_2','114.2841030','30.6116870','消防人家小区',1,'2026-02-16 16:43:27','2026-02-16 16:43:27',0),(12,'中钢集团安环院','青山区','CLASS_2','114.4339630','30.6346560','中钢集团安环院',1,'2026-02-16 16:43:27','2026-02-16 16:43:27',0),(13,'珞狮路322号','洪山区','CLASS_2','114.3437890','30.5058240','珞狮路322号',1,'2026-02-16 16:43:27','2026-02-16 16:43:27',0),(14,'江博社区','汉阳区','CLASS_2','114.2468100','30.5564520','江博社区',1,'2026-02-16 16:43:27','2026-02-16 16:43:27',0),(15,'六合路32号','江岸区','CLASS_2','114.2995450','30.5958740','六合路32号',1,'2026-02-16 16:43:27','2026-02-16 16:43:27',0),(16,'纱帽正街326号','汉南区','CLASS_2','114.0812450','30.3094560','纱帽正街326号',1,'2026-02-16 16:43:27','2026-02-16 16:43:27',0),(17,'常青五路5号','江汉区','CLASS_2','114.2374560','30.6207890','常青五路5号',1,'2026-02-16 16:43:27','2026-02-16 16:43:27',0),(18,'阳逻税务所','新洲区','CLASS_3','114.5723480','30.6981230','阳逻税务所',1,'2026-02-16 16:43:27','2026-02-16 16:43:27',0),(19,'普能科技园','东湖新技术开发区','CLASS_3','114.4223450','30.4887650','普能科技园',1,'2026-02-16 16:43:27','2026-02-16 16:43:27',0),(20,'沌口新区','汉南区','CLASS_3','114.1456780','30.4567890','沌口新区',1,'2026-02-16 16:43:27','2026-02-16 16:43:27',0),(21,'吴家山经开区','东西湖区','CLASS_3','114.1234560','30.6345670','吴家山经济技术开发区',1,'2026-02-16 16:43:27','2026-02-16 16:43:27',0),(22,'星光大道88号','蔡甸区','CLASS_3','113.9567890','30.4876540','星光大道88号',1,'2026-02-16 16:43:27','2026-02-16 16:43:27',0),(23,'黄陂冯湾','黄陂区','CLASS_3','114.3765430','30.8765430','黄陂冯湾',1,'2026-02-16 16:43:27','2026-02-16 16:43:27',0),(24,'胡刘家湾','江夏区','CLASS_3','114.2654320','30.2987650','胡刘家湾',1,'2026-02-16 16:43:27','2026-02-16 16:43:27',0),(25,'宝武金属资源','青山区','CLASS_3','114.3987650','30.6234560','宝武金属资源',1,'2026-02-16 16:43:27','2026-02-16 16:43:27',0),(26,'百泰路','黄陂区','CLASS_4A','114.3761890','30.8854320','百泰路',1,'2026-02-16 16:43:27','2026-02-16 16:43:27',0),(27,'沿河大道','硚口区','CLASS_4A','114.2543210','30.5678900','沿河大道',1,'2026-02-16 16:43:27','2026-02-16 16:43:27',0),(28,'余泊大道','新洲区','CLASS_4A','114.7890120','30.8345670','余泊大道',1,'2026-02-16 16:43:27','2026-02-16 16:43:27',0),(29,'光谷四路','东湖新技术开发区','CLASS_4A','114.5012340','30.4765430','光谷四路',1,'2026-02-16 16:43:27','2026-02-16 16:43:27',0),(30,'京广铁路','武昌区','CLASS_4B','114.3123450','30.5234560','京广铁路沿线',1,'2026-02-16 16:43:27','2026-02-16 16:43:27',0);

/*Table structure for table `noise_standard` */

DROP TABLE IF EXISTS `noise_standard`;

CREATE TABLE `noise_standard` (
  `id` int NOT NULL AUTO_INCREMENT,
  `functional_zone` enum('CLASS_1','CLASS_2','CLASS_3','CLASS_4A','CLASS_4B') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '功能区类型',
  `day_limit` decimal(5,2) NOT NULL COMMENT '昼间标准限值(dB)',
  `night_limit` decimal(5,2) NOT NULL COMMENT '夜间标准限值(dB)',
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` int DEFAULT '0' COMMENT '逻辑删除标记 (0:未删除, 1:已删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `functional_zone` (`functional_zone`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='噪声标准阈值配置表（国标）';

/*Data for the table `noise_standard` */

insert  into `noise_standard`(`id`,`functional_zone`,`day_limit`,`night_limit`,`description`,`update_time`,`deleted`) values (1,'CLASS_1','55.00','45.00','1类区：康复疗养区等特别需要安静的区域','2026-02-16 16:43:27',0),(2,'CLASS_2','60.00','50.00','2类区：以商业金融、集市贸易为主要功能，或者居住、商业、工业混杂，需要维护住宅安静的区域','2026-02-16 16:43:27',0),(3,'CLASS_3','65.00','55.00','3类区：以工业生产、仓储物流为主要功能，需要防止工业噪声对周围环境产生严重影响的区域','2026-02-16 16:43:27',0),(4,'CLASS_4A','70.00','55.00','4a类区：高速公路、一级公路、二级公路、城市快速路、城市主干路、城市次干路、城市轨道交通（地面段）、内河航道两侧区域','2026-02-16 16:43:27',0),(5,'CLASS_4B','70.00','60.00','4b类区：铁路干线两侧区域','2026-02-16 16:43:27',0);

/*Table structure for table `notifications` */

DROP TABLE IF EXISTS `notifications`;

CREATE TABLE `notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL COMMENT '接收用户ID（NULL表示全体通知）',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知标题',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知内容',
  `type` enum('SYSTEM','TASK','COMPLAINT') COLLATE utf8mb4_unicode_ci DEFAULT 'SYSTEM' COMMENT '通知类型',
  `related_id` bigint DEFAULT NULL COMMENT '关联ID（如投诉ID）',
  `is_read` tinyint(1) DEFAULT '0' COMMENT '是否已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `deleted` int DEFAULT '0' COMMENT '逻辑删除标记 (0:未删除, 1:已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_user_unread` (`user_id`,`is_read`),
  CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知表';

/*Data for the table `notifications` */

insert  into `notifications`(`id`,`user_id`,`title`,`content`,`type`,`related_id`,`is_read`,`create_time`,`deleted`) values (1,5,'投诉处理开始','您的投诉已开始处理，请耐心等待','COMPLAINT',1,1,'2026-02-26 14:00:27',0),(5,5,'处理进度更新','您的投诉处理进度已更新: test4','COMPLAINT',1,1,'2026-02-26 14:21:10',0),(6,5,'投诉处理完成','您的投诉已处理完成，感谢您的反馈','COMPLAINT',1,1,'2026-02-26 14:21:29',0),(7,5,'投诉状态更新','您的投诉已标记为未解决，处理人员将重新处理','COMPLAINT',1,1,'2026-02-26 14:22:33',0),(8,5,'投诉处理完成','您的投诉已处理完成，感谢您的反馈','COMPLAINT',1,1,'2026-02-26 14:27:22',0),(9,5,'投诉状态更新','您的投诉已标记为未解决，处理人员将重新处理','COMPLAINT',1,1,'2026-02-26 14:28:50',0),(10,5,'投诉处理完成','您的投诉已处理完成，感谢您的反馈','COMPLAINT',1,1,'2026-02-26 14:31:22',0),(11,5,'投诉状态更新','您的投诉已标记为未解决，处理人员将重新处理','COMPLAINT',1,1,'2026-02-26 14:31:41',0),(12,5,'投诉处理完成','您的投诉已处理完成，感谢您的反馈','COMPLAINT',1,1,'2026-02-26 14:35:29',0),(13,5,'投诉状态更新','您的投诉已标记为未解决，处理人员将重新处理','COMPLAINT',1,1,'2026-02-26 14:35:43',0),(14,5,'投诉处理完成','您的投诉已处理完成，感谢您的反馈','COMPLAINT',1,1,'2026-02-26 14:38:09',0),(15,5,'投诉状态更新','您的投诉已标记为未解决，处理人员将重新处理','COMPLAINT',1,1,'2026-02-26 14:38:22',0),(16,5,'投诉处理完成','您的投诉已处理完成，感谢您的反馈','COMPLAINT',1,1,'2026-02-26 14:41:47',0),(17,5,'投诉状态更新','您的投诉已标记为未解决，处理人员将重新处理','COMPLAINT',1,1,'2026-02-26 14:42:01',0),(18,5,'投诉处理完成','您的投诉已处理完成，感谢您的反馈','COMPLAINT',1,1,'2026-02-26 14:44:57',0),(19,5,'投诉状态更新','您的投诉已标记为未解决，处理人员将重新处理','COMPLAINT',1,1,'2026-02-26 14:45:13',0),(20,5,'投诉处理完成','您的投诉已处理完成，感谢您的反馈','COMPLAINT',1,1,'2026-02-26 14:54:19',0),(21,5,'投诉未解决','您处理的投诉被标记为未解决，请重新处理','TASK',1,1,'2026-02-26 14:54:29',0),(22,5,'投诉状态更新','您的投诉已标记为未解决，处理人员将重新处理','COMPLAINT',1,1,'2026-02-26 14:54:29',0),(23,5,'投诉处理完成','您的投诉已处理完成，感谢您的反馈','COMPLAINT',1,1,'2026-02-26 15:02:03',0),(24,5,'投诉状态更新','您的投诉已标记为未解决，处理人员将重新处理','COMPLAINT',1,1,'2026-02-26 15:02:14',0),(25,5,'投诉处理完成','您的投诉已处理完成，感谢您的反馈','COMPLAINT',1,1,'2026-02-26 15:05:07',0),(26,5,'投诉状态更新','您的投诉已标记为未解决，处理人员将重新处理','COMPLAINT',1,1,'2026-02-26 15:05:42',0),(27,5,'投诉处理完成','您的投诉已处理完成，感谢您的反馈','COMPLAINT',1,1,'2026-02-26 15:27:51',0),(28,2,'投诉未解决','您处理的投诉被标记为未解决，请重新处理','TASK',1,1,'2026-02-26 15:28:03',0),(29,5,'投诉状态更新','您的投诉已标记为未解决，处理人员将重新处理','COMPLAINT',1,1,'2026-02-26 15:28:03',0),(30,5,'投诉处理完成','您的投诉已处理完成，感谢您的反馈','COMPLAINT',1,1,'2026-02-26 15:28:21',0),(31,5,'投诉处理完成','您的投诉已处理完成，感谢您的反馈','COMPLAINT',1,1,'2026-02-26 15:32:06',0),(32,5,'投诉已删除','您的投诉已成功删除','COMPLAINT',2,1,'2026-02-27 16:36:03',0),(33,2,'新任务分配','您有新的噪音投诉处理任务，请及时处理','TASK',4,1,'2026-03-18 20:57:10',0),(34,5,'投诉已分配','您的投诉已分配给处理人员，我们会尽快处理','COMPLAINT',4,0,'2026-03-18 20:57:10',0),(35,5,'投诉处理开始','您的投诉已开始处理，请耐心等待','COMPLAINT',4,0,'2026-03-19 17:07:51',0),(36,2,'新任务分配','您有新的噪音投诉处理任务，请及时处理','TASK',3,0,'2026-03-27 17:38:30',0),(37,5,'投诉已分配','您的投诉已分配给处理人员，我们会尽快处理','COMPLAINT',3,0,'2026-03-27 17:38:30',0);

/*Table structure for table `ratings` */

DROP TABLE IF EXISTS `ratings`;

CREATE TABLE `ratings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `complaint_id` bigint NOT NULL COMMENT '投诉ID',
  `score` tinyint NOT NULL COMMENT '评分(1-5)',
  `comment` text COLLATE utf8mb4_unicode_ci COMMENT '评价内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `deleted` int DEFAULT '0' COMMENT '逻辑删除标记 (0:未删除, 1:已删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `complaint_id` (`complaint_id`),
  CONSTRAINT `ratings_ibfk_1` FOREIGN KEY (`complaint_id`) REFERENCES `complaints` (`id`) ON DELETE CASCADE,
  CONSTRAINT `ratings_chk_1` CHECK (((`score` >= 1) and (`score` <= 5)))
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投诉评价表';

/*Data for the table `ratings` */

insert  into `ratings`(`id`,`complaint_id`,`score`,`comment`,`create_time`,`deleted`) values (1,1,5,'test3','2026-02-26 15:41:02',0);

/*Table structure for table `users` */

DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '加密密码',
  `real_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '真实姓名',
  `role` enum('RESIDENT','WORKER','ADMIN') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '头像URL',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `deleted` int DEFAULT '0' COMMENT '逻辑删除标记 (0:未删除, 1:已删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `idx_username` (`username`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

/*Data for the table `users` */

insert  into `users`(`id`,`username`,`password`,`real_name`,`role`,`phone`,`avatar`,`create_time`,`deleted`) values (1,'admin','0192023a7bbd73250516f069df18b500','系统管理员','ADMIN','13800138000','','2026-02-26 13:08:30',0),(2,'worker1','d5700513458d427810d14b6575fba6f6','处理员张三','WORKER','13800138001','','2026-02-26 13:08:40',0),(3,'resident1','4e74612466aa473adba4e4f77e14e50a','居民李四','RESIDENT','13800138002','','2026-02-26 13:09:04',0),(4,'test1','96e79218965eb72c92a549dd5a330112','测试用户1','RESIDENT','13800138003','','2026-02-26 13:09:20',0),(5,'test2','96e79218965eb72c92a549dd5a330112','test2','RESIDENT','13800138004','','2026-02-26 13:09:51',0),(8,'test3','96e79218965eb72c92a549dd5a330112','测试处理人员','RESIDENT','13211113123','/uploads/default-avatar.png','2026-03-01 13:44:53',0),(9,'worker2','96e79218965eb72c92a549dd5a330112','处理人员李四','WORKER','13212333123','/uploads/default-avatar.png','2026-03-01 13:51:45',0),(10,'testuser','e10adc3949ba59abbe56e057f20f883e','????','RESIDENT',NULL,'/uploads/default-avatar.png','2026-03-29 20:29:23',0),(11,'worker','e10adc3949ba59abbe56e057f20f883e','????','RESIDENT',NULL,'/uploads/default-avatar.png','2026-03-29 21:08:26',0);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
