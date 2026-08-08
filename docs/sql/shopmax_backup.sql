mysqldump: [Warning] Using a password on the command line interface can be insecure.
-- MySQL dump 10.13  Distrib 8.0.33, for Win64 (x86_64)
--
-- Host: localhost    Database: shopmax
-- ------------------------------------------------------
-- Server version	8.0.33

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
-- Table structure for table `cms_banner`
--

DROP TABLE IF EXISTS `cms_banner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cms_banner` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片URL',
  `link_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '跳转链接',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_status_sort` (`status`,`sort`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='首页轮播图表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cms_banner`
--

LOCK TABLES `cms_banner` WRITE;
/*!40000 ALTER TABLE `cms_banner` DISABLE KEYS */;
INSERT INTO `cms_banner` VALUES (1,'时尚衣橱换新季，全场最低7折','http://localhost:9000/shopmax/banner/20260618/9cfaa623.png','/pages/search/index',1,1,'2026-05-30 11:40:40','2026-05-30 11:40:40',0),(2,'美味食光不打烊，快乐翻倍','http://localhost:9000/shopmax/banner/20260618/03b92e3e.png','/pages/search/index',2,1,'2026-05-30 11:40:40','2026-05-30 11:40:40',0),(3,'智能生活新体验，更多好物等你挑','http://localhost:9000/shopmax/banner/20260618/ea52cb38.png','/pages/coupon/center',3,1,'2026-05-30 11:40:40','2026-05-30 11:40:40',0),(4,'111','http://localhost:9000/shopmax/banner/20260602/434c0723.png','',4,1,'2026-05-30 11:40:40','2026-06-02 22:59:29',1),(5,'222','http://localhost:9000/shopmax/banner/20260602/9fb14391.jpg','',5,1,'2026-06-02 22:59:00','2026-06-02 22:59:21',1);
/*!40000 ALTER TABLE `cms_banner` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cms_note`
--

DROP TABLE IF EXISTS `cms_note`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cms_note` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '笔记ID',
  `user_id` bigint NOT NULL COMMENT '作者用户ID',
  `title` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '笔记标题',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '笔记正文',
  `cover_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '封面图URL',
  `content_type` tinyint NOT NULL DEFAULT '1' COMMENT '内容类型: 1-图文 2-视频(P2)',
  `video_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '视频URL(P2预留)',
  `video_duration` int DEFAULT NULL COMMENT '视频时长秒数(P2预留)',
  `status` tinyint NOT NULL DEFAULT '3' COMMENT '状态: 1-草稿 2-已发布 3-审核中 4-已驳回 5-已删除',
  `reject_reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '驳回原因',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_user_id` bigint DEFAULT NULL COMMENT '审核人ID',
  `view_count` int NOT NULL DEFAULT '0' COMMENT '浏览数',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数(冗余)',
  `comment_count` int NOT NULL DEFAULT '0' COMMENT '评论数(冗余)',
  `favorite_count` int NOT NULL DEFAULT '0' COMMENT '收藏数(冗余)',
  `share_count` int NOT NULL DEFAULT '0' COMMENT '分享数(冗余)',
  `is_recommended` tinyint NOT NULL DEFAULT '0' COMMENT '是否推荐: 0-否 1-是(P2)',
  `is_top` tinyint NOT NULL DEFAULT '0' COMMENT '是否置顶: 0-否 1-是(P2)',
  `is_essence` tinyint NOT NULL DEFAULT '0' COMMENT '是否加精: 0-否 1-是(P2)',
  `longitude` decimal(10,7) DEFAULT NULL COMMENT '经度(P2预留)',
  `latitude` decimal(10,7) DEFAULT NULL COMMENT '纬度(P2预留)',
  `location_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '位置名称(P2预留)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_content_type` (`content_type`),
  KEY `idx_is_recommended` (`is_recommended`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_user_status_time` (`user_id`,`status`,`create_time`),
  KEY `idx_status_time` (`status`,`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='种草笔记主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cms_note`
--

LOCK TABLES `cms_note` WRITE;
/*!40000 ALTER TABLE `cms_note` DISABLE KEYS */;
INSERT INTO `cms_note` VALUES (1,2,'夏日穿搭分享 — 超舒服的纯棉T恤','这件T恤真的太舒服了！面料柔软透气，夏天穿完全不会闷热，推荐给大家～\n\n我买了白色和黑色两个颜色，都超级百搭。搭配牛仔裤或者短裙都可以，质量也很好，洗了几次都没有变形。','https://picsum.photos/seed/note1/400/400',1,NULL,NULL,2,NULL,NULL,NULL,1291,128,15,42,0,1,0,0,NULL,NULL,NULL,'2026-05-21 23:45:13','2026-05-28 23:00:51',0),(2,3,'蓝牙耳机深度测评 | 百元价位千元体验','用了一周这款蓝牙耳机，音质出乎意料的好。降噪效果在通勤时非常实用，续航也能满足一天的使用。\n\n主要优点：\n1. 音质清晰，低音饱满\n2. 佩戴舒适，长时间不累\n3. 续航约6小时\n\n缺点：充电盒有点大','https://picsum.photos/seed/note2/400/400',1,NULL,NULL,2,'1','2026-05-25 16:54:01',NULL,2569,230,42,88,0,1,0,0,NULL,NULL,NULL,'2026-05-21 23:45:13','2026-06-08 23:02:39',0),(3,2,'租房好物清单 | 小空间收纳神器','搬进30平小公寓后，收纳成了最大的问题。整理了这一个月来觉得最实用的收纳好物～\n\n1. 床底收纳箱 - 换季衣物终于有地方放了\n2. 门后挂钩架 - 包包帽子全部上墙\n3. 免打孔置物架 - 卫生间神器','https://picsum.photos/seed/note3/400/400',1,NULL,NULL,2,NULL,NULL,NULL,986,86,12,30,0,0,0,0,NULL,NULL,NULL,'2026-05-21 23:45:13','2026-05-28 23:00:56',0),(4,3,'平价口红试色合集 💋 10支50元以内','整理了最近入手的10支平价口红，每一支都不超过50元！学生党友好～\n\n试色从豆沙色到正红色都有，黄皮友好度也标注了。个人最推荐第3支，显白又日常。','https://picsum.photos/seed/note4/400/400',1,NULL,NULL,2,NULL,'2026-05-25 15:26:38',NULL,7,1,0,0,0,0,0,0,NULL,NULL,NULL,'2026-05-21 23:45:13','2026-06-01 18:07:18',0),(5,1,'周末烘焙 | 零失败的巧克力熔岩蛋糕','第一次做就成功了！分享这个超简单的配方～\n\n材料：黑巧克力100g、黄油50g、鸡蛋2个、低筋面粉30g、糖40g\n\n步骤：\n1. 巧克力和黄油隔水融化\n2. 鸡蛋加糖打发\n3. 混合后筛入面粉\n4. 200度烤12分钟','https://picsum.photos/seed/note5/400/400',1,NULL,NULL,4,'敏感内容','2026-05-25 15:56:02',NULL,1898,196,28,65,0,0,0,0,NULL,NULL,NULL,'2026-05-21 23:45:13','2026-05-25 16:53:42',0),(6,2,'iPhone 15 Pro Max 一个月使用感受','从安卓换到iPhone，最大的感受就是系统流畅度确实不一样。\n\n拍照方面：人像模式进步很大，暗光表现也很不错\n续航：一天一充够用\n缺点：充电速度还是偏慢\n\n总体来说值得入手！','https://picsum.photos/seed/note6/400/400',1,NULL,NULL,2,NULL,NULL,NULL,3477,313,56,119,0,1,0,0,NULL,NULL,NULL,'2026-05-21 23:45:13','2026-06-10 17:38:29',0),(7,18,'iPhone 13','终于到货啦！！哇咔咔咔！！！！','http://localhost:9000/shopmax/community/20260529/45a7a346.jpg',1,NULL,NULL,2,NULL,'2026-05-29 19:18:56',NULL,9,1,2,0,0,0,0,0,NULL,NULL,NULL,'2026-05-29 19:17:33','2026-06-10 17:38:25',0);
/*!40000 ALTER TABLE `cms_note` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cms_note_comment`
--

DROP TABLE IF EXISTS `cms_note_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cms_note_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `note_id` bigint NOT NULL COMMENT '笔记ID',
  `user_id` bigint NOT NULL COMMENT '评论用户ID',
  `parent_id` bigint DEFAULT NULL COMMENT '父评论ID(NULL=一级评论, 非NULL=二级回复)',
  `reply_to_user_id` bigint DEFAULT NULL COMMENT '被回复用户ID',
  `content` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_note_id` (`note_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_note_parent_time` (`note_id`,`parent_id`,`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记评论表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cms_note_comment`
--

LOCK TABLES `cms_note_comment` WRITE;
/*!40000 ALTER TABLE `cms_note_comment` DISABLE KEYS */;
INSERT INTO `cms_note_comment` VALUES (1,1,3,NULL,NULL,'颜色好看！我也买了同款，质量真的不错 👍',0,'2026-05-21 23:49:03','2026-05-21 23:49:03',0),(2,1,1,NULL,NULL,'尺码偏大还是偏小？想入手一件',0,'2026-05-21 23:49:03','2026-05-21 23:49:03',0),(3,1,2,2,1,'我觉得码数正常，按平时穿的选就行～',0,'2026-05-21 23:49:03','2026-05-21 23:49:03',0),(4,2,1,NULL,NULL,'这款我也在用，降噪确实不错',0,'2026-05-21 23:49:03','2026-05-21 23:49:03',0),(5,2,2,NULL,NULL,'音质和AirPods比怎么样？',0,'2026-05-21 23:49:03','2026-05-21 23:49:03',0),(6,2,3,5,2,'个人觉得差距不大，这个价格性价比很高',0,'2026-05-21 23:49:03','2026-05-21 23:49:03',0),(7,3,3,NULL,NULL,'床底收纳箱求链接！',0,'2026-05-21 23:49:03','2026-05-21 23:49:03',0),(8,5,2,NULL,NULL,'周末试了一下，真的零失败！感谢分享',0,'2026-05-21 23:49:03','2026-05-21 23:49:03',0),(9,5,3,8,2,'开心！烤好的时候整个厨房都是巧克力香味',0,'2026-05-21 23:49:03','2026-05-21 23:49:03',0),(10,6,3,NULL,NULL,'拍照效果确实好，尤其人像模式',0,'2026-05-21 23:49:03','2026-05-21 23:49:03',0),(11,6,1,NULL,NULL,'续航能撑一天重度使用吗？',0,'2026-05-21 23:49:03','2026-05-21 23:49:03',0),(12,6,2,11,1,'重度使用的话下午需要充一次，轻度完全够',0,'2026-05-21 23:49:03','2026-05-21 23:49:03',0),(14,7,1,NULL,NULL,'感谢支持本平台，本平台定期会发放海量优惠卷，期待您的下次好货分享！OvO',0,'2026-06-01 18:06:58','2026-06-01 18:06:58',0);
/*!40000 ALTER TABLE `cms_note_comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cms_note_favorite`
--

DROP TABLE IF EXISTS `cms_note_favorite`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cms_note_favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `note_id` bigint NOT NULL COMMENT '笔记ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_note_user` (`note_id`,`user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_note_id` (`note_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记收藏表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cms_note_favorite`
--

LOCK TABLES `cms_note_favorite` WRITE;
/*!40000 ALTER TABLE `cms_note_favorite` DISABLE KEYS */;
INSERT INTO `cms_note_favorite` VALUES (2,1,3,'2026-05-21 23:49:03'),(4,2,4,'2026-05-21 23:49:03'),(5,3,3,'2026-05-21 23:49:03'),(6,5,1,'2026-05-21 23:49:03'),(7,5,2,'2026-05-21 23:49:03'),(9,6,2,'2026-05-21 23:49:03'),(10,6,3,'2026-05-21 23:49:03'),(12,1,1,'2026-05-28 22:59:50'),(16,2,1,'2026-06-08 23:02:40');
/*!40000 ALTER TABLE `cms_note_favorite` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cms_note_image`
--

DROP TABLE IF EXISTS `cms_note_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cms_note_image` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `note_id` bigint NOT NULL COMMENT '笔记ID',
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片URL',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序序号',
  `width` int DEFAULT NULL COMMENT '图片宽度(P2预留)',
  `height` int DEFAULT NULL COMMENT '图片高度(P2预留)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_note_id` (`note_id`),
  KEY `idx_note_sort` (`note_id`,`sort_order`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记图片表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cms_note_image`
--

LOCK TABLES `cms_note_image` WRITE;
/*!40000 ALTER TABLE `cms_note_image` DISABLE KEYS */;
INSERT INTO `cms_note_image` VALUES (1,1,'https://picsum.photos/seed/img1a/800/800',0,NULL,NULL,'2026-05-21 23:45:13'),(2,1,'https://picsum.photos/seed/img1b/800/800',1,NULL,NULL,'2026-05-21 23:45:13'),(3,1,'https://picsum.photos/seed/img1c/800/800',2,NULL,NULL,'2026-05-21 23:45:13'),(4,2,'https://picsum.photos/seed/img2a/800/800',0,NULL,NULL,'2026-05-21 23:45:13'),(5,2,'https://picsum.photos/seed/img2b/800/800',1,NULL,NULL,'2026-05-21 23:45:13'),(6,3,'https://picsum.photos/seed/img3a/800/800',0,NULL,NULL,'2026-05-21 23:45:13'),(7,4,'https://picsum.photos/seed/img4a/800/800',0,NULL,NULL,'2026-05-21 23:45:13'),(8,5,'https://picsum.photos/seed/img5a/800/800',0,NULL,NULL,'2026-05-21 23:45:13'),(9,6,'https://picsum.photos/seed/img6a/800/800',0,NULL,NULL,'2026-05-21 23:45:13'),(10,6,'https://picsum.photos/seed/img6b/800/800',1,NULL,NULL,'2026-05-21 23:45:13'),(11,7,'http://localhost:9000/shopmax/community/20260529/45a7a346.jpg',0,NULL,NULL,'2026-05-29 19:17:33');
/*!40000 ALTER TABLE `cms_note_image` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cms_note_like`
--

DROP TABLE IF EXISTS `cms_note_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cms_note_like` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `note_id` bigint NOT NULL COMMENT '笔记ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_note_user` (`note_id`,`user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_note_id` (`note_id`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记点赞表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cms_note_like`
--

LOCK TABLES `cms_note_like` WRITE;
/*!40000 ALTER TABLE `cms_note_like` DISABLE KEYS */;
INSERT INTO `cms_note_like` VALUES (2,1,3,'2026-05-21 23:45:13'),(3,1,4,'2026-05-21 23:45:13'),(4,2,1,'2026-05-21 23:45:13'),(5,2,2,'2026-05-21 23:45:13'),(7,3,3,'2026-05-21 23:45:13'),(8,3,4,'2026-05-21 23:45:13'),(9,5,2,'2026-05-21 23:45:13'),(10,5,3,'2026-05-21 23:45:13'),(11,5,4,'2026-05-21 23:45:13'),(12,6,2,'2026-05-21 23:45:13'),(13,6,3,'2026-05-21 23:45:13'),(18,1,1,'2026-05-28 23:00:52'),(19,3,1,'2026-05-28 23:00:57'),(20,4,1,'2026-05-28 23:01:02'),(21,6,1,'2026-05-28 23:01:06'),(28,7,1,'2026-06-01 18:07:13');
/*!40000 ALTER TABLE `cms_note_like` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cms_note_product`
--

DROP TABLE IF EXISTS `cms_note_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cms_note_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `note_id` bigint NOT NULL COMMENT '笔记ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_note_id` (`note_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记关联商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cms_note_product`
--

LOCK TABLES `cms_note_product` WRITE;
/*!40000 ALTER TABLE `cms_note_product` DISABLE KEYS */;
INSERT INTO `cms_note_product` VALUES (1,1,1,'2026-05-21 23:45:13'),(2,2,2,'2026-05-21 23:45:13'),(3,6,3,'2026-05-21 23:45:13');
/*!40000 ALTER TABLE `cms_note_product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cms_search_keyword`
--

DROP TABLE IF EXISTS `cms_search_keyword`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cms_search_keyword` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `keyword` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '搜索关键词',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `search_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '搜索时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_keyword` (`keyword`),
  KEY `idx_search_time` (`search_time`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索关键词记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cms_search_keyword`
--

LOCK TABLES `cms_search_keyword` WRITE;
/*!40000 ALTER TABLE `cms_search_keyword` DISABLE KEYS */;
/*!40000 ALTER TABLE `cms_search_keyword` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `csms_faq`
--

DROP TABLE IF EXISTS `csms_faq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `csms_faq` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category` varchar(64) DEFAULT NULL COMMENT '分类: 支付/退换货/配送/发票/会员/售后/其他',
  `question` varchar(512) NOT NULL COMMENT '问题',
  `answer` text NOT NULL COMMENT '答案',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态: 0禁用 1启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='FAQ知识库表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `csms_faq`
--

LOCK TABLES `csms_faq` WRITE;
/*!40000 ALTER TABLE `csms_faq` DISABLE KEYS */;
INSERT INTO `csms_faq` VALUES (1,'支付','支持哪些支付方式？','我们支持微信支付、支付宝、银行卡支付三种方式，您可以在结算页面选择最方便的支付方式。',1,1,'2026-06-02 08:56:43','2026-06-02 08:56:43',0),(2,'支付','支付失败怎么办？','请先检查银行卡余额是否充足，或尝试更换支付方式。如果多次尝试仍失败，请在聊天窗口输入\"人工客服\"寻求帮助。',2,1,'2026-06-02 08:56:43','2026-06-02 08:56:43',0),(3,'支付','支付后多久确认到账？','微信支付和支付宝通常是即时到账。银行卡支付根据银行不同可能需要1-3分钟。如果超过10分钟仍未确认，请联系客服。',3,1,'2026-06-02 08:56:43','2026-06-02 08:56:43',0),(4,'支付','可以使用优惠券吗？','可以的！在结算页面点击\"使用优惠券\"，选择您要使用的优惠券即可自动抵扣。注意每笔订单只能使用一张优惠券。',4,1,'2026-06-02 08:56:43','2026-06-02 08:56:43',0),(5,'退换货','退货流程是什么？','在\"我的订单\"中找到对应订单，点击\"申请退货\"，填写退货原因并上传凭证照片，提交后等待审核。审核通过后，您会收到退货地址，按指引寄回商品并填写运单号即可。',5,1,'2026-06-02 08:56:43','2026-06-02 08:56:43',0),(6,'退换货','多久可以退货？','签收后7天内可申请无理由退货，商品需保持原包装完好、不影响二次销售。如果是质量问题，请在签收后24小时内拍照联系客服处理。',6,1,'2026-06-02 08:56:43','2026-06-02 08:56:43',0),(7,'退换货','退款多久到账？','审核通过后，微信/支付宝退款1-3个工作日到账，银行卡退款3-7个工作日到账。您可以在\"我的订单\"中查看退款进度。',7,1,'2026-06-02 08:56:43','2026-06-02 08:56:43',0),(8,'退换货','退货的运费谁承担？','因商品质量问题导致的退货，运费由我们承担（请先垫付，收到退货后返还）。非质量问题的无理由退货，运费由您承担。',8,1,'2026-06-02 08:56:43','2026-06-02 08:56:43',0),(9,'退换货','换货怎么操作？','目前暂不支持直接换货。您可以先申请退货退款，再重新下单购买需要的商品。给您带来不便敬请谅解。',9,1,'2026-06-02 08:56:43','2026-06-02 08:56:43',0),(10,'配送','多久能发货？','正常订单下单后24小时内发货（节假日顺延）。预售商品按商品页面标注的时间发货，请您留意商品详情页的预售说明。',10,1,'2026-06-02 08:56:43','2026-06-02 08:56:43',0),(11,'配送','如何查询物流？','在\"我的订单\"中找到对应订单，点击进入订单详情页即可查看实时物流跟踪信息。',11,1,'2026-06-02 08:56:43','2026-06-02 08:56:43',0),(12,'配送','配送范围和费用？','全国大部分地区包邮。部分偏远地区（新疆、西藏、青海等）可能产生额外运费，系统会在下单时自动计算并提示。',12,1,'2026-06-02 08:56:43','2026-06-02 08:56:43',0),(13,'配送','可以修改收货地址吗？','下单后如果还未发货，可以在\"我的订单\"中修改收货地址。如果已经发货，请联系客服协助处理。',13,1,'2026-06-02 08:56:43','2026-06-02 08:56:43',0),(14,'配送','收到包裹破损怎么办？','请先拍照保留证据（外包装和商品），然后拒收或在签收后24小时内联系客服，我们将为您处理补发或退款。',14,1,'2026-06-02 08:56:43','2026-06-02 08:56:43',0),(15,'发票','如何开发票？','下单时在结算页面的\"发票信息\"栏填写开票信息即可。我们支持电子发票和纸质发票两种形式，电子发票会发送到您的邮箱。',15,1,'2026-06-02 08:56:43','2026-06-02 08:56:43',0),(16,'发票','发票可以补开吗？','下单后30天内可以在\"我的订单\"中找到对应订单申请补开发票。超过30天的订单请联系客服协助处理。',16,1,'2026-06-02 08:56:43','2026-06-02 08:56:43',0),(17,'发票','发票抬头写错了怎么办？','如果发票还未开具，可以在订单详情中修改发票信息。如果已经开具，请联系客服作废原发票后重新开具。',17,1,'2026-06-02 08:56:43','2026-06-02 08:56:43',0),(18,'会员','会员有什么权益？','会员享受专属折扣价、生日礼包、双倍积分、优先客服、专属活动等权益。等级越高，权益越多！详情可在\"我的-会员中心\"查看。',18,1,'2026-06-02 08:56:43','2026-06-02 08:56:43',0),(19,'会员','如何成为会员？','注册即为基础会员。累计消费满1000元自动升级为银卡会员，满5000元升级为金卡会员，满10000元升级为钻石会员。',19,1,'2026-06-02 08:56:43','2026-06-02 08:56:43',0),(20,'会员','积分有什么用？','积分可以在下单时抵扣现金（100积分=1元），也可以在积分商城兑换商品或优惠券。积分有效期为获得之日起1年。',20,1,'2026-06-02 08:56:43','2026-06-02 08:56:43',0),(21,'会员','积分怎么获取？','消费1元得1积分，每日签到可得5-20积分，参与平台活动可以获得额外积分奖励。',21,1,'2026-06-02 08:56:43','2026-06-02 08:56:43',0),(22,'售后','商品有质量问题怎么办？','请在签收后24小时内拍照或录视频联系客服，我们将核实后为您办理换货或退款，并承担来回运费。',22,1,'2026-06-02 08:56:43','2026-06-02 08:56:43',0),(23,'售后','收到的商品和描述不符？','请拍照保留证据并联系客服，我们核实后将为您办理退货退款，运费由我们承担。给您带来的不便深表歉意。',23,1,'2026-06-02 08:56:43','2026-06-02 08:56:43',0),(24,'售后','商品漏发了怎么办？','请先核对包裹内的发货清单，确认后联系客服，我们会尽快为您补发漏发的商品。',24,1,'2026-06-02 08:56:43','2026-06-02 08:56:43',0),(25,'售后','如何联系人工客服？','工作时间（9:00-21:00）在本聊天窗口输入\"人工客服\"即可转接人工服务。非工作时间可以留言，我们会尽快回复。',25,1,'2026-06-02 08:56:43','2026-06-02 08:56:43',0),(26,'其他','如何修改密码？','在\"我的-设置-账户安全\"中点击\"修改密码\"，输入原密码和新密码即可完成修改。',26,1,'2026-06-02 08:56:43','2026-06-02 08:56:43',0),(27,'其他','账号被盗怎么办？','请立即联系客服冻结账号，然后通过\"忘记密码\"功能重置密码。建议开启手机验证登录提高安全性。',27,1,'2026-06-02 08:56:43','2026-06-02 08:56:43',0),(28,'其他','可以注销账号吗？','在\"我的-设置-账户安全\"中申请注销账号。注销后所有数据将被永久删除且不可恢复，请谨慎操作。有未完成订单时无法注销。',28,1,'2026-06-02 08:56:43','2026-06-02 08:56:43',0),(29,'其他','App支持哪些手机系统？','我们支持iOS 13.0及以上版本、Android 8.0及以上版本。您也可以在手机浏览器中访问我们的H5网页版。',29,1,'2026-06-02 08:56:43','2026-06-02 08:56:43',0),(30,'其他','营业时间是什么时候？','我们的在线客服工作时间为每天9:00-21:00（含节假日）。您也可以随时使用智能客服助手查询常见问题。',30,1,'2026-06-02 08:56:43','2026-06-02 08:56:43',0);
/*!40000 ALTER TABLE `csms_faq` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `csms_message`
--

DROP TABLE IF EXISTS `csms_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `csms_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL COMMENT '会话ID',
  `role` varchar(16) NOT NULL COMMENT '角色: user/assistant/system/tool',
  `content` text COMMENT '消息内容',
  `tool_calls` json DEFAULT NULL COMMENT '工具调用信息（JSON格式）',
  `tool_call_id` varchar(128) DEFAULT NULL COMMENT '工具调用结果关联ID',
  `token_count` int DEFAULT '0' COMMENT 'Token消耗',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客服消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `csms_message`
--

LOCK TABLES `csms_message` WRITE;
/*!40000 ALTER TABLE `csms_message` DISABLE KEYS */;
INSERT INTO `csms_message` VALUES (1,1,'user','你好',NULL,NULL,0,'2026-06-02 09:41:57','2026-06-02 09:41:57',0),(2,1,'assistant','您好！我是 ShopMax 电商平台的智能客服助手，很高兴为您服务。请问有什么可以帮助您的吗？',NULL,NULL,860,'2026-06-02 09:41:58','2026-06-02 09:41:58',0);
/*!40000 ALTER TABLE `csms_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `csms_session`
--

DROP TABLE IF EXISTS `csms_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `csms_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_no` varchar(32) NOT NULL COMMENT '会话编号（格式: CS-{yyyyMMdd}-{6位随机}）',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `status` tinyint DEFAULT '0' COMMENT '状态: 0进行中 1已结束',
  `last_message_time` datetime DEFAULT NULL COMMENT '最后消息时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_no` (`session_no`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客服会话表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `csms_session`
--

LOCK TABLES `csms_session` WRITE;
/*!40000 ALTER TABLE `csms_session` DISABLE KEYS */;
INSERT INTO `csms_session` VALUES (1,'CS-20260602-ZW31UB',1,0,'2026-06-02 09:41:58','2026-06-02 09:41:54','2026-06-02 09:41:54',0),(2,'CS-20260610-NJLYLS',19,0,'2026-06-10 17:18:30','2026-06-10 17:18:30','2026-06-10 17:18:30',0);
/*!40000 ALTER TABLE `csms_session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lms_anchor`
--

DROP TABLE IF EXISTS `lms_anchor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lms_anchor` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主播ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `real_name` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系电话',
  `nickname` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主播昵称',
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '主播头像',
  `cover` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '直播间封面',
  `introduction` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '主播简介',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-待审核 1-已通过 2-已拒绝 3-已禁用',
  `reject_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '拒绝原因',
  `level` tinyint NOT NULL DEFAULT '1' COMMENT '主播等级: 1-普通 2-铜牌 3-银牌 4-金牌 5-钻石',
  `fans_count` int NOT NULL DEFAULT '0' COMMENT '粉丝数',
  `total_live_count` int NOT NULL DEFAULT '0' COMMENT '累计直播场次',
  `total_duration` bigint NOT NULL DEFAULT '0' COMMENT '累计直播时长(秒)',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_level` (`level`),
  KEY `idx_fans_count` (`fans_count`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='主播信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lms_anchor`
--

LOCK TABLES `lms_anchor` WRITE;
/*!40000 ALTER TABLE `lms_anchor` DISABLE KEYS */;
INSERT INTO `lms_anchor` VALUES (1,2,'张三','13800138001','时尚美妆达人','https://img.alicdn.com/imgextra/i3/O1CN01uQJZkO1PkP0wFkVQR_!!6000000001877-2-tps-200-200.png',NULL,'专注时尚美妆5年，带你发现最美的自己',1,NULL,3,0,12,5377,NULL,'2026-05-01 10:44:05','2026-05-01 10:44:05',0),(2,3,'李四','13800138002','数码评测师','https://img.alicdn.com/imgextra/i3/O1CN01uQJZkO1PkP0wFkVQR_!!6000000001877-2-tps-200-200.png',NULL,'专业数码产品评测，客观公正，帮你选好物',1,NULL,2,0,0,0,NULL,'2026-05-01 10:44:05','2026-05-01 10:44:05',0);
/*!40000 ALTER TABLE `lms_anchor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lms_coin_log`
--

DROP TABLE IF EXISTS `lms_coin_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lms_coin_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `amount` int NOT NULL COMMENT '变动数量(正增负减)',
  `type` tinyint NOT NULL COMMENT '1注册赠送 2每日签到 3送礼消费 4系统赠送',
  `biz_id` varchar(64) DEFAULT NULL COMMENT '关联业务ID(消息ID)',
  `remark` varchar(128) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='虚拟币流水表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lms_coin_log`
--

LOCK TABLES `lms_coin_log` WRITE;
/*!40000 ALTER TABLE `lms_coin_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `lms_coin_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lms_gift`
--

DROP TABLE IF EXISTS `lms_gift`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lms_gift` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(32) NOT NULL COMMENT '礼物名称',
  `icon` varchar(255) NOT NULL COMMENT '图标URL',
  `animation_url` varchar(255) DEFAULT NULL COMMENT 'Lottie动画URL',
  `price` int NOT NULL COMMENT '虚拟币价格',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='礼物配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lms_gift`
--

LOCK TABLES `lms_gift` WRITE;
/*!40000 ALTER TABLE `lms_gift` DISABLE KEYS */;
INSERT INTO `lms_gift` VALUES (1,'玫瑰','/gifts/rose.png','/gifts/rose.json',10,1,'2026-06-05 11:04:01','2026-06-05 11:04:01',0),(2,'棒棒糖','/gifts/lollipop.png','/gifts/lollipop.json',20,2,'2026-06-05 11:04:01','2026-06-05 11:04:01',0),(3,'奶茶','/gifts/milktea.png','/gifts/milktea.json',50,3,'2026-06-05 11:04:01','2026-06-05 11:04:01',0),(4,'吉他','/gifts/guitar.png','/gifts/guitar.json',100,4,'2026-06-05 11:04:01','2026-06-05 11:04:01',0),(5,'火箭','/gifts/rocket.png','/gifts/rocket.json',200,5,'2026-06-05 11:04:01','2026-06-05 11:04:01',0),(6,'钻石','/gifts/diamond.png','/gifts/diamond.json',500,6,'2026-06-05 11:04:01','2026-06-05 11:04:01',0);
/*!40000 ALTER TABLE `lms_gift` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lms_live_product`
--

DROP TABLE IF EXISTS `lms_live_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lms_live_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `room_id` bigint NOT NULL COMMENT '直播间ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `live_price` decimal(10,2) NOT NULL COMMENT '直播价',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-已下架 1-已上架 2-讲解中',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='直播商品关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lms_live_product`
--

LOCK TABLES `lms_live_product` WRITE;
/*!40000 ALTER TABLE `lms_live_product` DISABLE KEYS */;
INSERT INTO `lms_live_product` VALUES (1,1,1,1,999.00,0,0,'2026-05-01 13:33:38','2026-05-01 13:33:38',0),(2,1,1,1,6999.00,0,1,'2026-05-01 13:34:21','2026-05-01 13:34:21',0);
/*!40000 ALTER TABLE `lms_live_product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lms_live_room`
--

DROP TABLE IF EXISTS `lms_live_room`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lms_live_room` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '直播间ID',
  `anchor_id` bigint NOT NULL COMMENT '主播ID',
  `title` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '直播标题',
  `cover` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '直播封面',
  `notice` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '直播公告',
  `type` tinyint NOT NULL DEFAULT '1' COMMENT '直播分类: 1-推荐 2-穿搭 3-美妆 4-美食 5-家居 6-数码 7-母婴',
  `start_time` datetime NOT NULL COMMENT '预告开始时间',
  `actual_start_time` datetime DEFAULT NULL COMMENT '实际开播时间',
  `end_time` datetime DEFAULT NULL COMMENT '直播结束时间',
  `push_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '推流地址',
  `pull_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '拉流地址(播放地址)',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-预告 1-直播中 2-已结束 3-已关闭',
  `online_count` int NOT NULL DEFAULT '0' COMMENT '当前在线人数',
  `total_view_count` int NOT NULL DEFAULT '0' COMMENT '累计观看人次',
  `peak_online_count` int NOT NULL DEFAULT '0' COMMENT '峰值在线人数',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  `duration` bigint NOT NULL DEFAULT '0' COMMENT '直播时长(秒)',
  `replay_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '回放地址',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
  `gift_count` bigint NOT NULL DEFAULT '0' COMMENT '礼物总数',
  `replay_duration` int DEFAULT NULL COMMENT '回放时长(秒)',
  PRIMARY KEY (`id`),
  KEY `idx_anchor_id` (`anchor_id`),
  KEY `idx_status` (`status`),
  KEY `idx_type` (`type`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_online_count` (`online_count`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='直播间表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lms_live_room`
--

LOCK TABLES `lms_live_room` WRITE;
/*!40000 ALTER TABLE `lms_live_room` DISABLE KEYS */;
INSERT INTO `lms_live_room` VALUES (1,1,'夏季美妆新品首发，限时特惠','https://img.alicdn.com/imgextra/i1/O1CN01hQwUXq1EWXRBbnvM7_!!6000000000362-0-tps-750-422.jpg','今晚8点准时开播，超多福利等着你！',3,'2026-05-01 20:00:00','2026-05-01 13:34:52','2026-05-01 13:34:55',NULL,NULL,2,0,0,0,0,2,NULL,'2026-05-01 10:44:05','2026-05-01 10:44:05',0,0,NULL),(2,2,'旗舰手机深度对比评测','https://img.alicdn.com/imgextra/i1/O1CN01hQwUXq1EWXRBbnvM7_!!6000000000362-0-tps-750-422.jpg','现场对比各品牌旗舰机型，帮你选对不买贵',6,'2026-05-02 15:00:00',NULL,NULL,NULL,NULL,0,0,0,0,0,0,NULL,'2026-05-01 10:44:05','2026-05-01 10:44:05',0,0,NULL),(3,1,'测试直播',NULL,NULL,1,'2026-06-05 12:00:00','2026-06-05 12:08:03','2026-06-05 12:10:26','rtmp://localhost:1935/live/3?token=eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImlhdCI6MTc4MDYzMTM3OCwiZXhwIjoxNzgwNzE3Nzc4fQ.KZDJSRG5te-N8h74itoMb4fa_DYXtvdjWbQ_gGsns28','http://localhost:8085/live/3.flv',2,0,0,0,0,142,NULL,'2026-06-05 11:46:42','2026-06-05 12:14:46',1,0,NULL),(4,1,'测试直播','','',1,'2026-06-05 12:35:05','2026-06-05 12:35:57','2026-06-05 12:40:47','rtmp://localhost:1935/live/4?token=eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImlhdCI6MTc4MDYzNDEyMSwiZXhwIjoxNzgwNzIwNTIxfQ.wZUE19R1U9NZa6MGLIv1RnL-bA2DlcSGuK1iH9umiLY','http://localhost:8085/live/4.flv',2,0,0,0,0,289,NULL,'2026-06-05 12:35:12','2026-06-05 12:40:49',1,0,NULL),(5,1,'测试直播','','',1,'2026-06-05 12:40:57','2026-06-05 12:41:20','2026-06-05 12:48:44','rtmp://localhost:1935/live/5?token=eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImlhdCI6MTc4MDYzNDQ2MiwiZXhwIjoxNzgwNzIwODYyfQ.qg606qjHSj5jfsRZPTPTUqYbIMTQpk_GbeVDmgQkQ5o','http://localhost:8085/live/5.flv?token=eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImlhdCI6MTc4MDYzNDQ2MiwiZXhwIjoxNzgwNzIwODYyfQ.qg606qjHSj5jfsRZPTPTUqYbIMTQpk_GbeVDmgQkQ5o',2,0,0,0,0,444,NULL,'2026-06-05 12:40:59','2026-06-05 12:49:54',1,0,NULL),(6,1,'测试直播','','',1,'2026-06-05 12:50:06','2026-06-05 12:51:19','2026-06-05 13:42:49','rtmp://localhost:1935/live/6?token=eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImlhdCI6MTc4MDYzNTAxMiwiZXhwIjoxNzgwNzIxNDEyfQ.qV7f3zhT9Mf7-r-6NOxTLjFPInYJ0pj4-v1XawLxSWQ','http://localhost:8085/live/6.flv?token=eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImlhdCI6MTc4MDYzNTAxMiwiZXhwIjoxNzgwNzIxNDEyfQ.qV7f3zhT9Mf7-r-6NOxTLjFPInYJ0pj4-v1XawLxSWQ',2,0,0,0,0,3090,NULL,'2026-06-05 12:50:09','2026-06-05 13:42:52',1,0,NULL),(7,1,'测试','','',1,'2026-06-05 13:43:02','2026-06-05 13:43:33','2026-06-05 13:49:25','rtmp://localhost:1935/live/7?token=eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImlhdCI6MTc4MDYzODE4NywiZXhwIjoxNzgwNzI0NTg3fQ.fKwwGcVT6OwzJMUGSJwJ1GkzwvOlEDmUtnkaeHPujLU','http://localhost:8085/live/7.flv?token=eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImlhdCI6MTc4MDYzODE4NywiZXhwIjoxNzgwNzI0NTg3fQ.fKwwGcVT6OwzJMUGSJwJ1GkzwvOlEDmUtnkaeHPujLU',2,0,0,0,0,351,NULL,'2026-06-05 13:43:04','2026-06-05 13:50:13',1,0,NULL),(8,1,'测试','','',1,'2026-06-05 13:50:20','2026-06-05 13:50:56','2026-06-05 13:57:58','rtmp://localhost:1935/live/8?token=eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImlhdCI6MTc4MDYzODYyNCwiZXhwIjoxNzgwNzI1MDI0fQ.LkAXMsTMN9IVTSki2Qf33DwNICJFS89U9qyfnnJD3PQ','http://localhost:8085/live/8.flv?token=eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImlhdCI6MTc4MDYzODYyNCwiZXhwIjoxNzgwNzI1MDI0fQ.LkAXMsTMN9IVTSki2Qf33DwNICJFS89U9qyfnnJD3PQ',2,0,0,0,0,421,NULL,'2026-06-05 13:50:22','2026-06-05 14:01:28',1,0,NULL),(9,1,'测试','','',1,'2026-06-05 14:01:38','2026-06-05 14:02:14','2026-06-05 14:35:30','rtmp://localhost:1935/live/9?token=eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImlhdCI6MTc4MDYzOTMwMSwiZXhwIjoxNzgwNzI1NzAxfQ.x1CRvE43x9q5elEOgxm_k4zHYXjJxvE7lVUFxzev16g','http://localhost:8085/live/9.flv?token=eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImlhdCI6MTc4MDYzOTMwMSwiZXhwIjoxNzgwNzI1NzAxfQ.x1CRvE43x9q5elEOgxm_k4zHYXjJxvE7lVUFxzev16g',2,15,15,0,0,1996,NULL,'2026-06-05 14:01:39','2026-06-05 14:35:35',1,0,NULL),(10,1,'测试直播','','',1,'2026-06-05 14:36:06','2026-06-05 14:36:38','2026-06-05 14:47:22','rtmp://localhost:1935/live/10?token=eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImlhdCI6MTc4MDY0MTM3MCwiZXhwIjoxNzgwNzI3NzcwfQ.ddv9aaDf6pkd4MHvJgZMgTt7XmoRRIIqUNNLo7bF25E','http://localhost:8085/live/10.flv?token=eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImlhdCI6MTc4MDY0MTM3MCwiZXhwIjoxNzgwNzI3NzcwfQ.ddv9aaDf6pkd4MHvJgZMgTt7XmoRRIIqUNNLo7bF25E',2,2,2,0,0,644,NULL,'2026-06-05 14:36:08','2026-06-05 14:49:15',1,0,NULL),(11,1,'测试直播','','',1,'2026-06-05 14:49:26','2026-06-05 14:49:52','2026-06-05 14:52:45','rtmp://localhost:1935/live/11?token=eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImlhdCI6MTc4MDY0MjE3MSwiZXhwIjoxNzgwNzI4NTcxfQ.OjrmCZX3z8dEeuCiEVq-pkQ5zYyRsxdS-QVUno-7ut8','http://localhost:8085/live/11.flv?token=eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImlhdCI6MTc4MDY0MjE3MSwiZXhwIjoxNzgwNzI4NTcxfQ.OjrmCZX3z8dEeuCiEVq-pkQ5zYyRsxdS-QVUno-7ut8',2,3,3,0,0,173,NULL,'2026-06-05 14:49:29','2026-06-05 15:05:14',0,0,NULL),(12,1,'测试','','',1,'2026-06-09 09:32:22','2026-06-09 09:33:34','2026-06-09 09:38:39','rtmp://localhost:1935/live/12?token=eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImlhdCI6MTc4MDk2ODc3OSwiZXhwIjoxNzgxMDU1MTc5fQ.PGtdnBZ8lwglF6VowlMxv_TUDDQkN1qnrXF1y3hXoMU','http://localhost:8085/live/12.flv?token=eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImlhdCI6MTc4MDk2ODc3OSwiZXhwIjoxNzgxMDU1MTc5fQ.PGtdnBZ8lwglF6VowlMxv_TUDDQkN1qnrXF1y3hXoMU',2,1,1,0,0,304,NULL,'2026-06-09 09:32:27','2026-06-09 09:34:02',0,0,NULL),(13,1,'测试','','',1,'2026-06-21 02:03:13','2026-06-21 02:03:50','2026-06-21 02:04:40','rtmp://localhost:1935/live/13?token=eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImlhdCI6MTc4MTk3ODYwMywiZXhwIjoxNzgyMDY1MDAzfQ.ZH_AxKsQZx8Jsm8aue7ZN_8w_UoYGvNqNuhRsCj5HSo','http://localhost:8085/live/13.flv?token=eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImlhdCI6MTc4MTk3ODYwMywiZXhwIjoxNzgyMDY1MDAzfQ.ZH_AxKsQZx8Jsm8aue7ZN_8w_UoYGvNqNuhRsCj5HSo',2,1,1,0,0,49,NULL,'2026-06-21 02:03:19','2026-06-21 02:04:06',0,0,NULL);
/*!40000 ALTER TABLE `lms_live_room` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mms_coupon`
--

DROP TABLE IF EXISTS `mms_coupon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mms_coupon` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '优惠券名称',
  `type` tinyint NOT NULL DEFAULT '1' COMMENT '优惠券类型: 1-满减券 2-折扣券 3-运费券 4-新人券',
  `min_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '使用门槛金额(0表示无门槛)',
  `discount_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '减免金额(满减券使用)',
  `discount_rate` decimal(3,2) NOT NULL DEFAULT '1.00' COMMENT '折扣率(折扣券使用, 如0.85表示85折)',
  `total_count` int NOT NULL DEFAULT '0' COMMENT '发放总量',
  `received_count` int NOT NULL DEFAULT '0' COMMENT '已领取数量',
  `used_count` int NOT NULL DEFAULT '0' COMMENT '已使用数量',
  `per_limit` int NOT NULL DEFAULT '1' COMMENT '每人限领数量',
  `valid_days` int NOT NULL DEFAULT '7' COMMENT '领取后有效天数',
  `use_start_time` datetime DEFAULT NULL COMMENT '固定有效期-开始时间',
  `use_end_time` datetime DEFAULT NULL COMMENT '固定有效期-结束时间',
  `applicable_type` tinyint NOT NULL DEFAULT '1' COMMENT '适用类型: 1-全部商品 2-指定分类 3-指定商品',
  `applicable_ids` varchar(2000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '适用分类/商品ID列表(JSON数组)',
  `integral_cost` int DEFAULT '0',
  `stackable` tinyint DEFAULT '0',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '使用说明',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券模板表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mms_coupon`
--

LOCK TABLES `mms_coupon` WRITE;
/*!40000 ALTER TABLE `mms_coupon` DISABLE KEYS */;
INSERT INTO `mms_coupon` VALUES (1,'新人满100减20',1,100.00,20.00,1.00,1000,2,0,1,30,NULL,NULL,1,NULL,0,0,'新用户专享，满100元减20元，全场通用',1,'2026-05-01 10:44:01','2026-06-10 09:56:14',0),(2,'全场9折券',2,0.00,0.00,0.90,500,2,0,1,15,NULL,NULL,1,NULL,0,0,'全场商品9折优惠，每人限领1张',1,'2026-05-01 10:44:01','2026-06-10 09:56:09',0),(3,'满50减10运费券',3,50.00,10.00,1.00,200,2,0,2,30,NULL,NULL,1,NULL,0,0,'满50元可抵10元运费',1,'2026-05-01 10:44:01','2026-06-10 09:56:04',0),(4,'夏季大促满200减50',1,200.00,50.00,1.00,300,2,0,1,7,NULL,NULL,1,NULL,0,0,'夏季大促专属优惠，满200减50',1,'2026-05-01 10:44:01','2026-06-10 09:55:28',0),(5,'新人满100减20',1,100.00,20.00,1.00,1000,2,0,1,30,NULL,NULL,1,NULL,0,0,'新用户专享，满100元减20元，全场通用',1,'2026-05-01 11:39:02','2026-06-10 09:55:58',0),(6,'全场9折券',2,0.00,0.00,0.90,500,2,0,1,15,NULL,NULL,1,NULL,0,0,'全场商品9折优惠，每人限领1张',1,'2026-05-01 11:39:02','2026-06-10 09:55:50',0),(7,'满50减10运费券',3,50.00,10.00,1.00,200,2,0,2,30,NULL,NULL,1,NULL,0,0,'满50元可抵10元运费',1,'2026-05-01 11:39:02','2026-06-10 09:55:45',0),(8,'夏季大促满200减50',1,205.00,50.00,1.00,300,2,0,1,7,NULL,NULL,1,'[]',0,0,'夏季大促专属优惠，满200减50',1,'2026-05-01 11:39:02','2026-06-03 16:14:37',0);
/*!40000 ALTER TABLE `mms_coupon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mms_coupon_receive`
--

DROP TABLE IF EXISTS `mms_coupon_receive`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mms_coupon_receive` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '领取记录ID',
  `coupon_id` bigint NOT NULL COMMENT '优惠券ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `receive_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `use_time` datetime DEFAULT NULL COMMENT '使用时间',
  `order_id` bigint DEFAULT NULL COMMENT '关联订单ID',
  `order_no` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联订单号',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-未使用 1-已使用 2-已过期',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_coupon_id` (`coupon_id`),
  KEY `idx_status` (`status`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券领取记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mms_coupon_receive`
--

LOCK TABLES `mms_coupon_receive` WRITE;
/*!40000 ALTER TABLE `mms_coupon_receive` DISABLE KEYS */;
INSERT INTO `mms_coupon_receive` VALUES (1,5,1,'2026-05-30 10:43:14','2026-05-30 15:58:46',NULL,NULL,1,'2026-05-30 10:43:14','2026-05-30 15:58:46',0),(2,6,1,'2026-05-30 10:43:19','2026-05-30 11:36:45',NULL,NULL,1,'2026-05-30 10:43:19','2026-05-30 11:36:45',0),(3,7,1,'2026-05-30 10:43:20',NULL,NULL,NULL,0,'2026-05-30 10:43:20','2026-05-30 10:43:20',0),(4,8,1,'2026-05-30 10:43:21','2026-05-30 13:43:04',NULL,NULL,1,'2026-05-30 10:43:21','2026-05-30 13:43:04',0),(5,1,1,'2026-05-30 10:43:21','2026-05-30 13:54:25',NULL,NULL,1,'2026-05-30 10:43:21','2026-05-30 13:54:25',0),(6,2,1,'2026-05-30 10:43:22','2026-05-30 11:09:57',NULL,NULL,1,'2026-05-30 10:43:22','2026-05-30 11:09:57',0),(7,3,1,'2026-05-30 10:43:23',NULL,NULL,NULL,0,'2026-05-30 10:43:23','2026-05-30 10:43:23',0),(8,4,1,'2026-05-30 10:43:24','2026-05-30 11:41:41',NULL,NULL,1,'2026-05-30 10:43:24','2026-05-30 11:41:41',0),(16,8,1,'2026-06-03 16:14:37','2026-06-03 16:54:49',NULL,NULL,1,'2026-06-03 16:14:37','2026-06-03 16:54:49',0),(17,4,1,'2026-06-10 09:55:29',NULL,NULL,NULL,0,'2026-06-10 09:55:29','2026-06-10 09:55:29',0),(18,7,1,'2026-06-10 09:55:46',NULL,NULL,NULL,0,'2026-06-10 09:55:46','2026-06-10 09:55:46',0),(19,6,1,'2026-06-10 09:55:50',NULL,NULL,NULL,0,'2026-06-10 09:55:50','2026-06-10 09:55:50',0),(20,5,1,'2026-06-10 09:55:58',NULL,NULL,NULL,0,'2026-06-10 09:55:58','2026-06-10 09:55:58',0),(21,3,1,'2026-06-10 09:56:04',NULL,NULL,NULL,0,'2026-06-10 09:56:04','2026-06-10 09:56:04',0),(22,2,1,'2026-06-10 09:56:09',NULL,NULL,NULL,0,'2026-06-10 09:56:09','2026-06-10 09:56:09',0),(23,1,1,'2026-06-10 09:56:14',NULL,NULL,NULL,0,'2026-06-10 09:56:14','2026-06-10 09:56:14',0);
/*!40000 ALTER TABLE `mms_coupon_receive` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mms_group_buy_activity`
--

DROP TABLE IF EXISTS `mms_group_buy_activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mms_group_buy_activity` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '拼团活动ID',
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动名称',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `group_price` decimal(10,2) NOT NULL COMMENT '拼团价格',
  `required_count` int NOT NULL DEFAULT '2' COMMENT '成团人数',
  `expire_hours` int NOT NULL DEFAULT '24' COMMENT '拼团有效小时数',
  `stock` int NOT NULL DEFAULT '0' COMMENT '拼团库存',
  `start_time` datetime NOT NULL COMMENT '活动开始时间',
  `end_time` datetime NOT NULL COMMENT '活动结束时间',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_status` (`status`),
  KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拼团活动表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mms_group_buy_activity`
--

LOCK TABLES `mms_group_buy_activity` WRITE;
/*!40000 ALTER TABLE `mms_group_buy_activity` DISABLE KEYS */;
INSERT INTO `mms_group_buy_activity` VALUES (1,'夏日T恤2人拼团',1,1,49.90,2,24,100,'2026-05-01 00:00:00','2026-05-31 23:59:59',1,'2026-05-01 11:39:02','2026-05-01 11:39:02',0),(2,'品牌耳机3人拼团',2,2,159.00,3,12,50,'2026-05-01 00:00:00','2026-05-31 23:59:59',1,'2026-05-01 11:39:02','2026-05-01 11:39:02',0);
/*!40000 ALTER TABLE `mms_group_buy_activity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mms_group_buy_group`
--

DROP TABLE IF EXISTS `mms_group_buy_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mms_group_buy_group` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '拼团ID',
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `leader_id` bigint NOT NULL COMMENT '团长用户ID',
  `current_count` int NOT NULL DEFAULT '1' COMMENT '当前参团人数',
  `required_count` int NOT NULL COMMENT '成团人数',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-进行中 1-已成团 2-已失败',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `complete_time` datetime DEFAULT NULL COMMENT '成团/失败时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_leader_id` (`leader_id`),
  KEY `idx_status` (`status`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拼团记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mms_group_buy_group`
--

LOCK TABLES `mms_group_buy_group` WRITE;
/*!40000 ALTER TABLE `mms_group_buy_group` DISABLE KEYS */;
/*!40000 ALTER TABLE `mms_group_buy_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mms_group_buy_member`
--

DROP TABLE IF EXISTS `mms_group_buy_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mms_group_buy_member` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '成员ID',
  `group_id` bigint NOT NULL COMMENT '拼团ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `order_id` bigint DEFAULT NULL COMMENT '订单ID',
  `order_no` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '订单号',
  `is_leader` tinyint NOT NULL DEFAULT '0' COMMENT '是否团长: 0-否 1-是',
  `join_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '参团时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_user` (`group_id`,`user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拼团成员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mms_group_buy_member`
--

LOCK TABLES `mms_group_buy_member` WRITE;
/*!40000 ALTER TABLE `mms_group_buy_member` DISABLE KEYS */;
/*!40000 ALTER TABLE `mms_group_buy_member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mms_promotion`
--

DROP TABLE IF EXISTS `mms_promotion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mms_promotion` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '促销活动ID',
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动名称',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '活动描述',
  `type` tinyint NOT NULL DEFAULT '1' COMMENT '促销类型: 1-满减 2-满折',
  `min_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '满减门槛金额',
  `discount_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '减免金额(满减)',
  `discount_rate` decimal(3,2) NOT NULL DEFAULT '1.00' COMMENT '折扣率(满折, 如0.9表示9折)',
  `start_time` datetime NOT NULL COMMENT '活动开始时间',
  `end_time` datetime NOT NULL COMMENT '活动结束时间',
  `applicable_type` tinyint NOT NULL DEFAULT '1' COMMENT '适用类型: 1-全部商品 2-指定分类 3-指定商品',
  `applicable_ids` varchar(2000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '适用分类/商品ID列表(JSON数组)',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-未开始 1-进行中 2-已结束 3-已禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_end_time` (`end_time`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='促销活动表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mms_promotion`
--

LOCK TABLES `mms_promotion` WRITE;
/*!40000 ALTER TABLE `mms_promotion` DISABLE KEYS */;
INSERT INTO `mms_promotion` VALUES (1,'五一满减活动','五一劳动节全场满300减80',1,300.00,80.00,1.00,'2026-04-28 00:00:00','2026-05-07 23:59:59',1,NULL,1,'2026-05-01 10:44:01','2026-05-01 10:44:01',0),(2,'开学季折扣','开学季数码产品满500享8折',2,500.00,0.00,0.80,'2026-05-01 00:00:00','2026-05-31 23:59:59',1,NULL,1,'2026-05-01 10:44:01','2026-05-01 10:44:01',0),(3,'五一满减活动','五一劳动节全场满300减80',1,300.00,80.00,1.00,'2026-04-28 00:00:00','2026-05-07 23:59:59',1,NULL,1,'2026-05-01 11:39:02','2026-05-01 11:39:02',0),(4,'开学季折扣','开学季数码产品满500享8折',2,500.00,0.00,0.80,'2026-05-01 00:00:00','2026-05-31 23:59:59',1,NULL,1,'2026-05-01 11:39:02','2026-05-01 11:39:02',0);
/*!40000 ALTER TABLE `mms_promotion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mms_promotion_scope`
--

DROP TABLE IF EXISTS `mms_promotion_scope`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mms_promotion_scope` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '适用范围ID',
  `promotion_id` bigint NOT NULL COMMENT '促销活动ID',
  `target_type` tinyint NOT NULL DEFAULT '1' COMMENT '目标类型: 1-分类 2-商品',
  `target_id` bigint NOT NULL COMMENT '目标ID(分类ID或商品ID)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_promotion_id` (`promotion_id`),
  KEY `idx_target` (`target_type`,`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='促销适用范围表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mms_promotion_scope`
--

LOCK TABLES `mms_promotion_scope` WRITE;
/*!40000 ALTER TABLE `mms_promotion_scope` DISABLE KEYS */;
/*!40000 ALTER TABLE `mms_promotion_scope` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mms_seckill_message`
--

DROP TABLE IF EXISTS `mms_seckill_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mms_seckill_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `message_type` varchar(32) NOT NULL COMMENT '消息类型：SECKILL_ORDER',
  `business_id` varchar(64) NOT NULL COMMENT '业务ID：秒杀订单号',
  `content` text NOT NULL COMMENT '消息内容(JSON)',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0-待处理 1-已处理 2-处理失败 3-已死信',
  `retry_count` int NOT NULL DEFAULT '0' COMMENT '重试次数',
  `max_retry` int NOT NULL DEFAULT '3' COMMENT '最大重试次数',
  `next_retry_time` datetime DEFAULT NULL COMMENT '下次重试时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_status_next_retry` (`status`,`next_retry_time`),
  KEY `idx_business_id` (`business_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='秒杀本地消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mms_seckill_message`
--

LOCK TABLES `mms_seckill_message` WRITE;
/*!40000 ALTER TABLE `mms_seckill_message` DISABLE KEYS */;
/*!40000 ALTER TABLE `mms_seckill_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mms_seckill_product`
--

DROP TABLE IF EXISTS `mms_seckill_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mms_seckill_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '秒杀商品ID',
  `session_id` bigint NOT NULL COMMENT '场次ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `seckill_price` decimal(10,2) NOT NULL COMMENT '秒杀价',
  `seckill_stock` int NOT NULL DEFAULT '0' COMMENT '秒杀库存',
  `limit_per_user` int NOT NULL DEFAULT '1' COMMENT '每人限购数量',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mms_seckill_product`
--

LOCK TABLES `mms_seckill_product` WRITE;
/*!40000 ALTER TABLE `mms_seckill_product` DISABLE KEYS */;
INSERT INTO `mms_seckill_product` VALUES (1,1,1,1,99.00,100,1,1,1,'2026-06-15 15:58:38','2026-06-15 15:58:38',0);
/*!40000 ALTER TABLE `mms_seckill_product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mms_seckill_session`
--

DROP TABLE IF EXISTS `mms_seckill_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mms_seckill_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '场次ID',
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '场次名称(如: 10点场)',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-未开始 1-进行中 2-已结束',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀场次表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mms_seckill_session`
--

LOCK TABLES `mms_seckill_session` WRITE;
/*!40000 ALTER TABLE `mms_seckill_session` DISABLE KEYS */;
INSERT INTO `mms_seckill_session` VALUES (1,'10点场','2026-05-01 10:00:00','2026-05-01 12:00:00',1,'2026-05-01 11:39:02','2026-05-01 11:39:02',0),(2,'14点场','2026-05-01 14:00:00','2026-05-01 16:00:00',1,'2026-05-01 11:39:02','2026-05-01 11:39:02',0),(3,'20点场','2026-05-01 20:00:00','2026-05-01 22:00:00',0,'2026-05-01 11:39:02','2026-05-01 11:39:02',0),(4,'10:00 场','2026-06-15 00:00:00','2026-06-15 23:59:59',1,'2026-06-15 15:58:38','2026-06-15 15:58:38',0);
/*!40000 ALTER TABLE `mms_seckill_session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oms_cart_item`
--

DROP TABLE IF EXISTS `oms_cart_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oms_cart_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `sku_id` bigint DEFAULT NULL COMMENT 'SKU ID',
  `product_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称',
  `product_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品图片',
  `sku_specs` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'SKU规格',
  `price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '单价',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '数量',
  `selected` tinyint NOT NULL DEFAULT '1' COMMENT '是否选中: 0-否 1-是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oms_cart_item`
--

LOCK TABLES `oms_cart_item` WRITE;
/*!40000 ALTER TABLE `oms_cart_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `oms_cart_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oms_logistics`
--

DROP TABLE IF EXISTS `oms_logistics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oms_logistics` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `logistics_no` varchar(64) NOT NULL COMMENT '物流单号',
  `company` varchar(32) NOT NULL COMMENT '物流公司',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-已发货 1-运输中 2-派送中 3-已签收',
  `sender_name` varchar(32) DEFAULT NULL COMMENT '发件人姓名',
  `sender_phone` varchar(20) DEFAULT NULL COMMENT '发件人电话',
  `sender_address` varchar(255) DEFAULT NULL COMMENT '发件人地址',
  `sender_latitude` decimal(10,6) DEFAULT NULL COMMENT '发件人纬度',
  `sender_longitude` decimal(10,6) DEFAULT NULL COMMENT '发件人经度',
  `receiver_name` varchar(32) DEFAULT NULL COMMENT '收件人姓名',
  `receiver_phone` varchar(20) DEFAULT NULL COMMENT '收件人电话',
  `receiver_address` varchar(255) DEFAULT NULL COMMENT '收件人地址',
  `receiver_latitude` decimal(10,6) DEFAULT NULL COMMENT '收件人纬度',
  `receiver_longitude` decimal(10,6) DEFAULT NULL COMMENT '收件人经度',
  `last_query_time` datetime DEFAULT NULL COMMENT '上次查询API时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_logistics_no` (`logistics_no`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物流信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oms_logistics`
--

LOCK TABLES `oms_logistics` WRITE;
/*!40000 ALTER TABLE `oms_logistics` DISABLE KEYS */;
INSERT INTO `oms_logistics` VALUES (2,29,'SF1234567890','shunfeng',0,'黄先生','13685649654','肇庆市鼎湖区莲花镇广州应用科技学院肇庆校区学术交流中心',23.267625,112.671975,'kaitou','13794057765','佛山市南海区狮山镇小塘长安路7号',23.092790,112.976203,NULL,'2026-06-07 13:17:24','2026-06-08 17:26:57',0),(3,30,'SF1593574568','shunfeng',0,'黄先生','13645657898','肇庆市鼎湖区莲花镇广州应用科技学院肇庆校区学术交流中心',23.267625,112.671975,'kaitou','13794057765','佛山市南海区狮山镇小塘长安路7号',23.092790,112.976203,NULL,'2026-06-07 14:17:49','2026-06-08 17:26:50',0),(5,24,'SF1593574568','shunfeng',0,'黄先生','13686548984','肇庆市鼎湖区莲花镇广州应用科技学院肇庆校区学术交流中心',23.267625,112.671975,'kaitou','13794057765','佛山市南海区狮山镇小塘长安路7号',23.092790,112.976203,NULL,'2026-06-08 14:42:39','2026-06-08 17:24:22',0),(6,31,'SF3216549870','shunfeng',0,'黄先生','13698745632','肇庆市鼎湖区莲花镇广州应用科技学院肇庆校区学术交流中心',23.267625,112.671975,'kaitou','13794057765','佛山市南海区狮山镇小塘长安\r\n  路7号',23.092790,112.976203,NULL,'2026-06-08 14:46:08','2026-06-08 17:26:48',0),(7,32,'SF6543219870','shunfeng',0,'黄先生','13802265948','肇庆市鼎湖区莲花镇广州应用科技学院肇庆校区学术交流中心',23.269548,112.682962,'kaitou','13794057765','广东省佛山市南海区狮山镇小塘长安路7号',23.099202,113.034903,NULL,'2026-06-08 17:11:47','2026-06-08 17:11:47',0),(8,33,'SF1246537987','shunfeng',0,'黄先生','13800138000','肇庆市鼎湖区莲花镇广州应用科技学院肇庆校区学术交流中心',23.267625,112.671975,'kaitou','13794057765','广东省佛山市南海区狮山镇小塘长安路7号',23.092790,112.976203,NULL,'2026-06-08 17:40:34','2026-06-08 20:44:34',0),(9,37,'ZT1234567890','zhongtong',0,'吴女士','13800138000','广州应用科技学院肇庆校区二期菜鸟驿站',23.269876,112.678191,'kaitou','13794057765','广东省佛山市南海区狮山镇小塘长安路7号',23.099202,113.034903,NULL,'2026-06-21 01:19:22','2026-06-21 01:19:22',0);
/*!40000 ALTER TABLE `oms_logistics` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oms_logistics_company`
--

DROP TABLE IF EXISTS `oms_logistics_company`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oms_logistics_company` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL COMMENT '公司名称',
  `code` varchar(32) NOT NULL COMMENT '公司编码',
  `website` varchar(128) DEFAULT NULL COMMENT '官网',
  `phone` varchar(20) DEFAULT NULL COMMENT '客服电话',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物流公司表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oms_logistics_company`
--

LOCK TABLES `oms_logistics_company` WRITE;
/*!40000 ALTER TABLE `oms_logistics_company` DISABLE KEYS */;
INSERT INTO `oms_logistics_company` VALUES (1,'顺丰速运','shunfeng','https://www.sf-express.com','95338',1,'2026-06-07 11:08:14','2026-06-07 11:08:14'),(2,'韵达快递','yunda','https://www.yundaex.com','95546',1,'2026-06-07 11:08:14','2026-06-07 11:08:14'),(3,'圆通速递','yuantong','https://www.yto.net.cn','95554',1,'2026-06-07 11:08:14','2026-06-07 11:08:14'),(4,'中通快递','zhongtong','https://www.zto.com','95311',1,'2026-06-07 11:08:14','2026-06-07 11:08:14'),(5,'申通快递','shentong','https://www.sto.cn','95543',1,'2026-06-07 11:08:14','2026-06-07 11:08:14'),(6,'京东物流','jd','https://www.jdl.com','950616',1,'2026-06-07 11:08:14','2026-06-07 11:08:14'),(7,'EMS','ems','https://www.ems.com.cn','11183',1,'2026-06-07 11:08:14','2026-06-07 11:08:14'),(8,'德邦快递','debang','https://www.deppon.com','95353',1,'2026-06-07 11:08:14','2026-06-07 11:08:14'),(9,'极兔速递','jtexpress','https://www.jtexpress.com','956036',1,'2026-06-07 11:08:14','2026-06-07 11:08:14'),(10,'百世快递','huitongkuaidi','https://www.800bestex.com','95320',1,'2026-06-07 11:08:14','2026-06-07 11:08:14');
/*!40000 ALTER TABLE `oms_logistics_company` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oms_logistics_trace`
--

DROP TABLE IF EXISTS `oms_logistics_trace`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oms_logistics_trace` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `logistics_id` bigint NOT NULL COMMENT '物流ID',
  `trace_time` datetime NOT NULL COMMENT '轨迹时间',
  `content` varchar(500) NOT NULL COMMENT '轨迹内容',
  `location` varchar(128) DEFAULT NULL COMMENT '当前位置',
  `location_code` varchar(32) DEFAULT NULL COMMENT '地点编码',
  `latitude` decimal(10,6) DEFAULT NULL COMMENT '纬度',
  `longitude` decimal(10,6) DEFAULT NULL COMMENT '经度',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_logistics_id` (`logistics_id`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物流轨迹表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oms_logistics_trace`
--

LOCK TABLES `oms_logistics_trace` WRITE;
/*!40000 ALTER TABLE `oms_logistics_trace` DISABLE KEYS */;
INSERT INTO `oms_logistics_trace` VALUES (12,2,'2026-06-07 13:17:24','快件已发货','广东省深圳市南山区科技园',NULL,22.544610,113.946040,'2026-06-07 13:17:24'),(22,3,'2026-06-07 14:17:49','快件已发货','广东省肇庆市鼎湖区莲花镇丰乐路20号广州应用科技学院肇庆校区二期菜鸟驿站',NULL,NULL,NULL,'2026-06-07 14:17:50'),(24,5,'2026-06-08 14:42:39','快件已发货','广东省肇庆市鼎湖区莲花镇丰乐路20号广州应用科技学院肇庆校区二期菜鸟驿站',NULL,23.269548,112.682962,'2026-06-08 14:42:39'),(25,6,'2026-06-08 14:46:08','快件已发货','广东省肇庆市鼎湖区莲花镇丰乐路20号广州应用科技学院肇庆校区二期菜鸟驿站',NULL,23.269548,112.682962,'2026-06-08 14:46:08'),(26,7,'2026-06-08 17:11:47','快件已发货','肇庆市鼎湖区莲花镇广州应用科技学院肇庆校区学术交流中心',NULL,23.269548,112.682962,'2026-06-08 17:11:47'),(27,8,'2026-06-08 17:40:34','快件已发货','肇庆市鼎湖区莲花镇广州应用科技学院肇庆校区学术交流中心',NULL,23.269548,112.682962,'2026-06-08 17:40:34'),(28,9,'2026-06-21 01:19:22','快件已发货','广州应用科技学院肇庆校区二期菜鸟驿站',NULL,23.269876,112.678191,'2026-06-21 01:19:22');
/*!40000 ALTER TABLE `oms_logistics_trace` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oms_order`
--

DROP TABLE IF EXISTS `oms_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oms_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单编号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `total_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '订单总金额',
  `pay_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '应付金额',
  `freight_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '运费',
  `coupon_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '优惠券抵扣金额',
  `integral_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '积分抵扣金额',
  `user_coupon_id` bigint DEFAULT NULL,
  `user_coupon_id2` bigint DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '订单状态: 0-待付款 1-待发货 2-待收货 3-已完成 4-已取消 5-退款中 6-已退款',
  `pay_type` tinyint DEFAULT NULL COMMENT '支付方式: 1-支付宝 2-微信 3-余额',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `delivery_time` datetime DEFAULT NULL COMMENT '发货时间',
  `receive_time` datetime DEFAULT NULL COMMENT '收货时间',
  `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '取消原因',
  `receiver_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人手机号',
  `receiver_address` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货地址',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '买家留言',
  `source_type` tinyint NOT NULL DEFAULT '1' COMMENT '订单来源: 1-PC 2-H5 3-小程序 4-APP',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oms_order`
--

LOCK TABLES `oms_order` WRITE;
/*!40000 ALTER TABLE `oms_order` DISABLE KEYS */;
INSERT INTO `oms_order` VALUES (1,'SN20240422000001',2,7999.00,7999.00,0.00,0.00,0.00,NULL,NULL,3,NULL,NULL,NULL,NULL,NULL,NULL,'张三','13800138001','北京市北京市朝阳区建国路88号SOHO现代城A座1001室',NULL,1,'2026-04-22 20:55:35','2026-04-22 20:55:35',0),(2,'SN20240422000002',2,5299.00,5299.00,0.00,0.00,0.00,NULL,NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,'张三','13800138001','北京市北京市朝阳区建国路88号SOHO现代城A座1001室',NULL,1,'2026-04-22 20:55:35','2026-06-09 10:18:04',1),(3,'SN20240422000003',3,6999.00,6999.00,0.00,0.00,0.00,NULL,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,'李四','13800138002','上海市上海市浦东新区陆家嘴环路1000号恒生银行大厦20楼',NULL,1,'2026-04-22 20:55:35','2026-04-22 20:55:35',0),(4,'SN20240422000004',3,11598.00,11598.00,0.00,0.00,0.00,NULL,NULL,4,NULL,NULL,NULL,NULL,'2026-06-07 10:16:46','订单超时未支付，系统自动取消','李四','13800138002','上海市上海市浦东新区陆家嘴环路1000号恒生银行大厦20楼',NULL,1,'2026-04-22 20:55:35','2026-04-22 20:55:35',0),(5,'SN20240422000005',2,4599.00,4599.00,10.00,0.00,0.00,NULL,NULL,4,NULL,NULL,NULL,NULL,NULL,NULL,'张三','13800138001','北京市北京市朝阳区建国路88号SOHO现代城A座1001室',NULL,1,'2026-04-22 20:55:35','2026-04-22 20:55:35',0),(6,'SN20260528225834472935',1,7999.00,7999.00,0.00,0.00,0.00,NULL,NULL,1,2,'2026-05-28 23:20:07',NULL,NULL,NULL,NULL,'kaitou','13794057765','广东佛山南海碧桂园E栋D座G1001',NULL,1,'2026-05-28 22:58:35','2026-06-09 10:18:02',1),(7,'SN20260528232127156651',1,6999.00,6999.00,0.00,0.00,0.00,NULL,NULL,1,2,'2026-05-30 11:11:15',NULL,NULL,NULL,NULL,'kaitou','13794057765','广东佛山南海碧桂园E栋D座G1001',NULL,1,'2026-05-28 23:21:28','2026-06-09 10:18:00',1),(8,'SN20260530110957698957',1,7999.00,7199.10,0.00,799.90,0.00,6,NULL,1,2,'2026-05-30 11:11:12',NULL,NULL,NULL,NULL,'kaitou','13794057765','广东佛山南海碧桂园E栋D座G1001',NULL,3,'2026-05-30 11:09:58','2026-06-09 10:17:57',1),(9,'SN20260530113645288783',1,7999.00,7199.10,0.00,799.90,0.00,2,NULL,3,2,'2026-05-30 11:36:58','2026-05-30 11:37:46','2026-05-30 11:41:14',NULL,NULL,'kaitou','13794057765','广东佛山南海碧桂园E栋D座G1001',NULL,3,'2026-05-30 11:36:46','2026-05-30 11:36:46',0),(10,'SN20260530114141581908',1,4599.00,4549.00,0.00,50.00,0.00,8,NULL,1,2,'2026-05-30 11:41:44',NULL,NULL,NULL,NULL,'kaitou','13794057765','广东佛山南海碧桂园E栋D座G1001',NULL,3,'2026-05-30 11:41:41','2026-06-09 10:17:56',1),(11,'SN20260530134304884861',1,4599.00,4549.00,0.00,50.00,0.00,4,NULL,1,2,'2026-05-30 13:43:06',NULL,NULL,NULL,NULL,'kaitou','13794057765','广东佛山南海碧桂园E栋D座G1001',NULL,1,'2026-05-30 13:43:04','2026-06-09 10:17:52',1),(12,'SN20260530135425922873',1,2999.00,2979.00,0.00,20.00,0.00,5,NULL,6,3,'2026-05-30 13:54:36',NULL,NULL,NULL,NULL,'kaitou','13794057765','广东佛山南海碧桂园E栋D座G1001',NULL,1,'2026-05-30 13:54:25','2026-05-30 13:54:25',0),(13,'SN20260530155846879342',1,2999.00,2979.00,0.00,20.00,0.00,1,NULL,4,NULL,NULL,NULL,NULL,'2026-05-30 16:28:17','取消','kaitou','13794057765','广东佛山南海碧桂园E栋D座G1001',NULL,1,'2026-05-30 15:58:47','2026-05-30 17:09:40',1),(14,'SN20260530171059035400',1,2999.00,2999.00,0.00,0.00,0.00,NULL,NULL,6,1,'2026-06-01 12:29:14',NULL,NULL,NULL,NULL,'kaitou','13794057765','广东佛山南海碧桂园E栋D座G1001',NULL,1,'2026-05-30 17:11:00','2026-06-09 10:17:45',0),(15,'SN20260601130551084104',18,13999.00,13999.00,0.00,0.00,0.00,NULL,NULL,4,NULL,NULL,NULL,NULL,'2026-06-07 10:16:46','订单超时未支付，系统自动取消','黄先生','13794057765','广东广州黄埔广东省广州市黄埔区汤臣一品6栋201',NULL,3,'2026-06-01 13:05:51','2026-06-01 13:05:51',0),(16,'SN20260601150423430991',1,13999.00,13999.00,0.00,0.00,0.00,NULL,NULL,6,1,'2026-06-01 15:32:26',NULL,NULL,NULL,NULL,'kaitou','13794057765','广东佛山南海碧桂园E栋D座G1001',NULL,1,'2026-06-01 15:04:23','2026-06-09 09:57:33',0),(17,'SN20260601154539803096',1,7999.00,7999.00,0.00,0.00,0.00,NULL,NULL,6,1,'2026-06-01 15:57:11',NULL,NULL,NULL,NULL,'kaitou','13794057765','广东佛山南海碧桂园E栋D座G1001',NULL,1,'2026-06-01 15:45:40','2026-06-09 09:19:58',0),(18,'SN20260601162928512013',1,6999.00,6999.00,0.00,0.00,0.00,NULL,NULL,5,1,'2026-06-01 16:30:01',NULL,NULL,NULL,NULL,'kaitou','13794057765','广东佛山南海碧桂园E栋D座G1001',NULL,1,'2026-06-01 16:29:28','2026-06-01 16:53:32',1),(19,'SN20260601165345640857',1,6999.00,6999.00,0.00,0.00,0.00,NULL,NULL,4,NULL,NULL,NULL,NULL,'2026-06-01 16:54:11','取消','kaitou','13794057765','广东佛山南海碧桂园E栋D座G1001',NULL,1,'2026-06-01 16:53:45','2026-06-01 16:53:45',0),(20,'SN20260601165516824202',1,6999.00,6999.00,0.00,0.00,0.00,NULL,NULL,5,1,'2026-06-01 16:55:51',NULL,NULL,NULL,NULL,'kaitou','13794057765','广东佛山南海碧桂园E栋D座G1001',NULL,1,'2026-06-01 16:55:17','2026-06-01 17:10:22',1),(21,'SN20260601171037374980',1,6999.00,6999.00,0.00,0.00,0.00,NULL,NULL,6,1,'2026-06-01 17:11:08',NULL,NULL,NULL,NULL,'kaitou','13794057765','广东佛山南海碧桂园E栋D座G1001',NULL,1,'2026-06-01 17:10:37','2026-06-01 17:16:45',0),(22,'SN20260601180152859090',1,13999.00,13999.00,0.00,0.00,0.00,NULL,NULL,3,1,'2026-06-01 18:02:47','2026-06-01 18:03:41','2026-06-01 18:03:53',NULL,NULL,'kaitou','13794057765','广东佛山南海碧桂园E栋D座G1001',NULL,1,'2026-06-01 18:01:53','2026-06-01 18:03:36',0),(23,'SN20260602081733489858',1,6999.00,6999.00,0.00,0.00,0.00,NULL,NULL,4,NULL,NULL,NULL,NULL,'2026-06-02 08:24:33','取消','kaitou','13794057765','广东佛山南海碧桂园E栋D座G1001',NULL,1,'2026-06-02 08:17:34','2026-06-02 08:17:34',0),(24,'SN20260602082427325403',1,6999.00,6999.00,0.00,0.00,0.00,NULL,NULL,6,1,'2026-06-02 08:25:06',NULL,NULL,NULL,NULL,'kaitou','13794057765','广东佛山南海碧桂园E栋D座G1001',NULL,1,'2026-06-02 08:24:27','2026-06-09 08:52:28',0),(25,'SN20260603162856818513',1,13999.00,13949.00,0.00,50.00,0.00,16,NULL,4,NULL,NULL,NULL,NULL,'2026-06-03 16:30:57','用户取消','kaitou','13794057765','广东佛山南海碧桂园E栋D座G1001',NULL,1,'2026-06-03 16:28:57','2026-06-03 16:28:57',0),(26,'SN20260603163240791566',1,13999.00,13949.00,0.00,50.00,0.00,16,NULL,4,NULL,NULL,NULL,NULL,'2026-06-03 16:35:29','用户取消','kaitou','13794057765','广东佛山南海碧桂园E栋D座G1001',NULL,1,'2026-06-03 16:32:40','2026-06-03 16:32:40',0),(27,'SN20260603165449563557',1,13999.00,13949.00,0.00,50.00,0.00,16,NULL,3,1,'2026-06-03 17:00:22','2026-06-03 17:01:16','2026-06-03 17:01:21',NULL,NULL,'kaitou','13794057765','广东佛山南海碧桂园E栋D座G1001',NULL,1,'2026-06-03 16:54:50','2026-06-03 17:00:22',0),(28,'SN20260607131541923531',1,13999.00,13999.00,0.00,0.00,0.00,NULL,NULL,4,NULL,NULL,NULL,NULL,'2026-06-07 13:15:51','用户取消','kaitou','13794057765','广东佛山南海碧桂园E栋D座G1001',NULL,1,'2026-06-07 13:15:42','2026-06-07 13:15:42',0),(29,'SN20260607131609184356',1,2999.00,2999.00,0.00,0.00,0.00,NULL,NULL,3,3,'2026-06-07 13:16:13','2026-06-07 13:17:25','2026-06-07 14:08:58',NULL,NULL,'kaitou','13794057765','广东佛山南海碧桂园E栋D座G1001',NULL,1,'2026-06-07 13:16:10','2026-06-07 13:16:10',0),(30,'SN20260607141504934070',1,2999.00,2999.00,0.00,0.00,0.00,NULL,NULL,6,3,'2026-06-07 14:15:09','2026-06-07 14:17:50',NULL,NULL,NULL,'kaitou','13794057765','广东佛山南海尚东豪庭君瀚苑201',NULL,1,'2026-06-07 14:15:05','2026-06-07 14:15:05',0),(31,'SN20260608144444949982',1,2999.00,2999.00,0.00,0.00,0.00,NULL,NULL,6,3,'2026-06-08 14:44:50','2026-06-08 14:46:08',NULL,NULL,NULL,'kaitou','13794057765','广东省佛山市南海区狮山镇小塘长安路7号邮政快递部',NULL,1,'2026-06-08 14:44:45','2026-06-08 14:44:45',0),(32,'SN20260608171001562019',1,2999.00,2999.00,0.00,0.00,0.00,NULL,NULL,6,3,'2026-06-08 17:10:10','2026-06-08 17:11:47',NULL,NULL,NULL,'kaitou','13794057765','广东省佛山市南海区狮山镇小塘长安路7号',NULL,1,'2026-06-08 17:10:02','2026-06-08 17:10:02',0),(33,'SN20260608173829195592',1,2999.00,2999.00,0.00,0.00,0.00,NULL,NULL,3,3,'2026-06-08 17:38:33','2026-06-08 17:40:34','2026-06-08 20:47:15',NULL,NULL,'kaitou','13794057765','广东省佛山市南海区狮山镇小塘长安路7号',NULL,1,'2026-06-08 17:38:29','2026-06-08 17:38:29',0),(34,'SN20260609092034128445',1,3299.00,3299.00,0.00,0.00,0.00,NULL,NULL,6,1,'2026-06-09 09:21:47',NULL,NULL,NULL,NULL,'kaitou','13794057765','广东省佛山市南海区狮山镇小塘长安路7号',NULL,3,'2026-06-09 09:20:34','2026-06-09 09:22:28',0),(35,'SN20260621010834381774',1,7199.10,7199.10,0.00,0.00,0.00,NULL,NULL,4,NULL,NULL,NULL,NULL,'2026-06-21 01:12:37','用户取消','kaitou','13794057765','广东省佛山市南海区狮山镇小塘长安路7号',NULL,1,'2026-06-21 01:08:34','2026-06-21 01:08:34',0),(36,'SN20260621011255895503',1,7199.10,7199.10,0.00,0.00,0.00,NULL,NULL,4,NULL,NULL,NULL,NULL,'2026-06-21 01:14:49','用户取消','kaitou','13794057765','广东省佛山市南海区狮山镇小塘长安路7号',NULL,1,'2026-06-21 01:12:55','2026-06-21 01:12:55',0),(37,'SN20260621011538706593',1,7199.10,7199.10,0.00,0.00,0.00,NULL,NULL,6,1,'2026-06-21 01:16:01','2026-06-21 01:19:22',NULL,NULL,NULL,'kaitou','13794057765','广东省佛山市南海区狮山镇小塘长安路7号',NULL,1,'2026-06-21 01:15:39','2026-06-21 01:21:55',0),(38,'SN20260621015738803392',1,7199.10,7199.10,0.00,0.00,0.00,NULL,NULL,1,1,'2026-06-21 01:58:10',NULL,NULL,NULL,NULL,'kaitou','13794057765','广东省佛山市南海区狮山镇小塘长安路7号',NULL,1,'2026-06-21 01:57:39','2026-06-21 01:58:10',0);
/*!40000 ALTER TABLE `oms_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oms_order_item`
--

DROP TABLE IF EXISTS `oms_order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oms_order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `sku_id` bigint DEFAULT NULL COMMENT 'SKU ID',
  `product_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称',
  `product_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品图片',
  `sku_specs` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'SKU规格',
  `price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '单价',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '数量',
  `subtotal` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '小计金额',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单商品明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oms_order_item`
--

LOCK TABLES `oms_order_item` WRITE;
/*!40000 ALTER TABLE `oms_order_item` DISABLE KEYS */;
INSERT INTO `oms_order_item` VALUES (1,1,1,NULL,'iPhone 15 Pro','https://example.com/iphone15pro.jpg',NULL,7999.00,1,7999.00,'2026-04-22 20:55:35','2026-04-22 20:55:35',0),(2,2,2,NULL,'iPhone 15','https://example.com/iphone15.jpg',NULL,5299.00,1,5299.00,'2026-04-22 20:55:35','2026-06-09 10:18:03',1),(3,3,3,NULL,'华为 Mate 60 Pro','https://example.com/mate60pro.jpg',NULL,6999.00,1,6999.00,'2026-04-22 20:55:35','2026-04-22 20:55:35',0),(4,4,1,NULL,'iPhone 15 Pro','https://example.com/iphone15pro.jpg',NULL,7999.00,1,7999.00,'2026-04-22 20:55:35','2026-04-22 20:55:35',0),(5,4,2,NULL,'iPhone 15','https://example.com/iphone15.jpg',NULL,3599.00,1,3599.00,'2026-04-22 20:55:35','2026-04-22 20:55:35',0),(6,5,4,NULL,'小米14 Pro','https://example.com/mi14pro.jpg',NULL,4599.00,1,4599.00,'2026-04-22 20:55:35','2026-04-22 20:55:35',0),(7,22,10,NULL,'HuaWei MateBook Pro','http://localhost:9000/shopmax/product/20260601/54757300.png',NULL,13999.00,1,13999.00,'2026-06-01 18:01:53','2026-06-01 18:01:53',0),(8,23,8,NULL,'华为 Mate 60 Pro','http://localhost:9000/shopmax/product/20260601/b5a396d9.png',NULL,6999.00,1,6999.00,'2026-06-02 08:17:34','2026-06-02 08:17:34',0),(9,24,8,NULL,'华为 Mate 60 Pro','http://localhost:9000/shopmax/product/20260601/b5a396d9.png',NULL,6999.00,1,6999.00,'2026-06-02 08:24:27','2026-06-02 08:24:27',0),(10,25,10,NULL,'HuaWei MateBook Pro','http://localhost:9000/shopmax/product/20260601/54757300.png',NULL,13999.00,1,13999.00,'2026-06-03 16:28:57','2026-06-03 16:28:57',0),(11,26,10,NULL,'HuaWei MateBook Pro','http://localhost:9000/shopmax/product/20260601/54757300.png',NULL,13999.00,1,13999.00,'2026-06-03 16:32:40','2026-06-03 16:32:40',0),(12,27,10,NULL,'HuaWei MateBook Pro','http://localhost:9000/shopmax/product/20260601/54757300.png',NULL,13999.00,1,13999.00,'2026-06-03 16:54:50','2026-06-03 16:54:50',0),(13,28,10,NULL,'HuaWei MateBook Pro','http://localhost:9000/shopmax/product/20260601/54757300.png',NULL,13999.00,1,13999.00,'2026-06-07 13:15:42','2026-06-07 13:15:42',0),(14,29,5,NULL,'iQOO12','http://localhost:9000/shopmax/product/20260529/01adda31.jpg',NULL,2999.00,1,2999.00,'2026-06-07 13:16:10','2026-06-07 13:16:10',0),(15,30,5,NULL,'iQOO12','http://localhost:9000/shopmax/product/20260529/01adda31.jpg',NULL,2999.00,1,2999.00,'2026-06-07 14:15:05','2026-06-07 14:15:05',0),(16,31,5,NULL,'iQOO12','http://localhost:9000/shopmax/product/20260529/01adda31.jpg',NULL,2999.00,1,2999.00,'2026-06-08 14:44:45','2026-06-08 14:44:45',0),(17,32,5,NULL,'iQOO12','http://localhost:9000/shopmax/product/20260529/01adda31.jpg',NULL,2999.00,1,2999.00,'2026-06-08 17:10:02','2026-06-08 17:10:02',0),(18,33,5,NULL,'iQOO12','http://localhost:9000/shopmax/product/20260529/01adda31.jpg',NULL,2999.00,1,2999.00,'2026-06-08 17:38:29','2026-06-08 17:38:29',0),(19,34,2,NULL,'iQOO Neo9','http://localhost:9000/shopmax/product/20260529/5063b966.jpg',NULL,3299.00,1,3299.00,'2026-06-09 09:20:34','2026-06-09 09:20:34',0),(20,35,6,NULL,'iPhone 15 Pro','http://localhost:9000/shopmax/product/20260601/75015cdb.png',NULL,7999.00,1,7999.00,'2026-06-21 01:08:34','2026-06-21 01:08:34',0),(21,36,6,NULL,'iPhone 15 Pro','http://localhost:9000/shopmax/product/20260601/75015cdb.png',NULL,7999.00,1,7999.00,'2026-06-21 01:12:55','2026-06-21 01:12:55',0),(22,37,6,NULL,'iPhone 15 Pro','http://localhost:9000/shopmax/product/20260601/75015cdb.png',NULL,7999.00,1,7999.00,'2026-06-21 01:15:39','2026-06-21 01:15:39',0),(23,38,6,NULL,'iPhone 15 Pro','http://localhost:9000/shopmax/product/20260601/75015cdb.png',NULL,7999.00,1,7999.00,'2026-06-21 01:57:39','2026-06-21 01:57:39',0);
/*!40000 ALTER TABLE `oms_order_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oms_order_log`
--

DROP TABLE IF EXISTS `oms_order_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oms_order_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(64) NOT NULL COMMENT '订单编号',
  `old_status` varchar(32) DEFAULT NULL COMMENT '操作前状态',
  `action` varchar(32) NOT NULL COMMENT '操作动作: CREATE/PAY/SHIP/CONFIRM/CANCEL/REFUND/REFUND_APPLY/DELETE',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单操作日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oms_order_log`
--

LOCK TABLES `oms_order_log` WRITE;
/*!40000 ALTER TABLE `oms_order_log` DISABLE KEYS */;
INSERT INTO `oms_order_log` VALUES (1,25,'SN20260603162856818513','待付款','CANCEL','用户取消',1,'2026-06-03 16:30:57'),(2,26,'SN20260603163240791566','待付款','CANCEL','用户取消',1,'2026-06-03 16:35:29'),(3,27,'SN20260603165449563557','待发货','SHIP','订单已发货',1,'2026-06-03 17:01:16'),(4,27,'SN20260603165449563557','待收货','CONFIRM','确认收货，获得139490积分',1,'2026-06-03 17:01:21'),(5,28,'SN20260607131541923531','待付款','CANCEL','用户取消',1,'2026-06-07 13:15:51'),(6,29,'SN20260607131609184356','待付款','PAY','支付成功，支付方式: 余额',1,'2026-06-07 13:16:13'),(7,29,'SN20260607131609184356','待发货','SHIP','订单已发货',1,'2026-06-07 13:17:25'),(8,29,'SN20260607131609184356','待收货','CONFIRM','确认收货，获得29990积分',1,'2026-06-07 14:08:58'),(9,30,'SN20260607141504934070','待付款','PAY','支付成功，支付方式: 余额',1,'2026-06-07 14:15:09'),(10,30,'SN20260607141504934070','待发货','SHIP','订单已发货',1,'2026-06-07 14:17:50'),(11,30,'SN20260607141504934070','待收货','REFUND','余额退款成功: 用户申请退款',1,'2026-06-08 14:35:22'),(12,31,'SN20260608144444949982','待付款','PAY','支付成功，支付方式: 余额',1,'2026-06-08 14:44:50'),(13,31,'SN20260608144444949982','待发货','SHIP','订单已发货',1,'2026-06-08 14:46:08'),(14,31,'SN20260608144444949982','待收货','REFUND','余额退款成功: 用户申请退款',1,'2026-06-08 17:09:21'),(15,32,'SN20260608171001562019','待付款','PAY','支付成功，支付方式: 余额',1,'2026-06-08 17:10:10'),(16,32,'SN20260608171001562019','待发货','SHIP','订单已发货',1,'2026-06-08 17:11:47'),(17,32,'SN20260608171001562019','待收货','REFUND','余额退款成功: 用户申请退款',1,'2026-06-08 17:35:34'),(18,33,'SN20260608173829195592','待付款','PAY','支付成功，支付方式: 余额',1,'2026-06-08 17:38:33'),(19,33,'SN20260608173829195592','待发货','SHIP','订单已发货',1,'2026-06-08 17:40:34'),(20,33,'SN20260608173829195592','待收货','CONFIRM','确认收货，获得29990积分',1,'2026-06-08 20:47:15'),(21,24,'SN20260602082427325403','待发货','REFUND_APPLY','申请退款: 用户申请退款',1,'2026-06-08 22:53:37'),(22,17,'SN20260601154539803096','待发货','REFUND_APPLY','申请退款: 用户申请退款',1,'2026-06-09 08:41:19'),(23,34,'SN20260609092034128445','待发货','REFUND_APPLY','申请退款: 用户申请退款',1,'2026-06-09 09:22:10'),(24,16,'SN20260601150423430991','待发货','REFUND_APPLY','申请退款: 用户申请退款',1,'2026-06-09 09:55:03'),(25,14,'SN20260530171059035400','待发货','REFUND_APPLY','申请退款: 用户申请退款',1,'2026-06-09 10:00:28'),(26,11,'SN20260530134304884861','待发货','DELETE','订单删除',1,'2026-06-09 10:17:52'),(27,10,'SN20260530114141581908','待发货','DELETE','订单删除',1,'2026-06-09 10:17:56'),(28,8,'SN20260530110957698957','待发货','DELETE','订单删除',1,'2026-06-09 10:17:57'),(29,7,'SN20260528232127156651','待发货','DELETE','订单删除',1,'2026-06-09 10:18:00'),(30,6,'SN20260528225834472935','待发货','DELETE','订单删除',1,'2026-06-09 10:18:02'),(31,2,'SN20240422000002','待发货','DELETE','订单删除',1,'2026-06-09 10:18:04'),(32,35,'SN20260621010834381774','待付款','CANCEL','用户取消',1,'2026-06-21 01:12:37'),(33,36,'SN20260621011255895503','待付款','CANCEL','用户取消',1,'2026-06-21 01:14:49'),(34,37,'SN20260621011538706593','待发货','SHIP','订单已发货',1,'2026-06-21 01:19:22'),(35,37,'SN20260621011538706593','待收货','REFUND_APPLY','申请退款: 用户申请退款',1,'2026-06-21 01:21:33');
/*!40000 ALTER TABLE `oms_order_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oms_payment`
--

DROP TABLE IF EXISTS `oms_payment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oms_payment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `payment_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '支付单号',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `amount` decimal(10,2) NOT NULL COMMENT '支付金额',
  `pay_method` tinyint NOT NULL COMMENT '支付方式: 1-支付宝 2-微信 3-余额',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-待支付 1-支付成功 2-支付失败 3-退款中 4-已退款',
  `transaction_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方交易号',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `callback_time` datetime DEFAULT NULL COMMENT '回调时间',
  `refund_time` datetime DEFAULT NULL COMMENT '退款时间',
  `refund_amount` decimal(10,2) DEFAULT NULL COMMENT '退款金额',
  `refund_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '退款原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付流水表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oms_payment`
--

LOCK TABLES `oms_payment` WRITE;
/*!40000 ALTER TABLE `oms_payment` DISABLE KEYS */;
INSERT INTO `oms_payment` VALUES (9,'PAY202605301711063843',14,'SN20260530171059035400',1,2999.00,1,4,'ALI_MOCK_20260601122914','2026-06-01 12:29:15','2026-06-01 12:29:15','2026-06-09 10:17:45',2999.00,'用户申请退款','2026-05-30 17:11:06','2026-05-30 17:11:06'),(10,'PAY202606011305551681',15,'SN20260601130551084104',18,13999.00,1,0,NULL,NULL,NULL,NULL,NULL,NULL,'2026-06-01 13:05:55','2026-06-01 13:05:55'),(11,'PAY202606011504288119',16,'SN20260601150423430991',1,13999.00,1,4,'2026060122001443600509847070','2026-06-01 15:32:26','2026-06-01 15:32:26','2026-06-09 09:57:34',13999.00,'用户申请退款','2026-06-01 15:04:28','2026-06-01 15:04:28'),(12,'PAY202606011545430732',17,'SN20260601154539803096',1,7999.00,1,4,'2026060122001443600509847071','2026-06-01 15:57:11','2026-06-01 15:57:11','2026-06-09 09:19:59',7999.00,'用户申请退款','2026-06-01 15:45:43','2026-06-01 15:45:43'),(13,'PAY202606011629349657',18,'SN20260601162928512013',1,6999.00,1,1,'2026060122001443600509847072','2026-06-01 16:30:02','2026-06-01 16:30:02',NULL,NULL,NULL,'2026-06-01 16:29:34','2026-06-01 16:29:34'),(14,'PAY202606011653494769',19,'SN20260601165345640857',1,6999.00,1,0,NULL,NULL,NULL,NULL,NULL,NULL,'2026-06-01 16:53:49','2026-06-01 16:53:49'),(15,'PAY202606011655195323',20,'SN20260601165516824202',1,6999.00,1,1,'2026060122001443600509844123','2026-06-01 16:55:52','2026-06-01 16:55:52',NULL,NULL,NULL,'2026-06-01 16:55:19','2026-06-01 16:55:19'),(16,'PAY202606011710407737',21,'SN20260601171037374980',1,6999.00,1,4,'2026060122001443600509847073','2026-06-01 17:11:09','2026-06-01 17:11:09','2026-06-01 17:16:45',6999.00,'用户申请退款','2026-06-01 17:10:40','2026-06-01 17:10:40'),(17,'PAY202606011801579089',22,'SN20260601180152859090',1,13999.00,1,1,'2026060122001443600509848964','2026-06-01 18:02:48','2026-06-01 18:02:48',NULL,NULL,NULL,'2026-06-01 18:01:57','2026-06-01 18:01:57'),(18,'PAY202606020817398798',23,'SN20260602081733489858',1,6999.00,1,0,NULL,NULL,NULL,NULL,NULL,NULL,'2026-06-02 08:17:39','2026-06-02 08:17:39'),(19,'PAY202606020824364478',24,'SN20260602082427325403',1,6999.00,1,4,'2026060222001443600509850237','2026-06-02 08:25:07','2026-06-02 08:25:07','2026-06-09 08:52:29',6999.00,'用户申请退款','2026-06-02 08:24:36','2026-06-02 08:24:36'),(20,'PAY202606031629029666',25,'SN20260603162856818513',1,13949.00,1,0,NULL,NULL,NULL,NULL,NULL,NULL,'2026-06-03 16:29:02','2026-06-03 16:29:02'),(21,'PAY202606031632449483',26,'SN20260603163240791566',1,13949.00,1,0,NULL,NULL,NULL,NULL,NULL,NULL,'2026-06-03 16:32:44','2026-06-03 16:32:44'),(22,'PAY202606031700066776',27,'SN20260603165449563557',1,13949.00,1,1,'2026060322001443600509862433','2026-06-03 17:00:22','2026-06-03 17:00:22',NULL,NULL,NULL,'2026-06-03 17:00:06','2026-06-03 17:00:06'),(23,'PAY202606090920412486',34,'SN20260609092034128445',1,3299.00,1,4,'2026060922001443600509891962','2026-06-09 09:21:48','2026-06-09 09:21:48','2026-06-09 09:22:29',3299.00,'用户申请退款','2026-06-09 09:20:41','2026-06-09 09:20:41'),(24,'PAY202606210108414027',35,'SN20260621010834381774',1,7199.10,1,0,NULL,NULL,NULL,NULL,NULL,NULL,'2026-06-21 01:08:41','2026-06-21 01:08:41'),(25,'PAY202606210112598445',36,'SN20260621011255895503',1,7199.10,1,0,NULL,NULL,NULL,NULL,NULL,NULL,'2026-06-21 01:12:59','2026-06-21 01:12:59'),(26,'PAY202606210115449807',37,'SN20260621011538706593',1,7199.10,1,4,'2026062122001443600509954314','2026-06-21 01:16:02','2026-06-21 01:16:02','2026-06-21 01:21:55',7199.10,'用户申请退款','2026-06-21 01:15:44','2026-06-21 01:15:44'),(27,'PAY202606210157482463',38,'SN20260621015738803392',1,7199.10,1,1,'2026062122001443600509952582','2026-06-21 01:58:10','2026-06-21 01:58:10',NULL,NULL,NULL,'2026-06-21 01:57:48','2026-06-21 01:57:48');
/*!40000 ALTER TABLE `oms_payment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oms_refund_record`
--

DROP TABLE IF EXISTS `oms_refund_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oms_refund_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `refund_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '退款单号（幂等键，对应支付宝 out_request_no）',
  `payment_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关联支付单号',
  `order_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关联订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `refund_amount` decimal(10,2) NOT NULL COMMENT '本次退款金额',
  `refund_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '退款原因',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '退款状态: 0-处理中 1-退款成功 2-退款失败',
  `pay_method` tinyint NOT NULL COMMENT '支付方式: 1-支付宝 2-微信 3-余额',
  `gateway_refund_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方退款流水号',
  `fail_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '退款失败原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  KEY `idx_payment_no` (`payment_no`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oms_refund_record`
--

LOCK TABLES `oms_refund_record` WRITE;
/*!40000 ALTER TABLE `oms_refund_record` DISABLE KEYS */;
INSERT INTO `oms_refund_record` VALUES (1,'RF202606011716279127','PAY202606011710407737','SN20260601171037374980',1,6999.00,'用户申请退款',1,1,'2026060122001443600509847073',NULL,'2026-06-01 17:16:27','2026-06-01 17:16:27'),(2,'RF202606011803044250','PAY202606011801579089','SN20260601180152859090',1,13999.00,'用户申请退款',2,1,NULL,'商品已经在路上咯','2026-06-01 18:03:04','2026-06-01 18:03:04'),(3,'RF202606082253360297','PAY202606020824364478','SN20260602082427325403',1,6999.00,'用户申请退款',1,1,'MANUAL_1780966348772','管理员手动标记退款成功（旧订单支付网关不可用）','2026-06-08 22:53:36','2026-06-08 22:53:36'),(4,'RF202606090841194993','PAY202606011545430732','SN20260601154539803096',1,7999.00,'用户申请退款',1,1,'MANUAL_1780967998683','管理员手动标记退款成功（旧订单支付网关不可用）','2026-06-09 08:41:19','2026-06-09 08:41:19'),(5,'RF202606090922094832','PAY202606090920412486','SN20260609092034128445',1,3299.00,'用户申请退款',1,1,'2026060922001443600509891962',NULL,'2026-06-09 09:22:09','2026-06-09 09:22:09'),(6,'RF202606090955022388','PAY202606011504288119','SN20260601150423430991',1,13999.00,'用户申请退款',1,1,'MANUAL_1780970253591','管理员手动标记退款成功（旧订单支付网关不可用）','2026-06-09 09:55:02','2026-06-09 09:55:02'),(7,'RF202606091000270264','PAY202605301711063843','SN20260530171059035400',1,2999.00,'用户申请退款',1,1,'MANUAL_1780971465277','管理员手动标记退款成功（旧订单支付网关不可用）','2026-06-09 10:00:27','2026-06-09 10:00:27'),(8,'RF202606210121322438','PAY202606210115449807','SN20260621011538706593',1,7199.10,'用户申请退款',1,1,'2026062122001443600509954314',NULL,'2026-06-21 01:21:32','2026-06-21 01:21:32');
/*!40000 ALTER TABLE `oms_refund_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oms_seckill_order`
--

DROP TABLE IF EXISTS `oms_seckill_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oms_seckill_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` varchar(64) NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `session_id` bigint NOT NULL COMMENT '秒杀场次ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `seckill_price` decimal(10,2) NOT NULL COMMENT '秒杀价',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0-待支付 1-已支付 2-已取消 3-已超时',
  `expire_time` datetime NOT NULL COMMENT '支付过期时间',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `order_id` bigint DEFAULT NULL COMMENT '正式订单ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  UNIQUE KEY `uk_user_session_product` (`user_id`,`session_id`,`product_id`),
  KEY `idx_status_expire` (`status`,`expire_time`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='秒杀订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oms_seckill_order`
--

LOCK TABLES `oms_seckill_order` WRITE;
/*!40000 ALTER TABLE `oms_seckill_order` DISABLE KEYS */;
/*!40000 ALTER TABLE `oms_seckill_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pay_payment`
--

DROP TABLE IF EXISTS `pay_payment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pay_payment` (
  `id` bigint NOT NULL,
  `payment_no` varchar(64) NOT NULL COMMENT '支付单号',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(64) NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `amount` decimal(10,2) NOT NULL COMMENT '支付金额',
  `channel` varchar(32) NOT NULL COMMENT '支付渠道 ALIPAY/WX/PAY',
  `status` tinyint DEFAULT '0' COMMENT '状态 0-待支付 1-已支付 2-已退款',
  `trade_no` varchar(128) DEFAULT NULL COMMENT '第三方交易号',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pay_payment`
--

LOCK TABLES `pay_payment` WRITE;
/*!40000 ALTER TABLE `pay_payment` DISABLE KEYS */;
/*!40000 ALTER TABLE `pay_payment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pay_refund_record`
--

DROP TABLE IF EXISTS `pay_refund_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pay_refund_record` (
  `id` bigint NOT NULL,
  `refund_no` varchar(64) NOT NULL COMMENT '退款单号',
  `payment_id` bigint NOT NULL COMMENT '支付单ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `amount` decimal(10,2) NOT NULL COMMENT '退款金额',
  `reason` varchar(500) DEFAULT NULL COMMENT '退款原因',
  `status` tinyint DEFAULT '0' COMMENT '状态 0-待退款 1-已退款 2-退款失败',
  `trade_no` varchar(128) DEFAULT NULL COMMENT '第三方退款号',
  `refund_time` datetime DEFAULT NULL COMMENT '退款时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  KEY `idx_payment_id` (`payment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='退款记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pay_refund_record`
--

LOCK TABLES `pay_refund_record` WRITE;
/*!40000 ALTER TABLE `pay_refund_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `pay_refund_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pms_brand`
--

DROP TABLE IF EXISTS `pms_brand`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pms_brand` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '品牌ID',
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '品牌名称',
  `logo` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '品牌Logo',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '品牌描述',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='品牌表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pms_brand`
--

LOCK TABLES `pms_brand` WRITE;
/*!40000 ALTER TABLE `pms_brand` DISABLE KEYS */;
INSERT INTO `pms_brand` VALUES (1,'Apple','https://example.com/apple.png','苹果公司',1,1,'2026-04-22 20:55:30','2026-04-22 20:55:30',0),(2,'华为','https://example.com/huawei.png','华为技术有限公司',2,1,'2026-04-22 20:55:30','2026-04-22 20:55:30',0),(3,'小米','https://example.com/xiaomi.png','小米科技有限责任公司',3,1,'2026-04-22 20:55:30','2026-04-22 20:55:30',0),(4,'联想','https://example.com/lenovo.png','联想集团',4,1,'2026-04-22 20:55:30','2026-04-22 20:55:30',0),(5,'海尔','https://example.com/haier.png','海尔集团',5,1,'2026-04-22 20:55:30','2026-04-22 20:55:30',0);
/*!40000 ALTER TABLE `pms_brand` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pms_category`
--

DROP TABLE IF EXISTS `pms_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pms_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父分类ID',
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `level` tinyint NOT NULL DEFAULT '1' COMMENT '分类层级: 1-一级 2-二级 3-三级',
  `icon` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分类图标',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_level` (`level`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pms_category`
--

LOCK TABLES `pms_category` WRITE;
/*!40000 ALTER TABLE `pms_category` DISABLE KEYS */;
INSERT INTO `pms_category` VALUES (1,0,'手机数码',1,'phone',1,1,'2026-04-22 20:55:30','2026-04-22 20:55:30',0),(2,0,'电脑办公',1,'computer',2,1,'2026-04-22 20:55:30','2026-04-22 20:55:30',0),(3,0,'家用电器',1,'appliance',3,1,'2026-04-22 20:55:30','2026-04-22 20:55:30',0),(4,0,'服装鞋包',1,'clothing',4,1,'2026-04-22 20:55:30','2026-04-22 20:55:30',0),(5,1,'手机',2,'',1,1,'2026-04-22 20:55:30','2026-04-22 20:55:30',0),(6,1,'平板',2,'',2,1,'2026-04-22 20:55:30','2026-04-22 20:55:30',0),(7,1,'配件',2,'',3,1,'2026-04-22 20:55:30','2026-04-22 20:55:30',0),(8,2,'笔记本',2,'',1,1,'2026-04-22 20:55:30','2026-04-22 20:55:30',0),(9,2,'台式机',2,'',2,1,'2026-04-22 20:55:30','2026-04-22 20:55:30',0),(10,5,'iPhone',3,'',1,1,'2026-04-22 20:55:30','2026-04-22 20:55:30',0),(11,5,'华为手机',3,'',2,1,'2026-04-22 20:55:30','2026-04-22 20:55:30',0),(12,5,'小米手机',3,'',3,1,'2026-04-22 20:55:30','2026-04-22 20:55:30',0);
/*!40000 ALTER TABLE `pms_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pms_new_product_banner`
--

DROP TABLE IF EXISTS `pms_new_product_banner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pms_new_product_banner` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Banner标题',
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Banner图片',
  `product_id` bigint DEFAULT NULL COMMENT '关联商品ID（点击跳转商品详情）',
  `link_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '外部链接（与product_id二选一）',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '0-禁用 1-启用',
  `start_time` datetime DEFAULT NULL COMMENT '展示开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '展示结束时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_status_sort` (`status`,`sort`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='新品首发Banner推荐位';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pms_new_product_banner`
--

LOCK TABLES `pms_new_product_banner` WRITE;
/*!40000 ALTER TABLE `pms_new_product_banner` DISABLE KEYS */;
INSERT INTO `pms_new_product_banner` VALUES (1,'iPhone 15 Pro 新品首发','/api/v1/files/default/product',1,NULL,100,1,NULL,NULL,'2026-06-21 00:30:11','2026-06-21 00:31:50',0),(2,'华为 Mate 60 Pro 限量发售','/api/v1/files/default/product',3,NULL,90,1,NULL,NULL,'2026-06-21 00:30:11','2026-06-21 00:30:11',0),(3,'小米14 Pro 震撼上市','/api/v1/files/default/product',4,NULL,80,1,NULL,NULL,'2026-06-21 00:30:11','2026-06-21 00:30:11',0);
/*!40000 ALTER TABLE `pms_new_product_banner` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pms_product`
--

DROP TABLE IF EXISTS `pms_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pms_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称',
  `subtitle` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品副标题',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '商品描述',
  `main_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品主图',
  `sub_images` varchar(2000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品副图，逗号分隔',
  `detail` text COLLATE utf8mb4_unicode_ci COMMENT '商品详情',
  `category_id` bigint DEFAULT NULL COMMENT '分类ID',
  `brand_id` bigint DEFAULT NULL COMMENT '品牌ID',
  `original_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '原价',
  `sale_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '销售价',
  `stock` int NOT NULL DEFAULT '0' COMMENT '库存数量',
  `sales` int NOT NULL DEFAULT '0' COMMENT '销量',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-下架 1-上架',
  `is_recommend` tinyint NOT NULL DEFAULT '0' COMMENT '是否推荐: 0-否 1-是',
  `is_new` tinyint NOT NULL DEFAULT '0' COMMENT '是否新品: 0-否 1-是',
  `new_product_sort` int NOT NULL DEFAULT '0' COMMENT '新品排序权重（数值越大越靠前）',
  `new_product_start_time` datetime DEFAULT NULL COMMENT '新品上架时间（为空则永久展示）',
  `new_product_end_time` datetime DEFAULT NULL COMMENT '新品下架时间（为空则不自动过期）',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建者用户ID（店家关联）',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_brand_id` (`brand_id`),
  KEY `idx_status` (`status`),
  KEY `idx_is_recommend` (`is_recommend`),
  KEY `idx_is_new` (`is_new`),
  KEY `idx_create_user_id` (`create_user_id`),
  KEY `idx_product_new_sort` (`deleted`,`status`,`is_new`,`new_product_sort`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品SPU表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pms_product`
--

LOCK TABLES `pms_product` WRITE;
/*!40000 ALTER TABLE `pms_product` DISABLE KEYS */;
INSERT INTO `pms_product` VALUES (1,'iPhone 13','A17 Pro芯片，钛金属设计',NULL,'http://localhost:9000/shopmax/product/20260529/ba5fe9cb.jpg',NULL,NULL,1,1,8999.00,7999.00,101,500,0,1,1,0,NULL,NULL,0,'2026-04-22 20:55:30','2026-06-07 10:16:45',0,NULL),(2,'iQOO Neo9','A16芯片，灵动岛设计',NULL,'http://localhost:9000/shopmax/product/20260529/5063b966.jpg',NULL,NULL,1,1,2999.00,3299.00,201,800,1,1,1,0,NULL,NULL,0,'2026-04-22 20:55:30','2026-06-09 09:22:28',0,NULL),(3,'华为 Mate 60 Pro','麒麟9000S，卫星通话',NULL,'http://localhost:9000/shopmax/product/20260529/1bffeea2.jpg',NULL,NULL,1,2,6999.00,6999.00,50,1000,1,1,1,0,NULL,NULL,0,'2026-04-22 20:55:30','2026-04-22 20:55:30',0,NULL),(4,'小米14 Pro','骁龙8 Gen3，徕卡影像',NULL,'http://localhost:9000/shopmax/product/20260529/d1cf7042.jpg',NULL,NULL,1,1,4999.00,4599.00,150,600,1,1,1,0,NULL,NULL,0,'2026-04-22 20:55:30','2026-04-22 20:55:30',0,NULL),(5,'iQOO12','M3芯片，专业级性能',NULL,'http://localhost:9000/shopmax/product/20260529/01adda31.jpg',NULL,NULL,1,1,2999.00,2999.00,78,302,1,1,0,0,NULL,NULL,0,'2026-04-22 20:55:30','2026-06-08 20:47:15',0,NULL),(6,'iPhone 15 Pro','A17 Pro芯片，钛金属设计',NULL,'http://localhost:9000/shopmax/product/20260601/75015cdb.png',NULL,NULL,10,1,8999.00,7999.00,99,500,1,1,1,0,'2026-06-21 01:25:59','2026-06-30 00:00:00',0,'2026-05-31 15:26:27','2026-06-21 01:57:38',0,NULL),(7,'iPhone 15','A16芯片，灵动岛设计',NULL,'http://localhost:9000/shopmax/product/20260601/52af81aa.png',NULL,NULL,10,1,5999.00,5299.00,200,800,1,1,1,0,NULL,NULL,0,'2026-05-31 15:26:27','2026-05-31 15:26:27',0,NULL),(8,'华为 Mate 60 Pro','麒麟9000S，卫星通话',NULL,'http://localhost:9000/shopmax/product/20260601/b5a396d9.png',NULL,NULL,11,2,6999.00,6999.00,50,1000,1,1,1,0,NULL,NULL,0,'2026-05-31 15:26:27','2026-06-09 08:52:28',0,NULL),(9,'小米14 Pro','骁龙8 Gen3，徕卡影像',NULL,'http://localhost:9000/shopmax/product/20260601/97cea77b.png',NULL,NULL,12,3,4999.00,4599.00,150,600,1,1,1,0,NULL,NULL,0,'2026-05-31 15:26:27','2026-05-31 15:26:27',0,NULL),(10,'HuaWei MateBook Pro','M3芯片，专业级性能',NULL,'http://localhost:9000/shopmax/product/20260601/54757300.png',NULL,NULL,8,2,14999.00,13999.00,78,302,1,1,0,0,NULL,NULL,0,'2026-05-31 15:26:27','2026-06-07 13:15:50',0,NULL),(11,'iPhone 15 Pro','99新，免运费，2年质保','','http://localhost:9000/shopmax/product/20260601/3fd0576a.png',NULL,NULL,1,1,4999.00,3888.00,3,0,1,1,0,0,NULL,NULL,0,'2026-06-01 13:10:03','2026-06-01 17:52:51',0,18);
/*!40000 ALTER TABLE `pms_product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pms_product_review`
--

DROP TABLE IF EXISTS `pms_product_review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pms_product_review` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `rating` tinyint NOT NULL DEFAULT '5' COMMENT '评分(1-5)',
  `content` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '评价内容',
  `images` varchar(2000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '评价图片JSON数组',
  `reply_content` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商家回复',
  `reply_time` datetime DEFAULT NULL COMMENT '商家回复时间',
  `is_anonymous` tinyint DEFAULT '0' COMMENT '是否匿名: 0-否 1-是',
  `status` tinyint DEFAULT '1' COMMENT '状态: 0-隐藏 1-显示',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品评价表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pms_product_review`
--

LOCK TABLES `pms_product_review` WRITE;
/*!40000 ALTER TABLE `pms_product_review` DISABLE KEYS */;
INSERT INTO `pms_product_review` VALUES (1,1,24,1,5,'Very good product!',NULL,NULL,NULL,0,1,'2026-06-07 10:31:15','2026-06-07 10:31:15');
/*!40000 ALTER TABLE `pms_product_review` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pms_product_sku`
--

DROP TABLE IF EXISTS `pms_product_sku`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pms_product_sku` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'SKU ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU标题',
  `image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'SKU图片',
  `price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '价格',
  `stock` int NOT NULL DEFAULT '0' COMMENT '库存',
  `sales` int NOT NULL DEFAULT '0' COMMENT '销量',
  `specs` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'SKU规格属性，JSON格式',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品SKU表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pms_product_sku`
--

LOCK TABLES `pms_product_sku` WRITE;
/*!40000 ALTER TABLE `pms_product_sku` DISABLE KEYS */;
/*!40000 ALTER TABLE `pms_product_sku` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_banner`
--

DROP TABLE IF EXISTS `sys_banner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_banner` (
  `id` bigint NOT NULL,
  `title` varchar(128) NOT NULL COMMENT '标题',
  `image` varchar(500) NOT NULL COMMENT '图片URL',
  `url` varchar(500) DEFAULT NULL COMMENT '跳转链接',
  `position` tinyint DEFAULT '1' COMMENT '位置 1-首页 2-分类页',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Banner表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_banner`
--

LOCK TABLES `sys_banner` WRITE;
/*!40000 ALTER TABLE `sys_banner` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_banner` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_notification`
--

DROP TABLE IF EXISTS `sys_notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` tinyint NOT NULL COMMENT '通知类型: 1退款申请 2入驻审核 3内容审核 4库存预警',
  `title` varchar(128) NOT NULL COMMENT '通知标题',
  `content` varchar(500) DEFAULT NULL COMMENT '通知内容',
  `ref_id` bigint DEFAULT NULL COMMENT '关联业务ID',
  `ref_type` varchar(32) DEFAULT NULL COMMENT '关联业务类型: refund/store_apply/note_audit/stock',
  `is_read` tinyint NOT NULL DEFAULT '0' COMMENT '是否已读: 0未读 1已读',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_notification`
--

LOCK TABLES `sys_notification` WRITE;
/*!40000 ALTER TABLE `sys_notification` DISABLE KEYS */;
INSERT INTO `sys_notification` VALUES (1,2,'新的入驻申请','用户 lufe 申请入驻: 路飞周边小店',9,'store_apply',0,'2026-06-21 14:24:54','2026-06-21 14:58:57',0),(2,2,'新的入驻申请','用户 lufe 申请入驻: lufe的小店',9,'store_apply',0,'2026-06-21 14:40:57','2026-06-21 14:40:57',0);
/*!40000 ALTER TABLE `sys_notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ums_balance_log`
--

DROP TABLE IF EXISTS `ums_balance_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ums_balance_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `change_amount` decimal(10,2) NOT NULL COMMENT '变动金额（正=增加，负=减少）',
  `after_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '变动后余额',
  `type` tinyint NOT NULL COMMENT '类型: 1-充值 2-支付 3-退款 4-提现 5-管理员调整',
  `biz_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联业务ID',
  `pay_channel` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '支付渠道',
  `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='余额流水表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ums_balance_log`
--

LOCK TABLES `ums_balance_log` WRITE;
/*!40000 ALTER TABLE `ums_balance_log` DISABLE KEYS */;
INSERT INTO `ums_balance_log` VALUES (1,1,-2979.00,7020.99,2,'SN20260530135425922873',NULL,'余额支付订单','2026-05-30 13:54:35'),(2,1,2979.00,9999.99,3,'SN20260530135425922873',NULL,'用户申请退款','2026-05-30 13:55:15'),(3,1,-2999.00,7000.99,2,'SN20260607131609184356',NULL,'余额支付订单','2026-06-07 13:16:12'),(4,1,-2999.00,4001.99,2,'SN20260607141504934070',NULL,'余额支付订单','2026-06-07 14:15:09'),(5,1,2999.00,7000.99,3,'SN20260607141504934070',NULL,'用户申请退款','2026-06-08 14:35:22'),(6,1,-2999.00,4001.99,2,'SN20260608144444949982',NULL,'余额支付订单','2026-06-08 14:44:49'),(7,1,2999.00,7000.99,3,'SN20260608144444949982',NULL,'用户申请退款','2026-06-08 17:09:20'),(8,1,-2999.00,4001.99,2,'SN20260608171001562019',NULL,'余额支付订单','2026-06-08 17:10:09'),(9,1,2999.00,7000.99,3,'SN20260608171001562019',NULL,'用户申请退款','2026-06-08 17:35:34'),(10,1,-2999.00,4001.99,2,'SN20260608173829195592',NULL,'余额支付订单','2026-06-08 17:38:32');
/*!40000 ALTER TABLE `ums_balance_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ums_integral_log`
--

DROP TABLE IF EXISTS `ums_integral_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ums_integral_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `change_amount` int NOT NULL COMMENT '变动数量（正=增加，负=减少）',
  `after_amount` int NOT NULL DEFAULT '0' COMMENT '变动后余额',
  `type` tinyint NOT NULL COMMENT '类型: 1-注册赠送 2-邀请奖励 3-订单完成 4-积分兑换 5-积分支付 6-退款退回 7-管理员调整',
  `biz_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联业务ID（订单号/兑换单号等）',
  `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分流水表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ums_integral_log`
--

LOCK TABLES `ums_integral_log` WRITE;
/*!40000 ALTER TABLE `ums_integral_log` DISABLE KEYS */;
INSERT INTO `ums_integral_log` VALUES (1,1,71991,81991,3,'SN20260530113645288783','订单完成奖励','2026-05-30 11:41:13'),(2,1,139990,221981,3,'SN20260601180152859090','订单完成奖励','2026-06-01 18:03:52'),(3,1,139490,361471,3,'SN20260603165449563557','订单完成奖励','2026-06-03 17:01:20'),(4,1,29990,391461,3,'SN20260607131609184356','订单完成奖励','2026-06-07 14:08:57'),(5,1,29990,421451,3,'SN20260608173829195592','订单完成奖励','2026-06-08 20:47:15');
/*!40000 ALTER TABLE `ums_integral_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ums_user`
--

DROP TABLE IF EXISTS `ums_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ums_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '加密密码',
  `nickname` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
  `avatar` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '头像URL',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `gender` tinyint DEFAULT '0' COMMENT '性别: 0-未知 1-男 2-女',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
  `member_level` tinyint NOT NULL DEFAULT '1' COMMENT '会员等级: 1-普通 2-银卡 3-金卡 4-钻石',
  `integral` int NOT NULL DEFAULT '0' COMMENT '积分',
  `balance` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '账户余额',
  `growth_value` int NOT NULL DEFAULT '0' COMMENT '成长值',
  `openid_mp` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '微信小程序openid',
  `openid_app` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '微信APP openid',
  `unionid` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '微信unionid',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后登录IP',
  `referral_code` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `inviter_id` bigint DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
  `role` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'USER' COMMENT '角色: ADMIN/STORE/USER',
  `store_status` tinyint DEFAULT NULL COMMENT '店家审核状态: 0-待审核 1-已通过 2-已拒绝',
  `store_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '店铺名称',
  `store_logo` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '店铺Logo',
  `store_description` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '店铺简介',
  `store_apply_time` datetime DEFAULT NULL COMMENT '申请入驻时间',
  `store_audit_time` datetime DEFAULT NULL COMMENT '入驻审核时间',
  `store_reject_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '入驻拒绝原因',
  `coin_balance` int NOT NULL DEFAULT '0' COMMENT '虚拟币余额',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_openid_mp` (`openid_mp`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_role` (`role`),
  KEY `idx_store_status` (`store_status`),
  KEY `idx_referral_code` (`referral_code`),
  KEY `idx_inviter_id` (`inviter_id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户基础表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ums_user`
--

LOCK TABLES `ums_user` WRITE;
/*!40000 ALTER TABLE `ums_user` DISABLE KEYS */;
INSERT INTO `ums_user` VALUES (1,'admin','$2a$10$GdVAHiPpl.EkD1eSiw2i0eP7tuNBXmWVoWgrLcsHmrDl0w/K37TYS','管理员','data:image/jpeg;base64,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','13800138000','admin@shopmax.com',1,'2004-11-22',1,4,421451,4001.99,46145,NULL,NULL,NULL,'2026-06-21 16:05:32','10.244.57.153',NULL,NULL,'2026-04-15 15:10:47','2026-06-08 20:47:15',0,'ADMIN',NULL,NULL,NULL,NULL,NULL,NULL,NULL,0),(2,'user001','$2a$12$LRZHb9.frVmo20VdYyyAOuBIdyN2TkiUfqDAcwwWlawUuSqH7bDpC','张三',NULL,'13800138001','user001@test.com',1,NULL,1,1,100,0.00,50,NULL,NULL,NULL,'2026-04-22 20:03:27','0:0:0:0:0:0:0:1',NULL,NULL,'2026-04-15 15:10:47','2026-04-22 20:01:41',0,'USER',NULL,NULL,NULL,NULL,NULL,NULL,NULL,0),(3,'user002','$2a$12$/wIN4bTNb0S5Wun3r2zwvOHaD03gfiXhaEf5vvawB4ytUJCem61Aq','李四',NULL,'13800138002','user002@test.com',2,NULL,1,2,500,50.00,200,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-04-15 15:10:47','2026-04-22 20:02:15',0,'USER',NULL,NULL,NULL,NULL,NULL,NULL,NULL,0),(4,'user003','$2a$12$Ut1.S4XEH4nmmJAhcwFqyuuQprsF1Miq5EFZL8NhJFxBWYF3qVYR6','王五',NULL,'13800138003','user003@test.com',1,NULL,1,1,0,0.00,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-04-15 15:10:47','2026-04-22 20:02:27',0,'USER',NULL,NULL,NULL,NULL,NULL,NULL,NULL,0),(5,'testuser6','$2a$12$tKfip2Xa4xlTvRpNiHLglOqG17YXPduacKjJVRFPfoZQd.tGFe6Xu','TestUser',NULL,'13900139006','13900139006@qq.com',0,NULL,1,1,0,0.00,0,NULL,NULL,NULL,'2026-06-21 14:12:05','0:0:0:0:0:0:0:1',NULL,NULL,'2026-04-20 15:51:26','2026-04-22 20:02:36',0,'USER',0,'1','','','2026-06-21 14:12:14',NULL,NULL,0),(6,'kai','$2a$10$qdfbq1ouCYksxFsW/nZCR.//FhT1qd6R7bUsb3hHyilLZt0DtcjJS','kai',NULL,'13795058765','13795058765@qq.com',0,NULL,1,1,0,0.00,0,NULL,NULL,NULL,'2026-06-21 01:52:50','0:0:0:0:0:0:0:1',NULL,NULL,'2026-04-22 20:27:12','2026-04-22 20:27:12',0,'USER',2,'kai','','','2026-06-21 01:53:09','2026-06-21 14:09:49','暂不符合',0),(9,'lufe','$2a$10$8pQr5bOQzrNMXMJhcB3PvuE/GEzvdAGHY1VgZAn3TRzusnfSoFTgS','lufe',NULL,'13685259636','13685259636@qq.com',0,NULL,1,1,0,0.00,0,NULL,NULL,NULL,'2026-06-21 14:24:41','0:0:0:0:0:0:0:1',NULL,NULL,'2026-04-28 09:00:27','2026-04-28 09:00:27',0,'USER',0,'lufe的小店','','','2026-06-21 14:40:57','2026-06-21 14:40:29','1',0),(13,'ccc','$2a$10$lBCkrqVXo2SgIt5fi3OJzeIfeeNldXAplAkmGoiMwpDfEU08yEDKW','ccc',NULL,'13985259636','13985259636@qq.com',0,NULL,0,1,0,0.00,0,NULL,NULL,NULL,'2026-04-29 19:12:04','0:0:0:0:0:0:0:1',NULL,NULL,'2026-04-28 09:53:58','2026-04-28 09:53:58',0,'USER',NULL,NULL,NULL,NULL,NULL,NULL,NULL,0),(15,'aaa1','$2a$10$lAELIkFqdfJo4kOdyDwT/OG6PkTvoGust7LYZtpJn1gwuA7EhOxR6','aaa1',NULL,'13385259639','13385259639@qq.com',0,NULL,1,1,0,0.00,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-04-28 10:32:35','2026-04-28 10:32:53',1,'USER',NULL,NULL,NULL,NULL,NULL,NULL,NULL,0),(16,'kkk','$2a$10$RhNWEhhuQKkk1jOx25DvGu0imUi7qb4M2tdp4lGYb997I8GUVR3Xe','kkk','data:image/jpeg;base64,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','13525853696',NULL,0,NULL,1,1,0,0.00,0,NULL,NULL,NULL,'2026-06-21 01:51:44','0:0:0:0:0:0:0:1',NULL,NULL,'2026-05-13 19:02:23','2026-05-13 19:02:23',0,'STORE',1,'kkk的小摊','','小摊不大，应有尽有','2026-05-21 11:26:21','2026-05-21 11:26:50',NULL,0),(18,'user_7765','$2a$10$WRumGKVAsu.bB0NciCLjN.ryAS2uUY/MW7c0zEWp.OumOYxO.c48.','用户7765',NULL,'13794057765',NULL,0,NULL,1,1,0,0.00,0,NULL,NULL,NULL,'2026-06-15 14:22:12','0:0:0:0:0:0:0:1',NULL,NULL,'2026-05-26 18:42:21','2026-05-26 18:42:21',0,'STORE',1,'7765',NULL,'1','2026-05-26 18:42:50','2026-05-26 18:43:09',NULL,0),(19,'user_1773559695','$2a$10$Tp3lb49YcJv58loejDkC4.B1v/tyWumtvg/A0CAzIk0..1BOrags.','用户1773559695',NULL,NULL,'1773559695@qq.com',0,NULL,1,1,0,0.00,0,NULL,NULL,NULL,'2026-06-10 09:40:10','0:0:0:0:0:0:0:1','RF865207333',NULL,'2026-06-10 09:36:37','2026-06-10 09:36:37',0,'STORE',1,'111','','','2026-06-10 09:41:24','2026-06-10 09:41:37',NULL,0);
/*!40000 ALTER TABLE `ums_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ums_user_address`
--

DROP TABLE IF EXISTS `ums_user_address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ums_user_address` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `receiver_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人手机号',
  `province` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '省份',
  `province_code` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '省份编码',
  `city` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '城市',
  `city_code` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '城市编码',
  `district` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '区/县',
  `district_code` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '区/县编码',
  `detail_address` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '详细地址',
  `full_address` varchar(500) COLLATE utf8mb4_unicode_ci GENERATED ALWAYS AS (concat(`province`,`city`,`district`,`detail_address`)) STORED COMMENT '完整地址',
  `postal_code` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮编',
  `is_default` tinyint NOT NULL DEFAULT '0' COMMENT '是否默认: 0-否 1-是',
  `label` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签: 家/公司/学校等',
  `longitude` decimal(10,7) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10,7) DEFAULT NULL COMMENT '纬度',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_default` (`is_default`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收货地址表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ums_user_address`
--

LOCK TABLES `ums_user_address` WRITE;
/*!40000 ALTER TABLE `ums_user_address` DISABLE KEYS */;
INSERT INTO `ums_user_address` (`id`, `user_id`, `receiver_name`, `receiver_phone`, `province`, `province_code`, `city`, `city_code`, `district`, `district_code`, `detail_address`, `postal_code`, `is_default`, `label`, `longitude`, `latitude`, `create_time`, `update_time`, `deleted`) VALUES (1,2,'张三','13800138001','北京市','110000','北京市','110100','朝阳区','110105','建国路88号SOHO现代城A座1001室','100022',1,'公司',NULL,NULL,'2026-04-15 15:10:47','2026-04-15 15:10:47',0),(2,2,'张三','13800138001','北京市','110000','北京市','110100','海淀区','110108','中关村大街1号海龙大厦10层','100080',0,'家',NULL,NULL,'2026-04-15 15:10:47','2026-04-15 15:10:47',0),(3,3,'李四','13800138002','上海市','310000','上海市','310100','浦东新区','310115','陆家嘴环路1000号恒生银行大厦20楼','200120',1,'公司',NULL,NULL,'2026-04-15 15:10:47','2026-04-15 15:10:47',0),(4,3,'李四','13800138002','上海市','310000','上海市','310100','黄浦区','310101','南京东路100号','200002',0,'家',NULL,NULL,'2026-04-15 15:10:47','2026-04-15 15:10:47',0),(5,1,'kaitou','13794057765','广东省',NULL,'佛山市',NULL,'南海区',NULL,'狮山镇小塘长安路7号',NULL,1,NULL,NULL,NULL,'2026-05-28 22:11:47','2026-05-28 22:11:47',0),(6,18,'黄先生','13794057765','广东',NULL,'广州',NULL,'黄埔',NULL,'广东省广州市黄埔区汤臣一品6栋201',NULL,1,NULL,NULL,NULL,'2026-06-01 13:00:17','2026-06-01 13:00:17',0);
/*!40000 ALTER TABLE `ums_user_address` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-24 19:53:24
