-- MySQL dump 10.13  Distrib 5.6.15, for Win32 (x86)
--
-- Host: localhost    Database: maplebr90
-- ------------------------------------------------------
-- Server version	5.6.15-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT = @@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS = @@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION = @@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE = @@TIME_ZONE */;
/*!40103 SET TIME_ZONE = '+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS = @@UNIQUE_CHECKS, UNIQUE_CHECKS = 0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0 */;
/*!40101 SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES = @@SQL_NOTES, SQL_NOTES = 0 */;

GRANT ALL ON *.* to root@'%';
FLUSH PRIVILEGES;


DROP TABLE IF EXISTS `maple_var`;
CREATE TABLE `maple_var`
(
    `character_id` INTEGER UNSIGNED NOT NULL,
    `maple_key`    VARCHAR(50)      NOT NULL DEFAULT '',
    `value`        VARCHAR(100)     NOT NULL DEFAULT '',
    PRIMARY KEY (`character_id`, `maple_key`)
) ENGINE = InnoDB;
--
-- Table structure for table `accounts`
--

DROP TABLE IF EXISTS `accounts`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `accounts`
(
    `id`          int(11)             NOT NULL AUTO_INCREMENT,
    `name`        varchar(13)         NOT NULL DEFAULT '',
    `password`    varchar(128)        NOT NULL DEFAULT '',
    `salt`        varchar(256)                  DEFAULT NULL,
    `2ndpassword` varchar(134)                 DEFAULT NULL,
    `salt2`       varchar(256)                  DEFAULT NULL,
    `loggedin`    tinyint(1) unsigned NOT NULL DEFAULT '0',
    `lastlogin`   timestamp           NULL     DEFAULT NULL,
    `lastlogon`   timestamp           NULL     DEFAULT NULL,
    `createdat`   timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `birthday`    date                NOT NULL DEFAULT '0000-00-00',
    `banned`      tinyint(1)          NOT NULL DEFAULT '0',
    `banreason`   text,
    `gm`          tinyint(1)          NOT NULL DEFAULT '0',
    `email`       tinytext,
    `macs`        tinytext,
    `tempban`     timestamp           NOT NULL DEFAULT '0000-00-00 00:00:00',
    `greason`     tinyint(4) unsigned          DEFAULT NULL,
    `nxCredit`    int(11)                      DEFAULT NULL,
    `mPoints`     int(11)                      DEFAULT NULL,
    `gender`      tinyint(1) unsigned NOT NULL DEFAULT '0',
    `SessionIP`   varchar(64)                  DEFAULT NULL,
    `ipCreated`   varchar(64)                  DEFAULT NULL,
    `points`      int(11)             NOT NULL DEFAULT '0',
    `vpoints`     int(11)             NOT NULL DEFAULT '0',
    `sitelogged`  text,
    `webadmin`    int(1)                       DEFAULT '0',
    `nick`        varchar(20)                  DEFAULT NULL,
    `mute`        int(1)                       DEFAULT '0',
    `ip`          text,
    PRIMARY KEY (`id`),
    UNIQUE KEY `name` (`name`),
    KEY `ranking1` (`id`, `banned`, `gm`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 30113
  DEFAULT CHARSET = latin1
  ROW_FORMAT = DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `accounts`
--

LOCK TABLES `accounts` WRITE;
/*!40000 ALTER TABLE `accounts`
    DISABLE KEYS */;
INSERT INTO `accounts`
VALUES (30004, 'admin',
        '$10$jYVJnXepFhfzziC7ZBt5K.nxF4Sa95xq/eQj4MLJbvcfdOE5S0HFa',
        'ddc5f4f0dfeff1a572560ee436730eb11d64b7a7208400d45d7f49b2eb32919f', NULL, NULL, 0, '2020-06-17 19:01:54', '2020-06-17 16:47:02',
        '2011-11-25 16:08:40', '2011-01-01', 0, NULL, 0, '', '6C-71-D9-19-77-66', '0000-00-00 00:00:00', NULL, 995899,
        11994599, 0, '/192.168.137.1', NULL, 2, 0, '1466652818', 1, NULL, 0, NULL);
/*!40000 ALTER TABLE `accounts`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `achievements`
--

DROP TABLE IF EXISTS `achievements`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `achievements`
(
    `achievementid` int(9)  NOT NULL DEFAULT '0',
    `charid`        int(9)  NOT NULL DEFAULT '0',
    `accountid`     int(11) NOT NULL DEFAULT '0',
    PRIMARY KEY (`achievementid`, `charid`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `achievements`
--

LOCK TABLES `achievements` WRITE;
/*!40000 ALTER TABLE `achievements`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `achievements`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `alliances`
--

DROP TABLE IF EXISTS `alliances`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `alliances`
(
    `id`       int(11)      NOT NULL AUTO_INCREMENT,
    `name`     varchar(13)  NOT NULL,
    `leaderid` int(11)      NOT NULL,
    `guild1`   int(11)      NOT NULL,
    `guild2`   int(11)      NOT NULL,
    `guild3`   int(11)      NOT NULL DEFAULT '0',
    `guild4`   int(11)      NOT NULL DEFAULT '0',
    `guild5`   int(11)      NOT NULL DEFAULT '0',
    `rank1`    varchar(13)  NOT NULL DEFAULT 'Master',
    `rank2`    varchar(13)  NOT NULL DEFAULT 'Jr.Master',
    `rank3`    varchar(13)  NOT NULL DEFAULT 'Member',
    `rank4`    varchar(13)  NOT NULL DEFAULT 'Member',
    `rank5`    varchar(13)  NOT NULL DEFAULT 'Member',
    `capacity` int(11)      NOT NULL DEFAULT '2',
    `notice`   varchar(100) NOT NULL DEFAULT '',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `alliances`
--

LOCK TABLES `alliances` WRITE;
/*!40000 ALTER TABLE `alliances`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `alliances`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bbs_replies`
--

DROP TABLE IF EXISTS `bbs_replies`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bbs_replies`
(
    `replyid`   int(10) unsigned    NOT NULL AUTO_INCREMENT,
    `threadid`  int(10) unsigned    NOT NULL,
    `postercid` int(10) unsigned    NOT NULL,
    `timestamp` bigint(20) unsigned NOT NULL,
    `content`   varchar(26)         NOT NULL DEFAULT '',
    `guildid`   int(11)             NOT NULL DEFAULT '0',
    PRIMARY KEY (`replyid`)
) ENGINE = MyISAM
  AUTO_INCREMENT = 59
  DEFAULT CHARSET = latin1
  ROW_FORMAT = DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bbs_replies`
--

LOCK TABLES `bbs_replies` WRITE;
/*!40000 ALTER TABLE `bbs_replies`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `bbs_replies`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bbs_threads`
--

DROP TABLE IF EXISTS `bbs_threads`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bbs_threads`
(
    `threadid`      int(10) unsigned     NOT NULL AUTO_INCREMENT,
    `postercid`     int(10) unsigned     NOT NULL,
    `name`          varchar(26)          NOT NULL DEFAULT '',
    `timestamp`     bigint(20) unsigned  NOT NULL,
    `icon`          smallint(5) unsigned NOT NULL,
    `replycount`    smallint(5) unsigned NOT NULL DEFAULT '0',
    `startpost`     text                 NOT NULL,
    `guildid`       int(10) unsigned     NOT NULL,
    `localthreadid` int(10) unsigned     NOT NULL,
    PRIMARY KEY (`threadid`)
) ENGINE = MyISAM
  AUTO_INCREMENT = 222
  DEFAULT CHARSET = latin1
  ROW_FORMAT = DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bbs_threads`
--

LOCK TABLES `bbs_threads` WRITE;
/*!40000 ALTER TABLE `bbs_threads`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `bbs_threads`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `buddyentries`
--

DROP TABLE IF EXISTS `buddyentries`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `buddyentries`
(
    `id`        int(11)     NOT NULL AUTO_INCREMENT,
    `owner`     int(11)     NOT NULL,
    `buddyid`   int(11)     NOT NULL,
    `groupName` varchar(13) NOT NULL DEFAULT 'Default Group',
    PRIMARY KEY (`id`),
    KEY `owner` (`owner`),
    CONSTRAINT `buddyentries_ibfk_1` FOREIGN KEY (`owner`) REFERENCES `characters` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  AUTO_INCREMENT = 24516
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `buddyentries`
--

LOCK TABLES `buddyentries` WRITE;
/*!40000 ALTER TABLE `buddyentries`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `buddyentries`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cashshop_limit_sell`
--

DROP TABLE IF EXISTS `cashshop_limit_sell`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cashshop_limit_sell`
(
    `serial` int(11) NOT NULL,
    `amount` int(11) NOT NULL DEFAULT '0',
    PRIMARY KEY (`serial`)
) ENGINE = MyISAM
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cashshop_limit_sell`
--

LOCK TABLES `cashshop_limit_sell` WRITE;
/*!40000 ALTER TABLE `cashshop_limit_sell`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `cashshop_limit_sell`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cashshop_modified_items`
--

DROP TABLE IF EXISTS `cashshop_modified_items`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cashshop_modified_items`
(
    `serial`         int(11)    NOT NULL,
    `discount_price` int(11)    NOT NULL DEFAULT '-1',
    `mark`           tinyint(1) NOT NULL DEFAULT '-1',
    `showup`         tinyint(1) NOT NULL DEFAULT '0',
    `itemid`         int(11)    NOT NULL DEFAULT '0',
    `priority`       tinyint(3) NOT NULL DEFAULT '0',
    `package`        tinyint(1) NOT NULL DEFAULT '0',
    `period`         tinyint(3) NOT NULL DEFAULT '0',
    `gender`         tinyint(1) NOT NULL DEFAULT '0',
    `count`          tinyint(3) NOT NULL DEFAULT '0',
    `meso`           int(11)    NOT NULL DEFAULT '0',
    `unk_1`          tinyint(1) NOT NULL DEFAULT '0',
    `unk_2`          tinyint(1) NOT NULL DEFAULT '0',
    `unk_3`          tinyint(1) NOT NULL DEFAULT '0',
    `extra_flags`    int(11)    NOT NULL DEFAULT '0',
    PRIMARY KEY (`serial`)
) ENGINE = MyISAM
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cashshop_modified_items`
--

LOCK TABLES `cashshop_modified_items` WRITE;
/*!40000 ALTER TABLE `cashshop_modified_items`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `cashshop_modified_items`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `character_slots`
--

DROP TABLE IF EXISTS `character_slots`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `character_slots`
(
    `id`        int(11) NOT NULL AUTO_INCREMENT,
    `accid`     int(11) NOT NULL DEFAULT '0',
    `worldid`   int(11) NOT NULL DEFAULT '0',
    `charslots` int(11) NOT NULL DEFAULT '6',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 95
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `character_slots`
--

LOCK TABLES `character_slots` WRITE;
/*!40000 ALTER TABLE `character_slots`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `character_slots`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `characters`
--

DROP TABLE IF EXISTS `characters`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `characters`
(
    `id`                    int(11)             NOT NULL AUTO_INCREMENT,
    `accountid`             int(11)             NOT NULL DEFAULT '0',
    `world`                 tinyint(1)          NOT NULL DEFAULT '0',
    `name`                  varchar(13)         NOT NULL DEFAULT '',
    `level`                 int(3) unsigned     NOT NULL DEFAULT '0',
    `exp`                   int(11)             NOT NULL DEFAULT '0',
    `str`                   int(5)              NOT NULL DEFAULT '0',
    `dex`                   int(5) unsigned     NOT NULL DEFAULT '0',
    `luk`                   int(5)              NOT NULL DEFAULT '0',
    `int`                   int(5)              NOT NULL DEFAULT '0',
    `hp`                    int(5)              NOT NULL DEFAULT '0',
    `mp`                    int(5)              NOT NULL DEFAULT '0',
    `maxhp`                 int(5)              NOT NULL DEFAULT '0',
    `maxmp`                 int(5)              NOT NULL DEFAULT '0',
    `meso`                  int(11)             NOT NULL DEFAULT '0',
    `hpApUsed`              int(5)              NOT NULL DEFAULT '0',
    `mpApUsed`              int(5)              NOT NULL DEFAULT '0',
    `job`                   int(5)              NOT NULL DEFAULT '0',
    `skincolor`             tinyint(1)          NOT NULL DEFAULT '0',
    `gender`                tinyint(1)          NOT NULL DEFAULT '0',
    `fame`                  int(5)              NOT NULL DEFAULT '0',
    `hair`                  int(11)             NOT NULL DEFAULT '0',
    `face`                  int(11)             NOT NULL DEFAULT '0',
    `ap`                    int(5)              NOT NULL DEFAULT '0',
    `map`                   int(11)             NOT NULL DEFAULT '0',
    `spawnpoint`            int(3)              NOT NULL DEFAULT '0',
    `gm`                    int(3)              NOT NULL DEFAULT '0',
    `party`                 int(11)             NOT NULL DEFAULT '0',
    `buddyCapacity`         int(3)              NOT NULL DEFAULT '25',
    `createdate`            timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `guildid`               int(10) unsigned    NOT NULL DEFAULT '0',
    `guildrank`             tinyint(1) unsigned NOT NULL DEFAULT '5',
    `allianceRank`          tinyint(1) unsigned NOT NULL DEFAULT '5',
    `monsterbookcover`      int(11) unsigned    NOT NULL DEFAULT '0',
    `dojo_pts`              int(11) unsigned    NOT NULL DEFAULT '0',
    `dojoRecord`            tinyint(2) unsigned NOT NULL DEFAULT '0',
    `pets`                  varchar(13)         NOT NULL DEFAULT '-1,-1,-1',
    `subcategory`           int(11)             NOT NULL DEFAULT '0',
    `rank`                  int(11)             NOT NULL DEFAULT '1',
    `rankMove`              int(11)             NOT NULL DEFAULT '0',
    `jobRank`               int(11)             NOT NULL DEFAULT '1',
    `jobRankMove`           int(11)             NOT NULL DEFAULT '0',
    `marriageId`            int(11)             NOT NULL DEFAULT '0',
    `familyid`              int(11)             NOT NULL DEFAULT '0',
    `seniorid`              int(11)             NOT NULL DEFAULT '0',
    `junior1`               int(11)             NOT NULL DEFAULT '0',
    `junior2`               int(11)             NOT NULL DEFAULT '0',
    `currentrep`            int(11)             NOT NULL DEFAULT '0',
    `totalrep`              int(11)             NOT NULL DEFAULT '0',
    `occupationId`          int(5)              NOT NULL DEFAULT '0',
    `occupationEXP`         int(5)              NOT NULL DEFAULT '0',
    `reborns`               int(11)             NOT NULL DEFAULT '0',
    `jumplevel`             int(10) unsigned    NOT NULL DEFAULT '0',
    `jumpxp`                int(10) unsigned    NOT NULL DEFAULT '0',
    `factionId`             int(10) unsigned    NOT NULL DEFAULT '0',
    `gainedmsi`             int(10) unsigned    NOT NULL DEFAULT '0',
    `donatorpoints`         int(10) unsigned    NOT NULL DEFAULT '0',
    `playerAutoReborn`      int(10) unsigned    NOT NULL DEFAULT '0',
    `playerSuperDragonRoar` int(10) unsigned    NOT NULL DEFAULT '0',
    `agentpoints`           int(10) unsigned    NOT NULL DEFAULT '0',
    `contributedFP`         int(10) unsigned    NOT NULL DEFAULT '0',
    `sp`                    int(10) unsigned    NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `accountid` (`accountid`),
    KEY `party` (`party`),
    KEY `ranking1` (`level`, `exp`),
    KEY `ranking2` (`gm`, `job`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 277
  DEFAULT CHARSET = latin1
  ROW_FORMAT = DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `characters`
--

LOCK TABLES `characters` WRITE;
/*!40000 ALTER TABLE `characters`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `characters`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cheatlog`
--

DROP TABLE IF EXISTS `cheatlog`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cheatlog`
(
    `id`              int(11)   NOT NULL AUTO_INCREMENT,
    `characterid`     int(11)   NOT NULL DEFAULT '0',
    `offense`         tinytext  NOT NULL,
    `count`           int(11)   NOT NULL DEFAULT '0',
    `lastoffensetime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `param`           tinytext  NOT NULL,
    PRIMARY KEY (`id`),
    KEY `cid` (`characterid`)
) ENGINE = MyISAM
  DEFAULT CHARSET = latin1
  ROW_FORMAT = FIXED;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cheatlog`
--

LOCK TABLES `cheatlog` WRITE;
/*!40000 ALTER TABLE `cheatlog`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `cheatlog`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coupons`
--

DROP TABLE IF EXISTS `coupons`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `coupons`
(
    `id`        int(10) unsigned NOT NULL AUTO_INCREMENT,
    `code`      varchar(32)      NOT NULL,
    `used`      int(11)          NOT NULL DEFAULT '0',
    `character` varchar(13)               DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = MyISAM
  AUTO_INCREMENT = 2
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coupons`
--

LOCK TABLES `coupons` WRITE;
/*!40000 ALTER TABLE `coupons`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `coupons`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coupons_data`
--

DROP TABLE IF EXISTS `coupons_data`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `coupons_data`
(
    `id`       int(10) unsigned NOT NULL AUTO_INCREMENT,
    `code`     varchar(32)      NOT NULL,
    `type`     int(11)          NOT NULL,
    `itemData` int(11)          NOT NULL,
    `quantity` int(11)          NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = MyISAM
  AUTO_INCREMENT = 2
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coupons_data`
--

LOCK TABLES `coupons_data` WRITE;
/*!40000 ALTER TABLE `coupons_data`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `coupons_data`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `csequipment`
--

DROP TABLE IF EXISTS `csequipment`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `csequipment`
(
    `inventoryequipmentid` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `inventoryitemid`      int(10) unsigned NOT NULL DEFAULT '0',
    `upgradeslots`         int(11)          NOT NULL DEFAULT '0',
    `level`                int(11)          NOT NULL DEFAULT '0',
    `str`                  int(11)          NOT NULL DEFAULT '0',
    `dex`                  int(11)          NOT NULL DEFAULT '0',
    `int`                  int(11)          NOT NULL DEFAULT '0',
    `luk`                  int(11)          NOT NULL DEFAULT '0',
    `hp`                   int(11)          NOT NULL DEFAULT '0',
    `mp`                   int(11)          NOT NULL DEFAULT '0',
    `watk`                 int(11)          NOT NULL DEFAULT '0',
    `matk`                 int(11)          NOT NULL DEFAULT '0',
    `wdef`                 int(11)          NOT NULL DEFAULT '0',
    `mdef`                 int(11)          NOT NULL DEFAULT '0',
    `acc`                  int(11)          NOT NULL DEFAULT '0',
    `avoid`                int(11)          NOT NULL DEFAULT '0',
    `hands`                int(11)          NOT NULL DEFAULT '0',
    `speed`                int(11)          NOT NULL DEFAULT '0',
    `jump`                 int(11)          NOT NULL DEFAULT '0',
    `ViciousHammer`        tinyint(2)       NOT NULL DEFAULT '0',
    `itemEXP`              int(11)          NOT NULL DEFAULT '0',
    `durability`           int(11)          NOT NULL DEFAULT '-1',
    `enhance`              tinyint(3)       NOT NULL DEFAULT '0',
    `potential1`           smallint(5)      NOT NULL DEFAULT '0',
    `potential2`           smallint(5)      NOT NULL DEFAULT '0',
    `potential3`           smallint(5)      NOT NULL DEFAULT '0',
    `hpR`                  smallint(5)      NOT NULL DEFAULT '0',
    `mpR`                  smallint(5)      NOT NULL DEFAULT '0',
    PRIMARY KEY (`inventoryequipmentid`),
    KEY `inventoryitemid` (`inventoryitemid`),
    CONSTRAINT `csequiptment_ibfk_1` FOREIGN KEY (`inventoryitemid`) REFERENCES `csitems` (`inventoryitemid`) ON DELETE CASCADE
) ENGINE = InnoDB
  AUTO_INCREMENT = 5479
  DEFAULT CHARSET = latin1
  ROW_FORMAT = FIXED;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `csequipment`
--

LOCK TABLES `csequipment` WRITE;
/*!40000 ALTER TABLE `csequipment`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `csequipment`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `csitems`
--

DROP TABLE IF EXISTS `csitems`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `csitems`
(
    `inventoryitemid` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `characterid`     int(11)                   DEFAULT NULL,
    `accountid`       int(10)                   DEFAULT NULL,
    `packageid`       int(11)                   DEFAULT NULL,
    `itemid`          int(11)          NOT NULL DEFAULT '0',
    `inventorytype`   int(11)          NOT NULL DEFAULT '0',
    `position`        int(11)          NOT NULL DEFAULT '0',
    `quantity`        int(11)          NOT NULL DEFAULT '0',
    `owner`           tinytext,
    `GM_Log`          tinytext,
    `uniqueid`        int(11)          NOT NULL DEFAULT '-1',
    `flag`            int(2)           NOT NULL DEFAULT '0',
    `expiredate`      bigint(20)       NOT NULL DEFAULT '-1',
    `type`            tinyint(1)       NOT NULL DEFAULT '0',
    `sender`          varchar(13)      NOT NULL DEFAULT '',
    PRIMARY KEY (`inventoryitemid`),
    KEY `inventoryitems_ibfk_1` (`characterid`),
    KEY `characterid` (`characterid`),
    KEY `inventorytype` (`inventorytype`),
    KEY `accountid` (`accountid`),
    KEY `packageid` (`packageid`),
    KEY `characterid_2` (`characterid`, `inventorytype`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 11537
  DEFAULT CHARSET = latin1
  ROW_FORMAT = FIXED;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `csitems`
--

LOCK TABLES `csitems` WRITE;
/*!40000 ALTER TABLE `csitems`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `csitems`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customquestinfo`
--

DROP TABLE IF EXISTS `customquestinfo`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `customquestinfo`
(
    `id`           int(10) unsigned NOT NULL AUTO_INCREMENT,
    `questid`      int(11)          NOT NULL,
    `skillid`      int(11)          NOT NULL,
    `startReqs`    varchar(255)     NOT NULL DEFAULT '0,0,0',
    `completeReqs` varchar(255)     NOT NULL DEFAULT '0,0,0',
    `comments`     varchar(45)               DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = MyISAM
  AUTO_INCREMENT = 38
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customquestinfo`
--

LOCK TABLES `customquestinfo` WRITE;
/*!40000 ALTER TABLE `customquestinfo`
    DISABLE KEYS */;
INSERT INTO `customquestinfo`
VALUES (1, 190000, 0, '0,0,0', '0,0,0', 'Family Buff'),
       (2, 190001, 0, '0,0,0', '0,0,0', 'Family Buff'),
       (3, 190002, 0, '0,0,0', '0,0,0', 'Family Buff'),
       (4, 190003, 0, '0,0,0', '0,0,0', 'Family Buff'),
       (5, 190004, 0, '0,0,0', '0,0,0', 'Family Buff'),
       (6, 190005, 0, '0,0,0', '0,0,0', 'Family Buff'),
       (7, 190006, 0, '0,0,0', '0,0,0', 'Family Buff'),
       (8, 190007, 0, '0,0,0', '0,0,0', 'Family Buff'),
       (9, 190008, 0, '0,0,0', '0,0,0', 'Family Buff'),
       (10, 190009, 0, '0,0,0', '0,0,0', 'Family Buff'),
       (11, 190010, 0, '0,0,0', '0,0,0', 'Family Buff'),
       (12, 100000, 0, '0,0,0', '0,0,0', 'First Storyline quest, start at Agent P'),
       (13, 100001, 0, '0,0,0', '0,0,0', 'Start at OSSS Researcher, complete at Dr.Bing'),
       (14, 100002, 0, '0,0,0', 'mob,100100,30;item,4000019,15', 'Help Dr Bing hunt Snail and Snail shells'),
       (15, 100003, 0, '0,0,0', 'item,4001458,5', 'Get Crystanol Fragments from Reactors'),
       (16, 100004, 0, '0,0,0', '0,0,0', 'Start laboratory quest, someone looking.'),
       (17, 200000, 0, '0,0,0', '0,0,0', 'All storyline done check.'),
       (18, 150000, 0, '0,0,0', '0,0,0', 'Mulung Resting Map save'),
       (19, 170000, 0, '0,0,0', '0,0,0', 'Quick Slot Data'),
       (20, 170001, 0, '0,0,0', '0,0,0', 'Pet Ignore Tag'),
       (21, 170002, 0, '0,0,0', '0,0,0', 'Pet Auto HP'),
       (22, 170003, 0, '0,0,0', '0,0,0', 'Pet Auto MP'),
       (23, 170004, 0, '0,0,0', '0,0,0', 'Report'),
       (24, 170005, 0, '0,0,0', '0,0,0', 'Fixed Skills on KeyMap'),
       (25, 190011, 0, '0,0,0', '0,0,0', 'Speed Quiz'),
       (26, 100005, 0, '0,0,0', 'item,4032708,10', 'Deep sea\'s hunting mainboard'),
       (27, 100006, 0, '0,0,0', '0,0,0', 'Choose occupation.'),
       (28, 100007, 0, '0,0,0', 'item,4031752,20', 'Unblock 2nd portal and hunt TM part'),
       (29, 100008, 0, '0,0,0', 'item,4001459,1', 'Go to future after time machine fixed'),
       (31, 100009, 0, '0,0,0', '0,0,0', 'Find osss Boss'),
       (33, 190012, 0, '0,0,0', '0,0,0', '@clone command time log'),
       (34, 190013, 0, '0,0,0', '0,0,0', '@Bomb NX Whores'),
       (35, 190014, 0, '0,0,0', '0,0,0', '@Heal <name>'),
       (36, 190015, 0, '0,0,0', '0,0,0', '@Strip <name>'),
       (37, 190016, 0, '0,0,0', '0,0,0', '@Kill <name>');
/*!40000 ALTER TABLE `customquestinfo`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customquestmobs`
--

DROP TABLE IF EXISTS `customquestmobs`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `customquestmobs`
(
    `id`            int(10) unsigned NOT NULL AUTO_INCREMENT,
    `queststatusid` int(10) unsigned NOT NULL DEFAULT '0',
    `mob`           int(11)          NOT NULL DEFAULT '0',
    `count`         int(11)          NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `queststatusid` (`queststatusid`),
    CONSTRAINT `customQuestMobs_ibfk_1` FOREIGN KEY (`queststatusid`) REFERENCES `customqueststatus` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  ROW_FORMAT = DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customquestmobs`
--

LOCK TABLES `customquestmobs` WRITE;
/*!40000 ALTER TABLE `customquestmobs`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `customquestmobs`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customqueststatus`
--

DROP TABLE IF EXISTS `customqueststatus`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `customqueststatus`
(
    `id`          int(10) unsigned NOT NULL AUTO_INCREMENT,
    `characterid` int(11)          NOT NULL DEFAULT '0',
    `quest`       int(6)           NOT NULL DEFAULT '0',
    `status`      tinyint(4)       NOT NULL DEFAULT '0',
    `time`        int(11)          NOT NULL DEFAULT '0',
    `customData`  text,
    PRIMARY KEY (`id`),
    KEY `characterid` (`characterid`),
    CONSTRAINT `customQuestStatus_ibfk_1` FOREIGN KEY (`characterid`) REFERENCES `characters` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  AUTO_INCREMENT = 148465
  DEFAULT CHARSET = latin1
  ROW_FORMAT = FIXED;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customqueststatus`
--

LOCK TABLES `customqueststatus` WRITE;
/*!40000 ALTER TABLE `customqueststatus`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `customqueststatus`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `drop_data`
--

DROP TABLE IF EXISTS `drop_data`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `drop_data`
(
    `id`               bigint(20)       NOT NULL AUTO_INCREMENT,
    `dropperid`        int(11)          NOT NULL,
    `itemid`           int(11)          NOT NULL DEFAULT '0',
    `minimum_quantity` int(11)          NOT NULL DEFAULT '1',
    `maximum_quantity` int(11)          NOT NULL DEFAULT '1',
    `questid`          int(11)          NOT NULL DEFAULT '0',
    `chance`           int(11)          NOT NULL DEFAULT '0',
    `holdMaximum`      int(10) unsigned NOT NULL DEFAULT '99999',
    PRIMARY KEY (`id`),
    KEY `mobid` (`dropperid`)
) ENGINE = MyISAM
  AUTO_INCREMENT = 45345
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `evan_skillpoints`
--

DROP TABLE IF EXISTS `evan_skillpoints`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `evan_skillpoints`
(
    `id`          int(10) NOT NULL AUTO_INCREMENT,
    `characterid` int(10) NOT NULL,
    `evan1`       int(10) DEFAULT '0',
    `evan2`       int(10) DEFAULT '0',
    `evan3`       int(10) DEFAULT '0',
    `evan4`       int(10) DEFAULT '0',
    `evan5`       int(10) DEFAULT '0',
    `evan6`       int(10) DEFAULT '0',
    `evan7`       int(10) DEFAULT '0',
    `evan8`       int(10) DEFAULT '0',
    `evan9`       int(10) DEFAULT '0',
    `evan10`      int(10) DEFAULT '0',
    PRIMARY KEY (`id`)
) ENGINE = MyISAM
  AUTO_INCREMENT = 623
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `evan_skillpoints`
--

LOCK TABLES `evan_skillpoints` WRITE;
/*!40000 ALTER TABLE `evan_skillpoints`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `evan_skillpoints`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `famelog`
--

DROP TABLE IF EXISTS `famelog`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `famelog`
(
    `famelogid`      int(11)   NOT NULL AUTO_INCREMENT,
    `characterid`    int(11)   NOT NULL DEFAULT '0',
    `characterid_to` int(11)   NOT NULL DEFAULT '0',
    `when`           timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`famelogid`),
    KEY `characterid` (`characterid`),
    CONSTRAINT `famelog_ibfk_1` FOREIGN KEY (`characterid`) REFERENCES `characters` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  AUTO_INCREMENT = 68
  DEFAULT CHARSET = latin1
  ROW_FORMAT = DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `famelog`
--

LOCK TABLES `famelog` WRITE;
/*!40000 ALTER TABLE `famelog`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `famelog`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `families`
--

DROP TABLE IF EXISTS `families`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `families`
(
    `familyid` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `leaderid` int(10)          NOT NULL,
    `notice`   varchar(255) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
    PRIMARY KEY (`familyid`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 14
  DEFAULT CHARSET = latin1
  ROW_FORMAT = FIXED;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `families`
--

LOCK TABLES `families` WRITE;
/*!40000 ALTER TABLE `families`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `families`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `gifts`
--

DROP TABLE IF EXISTS `gifts`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `gifts`
(
    `giftid`    int(10) unsigned NOT NULL AUTO_INCREMENT,
    `recipient` int(11)          NOT NULL DEFAULT '0',
    `from`      varchar(13)      NOT NULL DEFAULT '',
    `message`   varchar(255)     NOT NULL DEFAULT '',
    `sn`        int(11)          NOT NULL DEFAULT '0',
    `uniqueid`  int(11)          NOT NULL DEFAULT '0',
    PRIMARY KEY (`giftid`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 14
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `gifts`
--

LOCK TABLES `gifts` WRITE;
/*!40000 ALTER TABLE `gifts`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `gifts`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `gmlog`
--

DROP TABLE IF EXISTS `gmlog`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `gmlog`
(
    `gmlogid` int(11)  NOT NULL AUTO_INCREMENT,
    `cid`     int(11)  NOT NULL DEFAULT '0',
    `command` tinytext NOT NULL,
    `mapid`   int(11)  NOT NULL DEFAULT '0',
    PRIMARY KEY (`gmlogid`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 4387
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `gmlog`
--

LOCK TABLES `gmlog` WRITE;
/*!40000 ALTER TABLE `gmlog`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `gmlog`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `guilds`
--

DROP TABLE IF EXISTS `guilds`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `guilds`
(
    `guildid`     int(10) unsigned     NOT NULL AUTO_INCREMENT,
    `leader`      int(10) unsigned     NOT NULL DEFAULT '0',
    `GP`          int(10) unsigned     NOT NULL DEFAULT '0',
    `logo`        int(10) unsigned              DEFAULT NULL,
    `logoColor`   smallint(5) unsigned NOT NULL DEFAULT '0',
    `name`        varchar(45)          NOT NULL,
    `rank1title`  varchar(45)          NOT NULL DEFAULT 'Master',
    `rank2title`  varchar(45)          NOT NULL DEFAULT 'Jr. Master',
    `rank3title`  varchar(45)          NOT NULL DEFAULT 'Member',
    `rank4title`  varchar(45)          NOT NULL DEFAULT 'Member',
    `rank5title`  varchar(45)          NOT NULL DEFAULT 'Member',
    `capacity`    int(10) unsigned     NOT NULL DEFAULT '10',
    `logoBG`      int(10) unsigned              DEFAULT NULL,
    `logoBGColor` smallint(5) unsigned NOT NULL DEFAULT '0',
    `notice`      varchar(101)                  DEFAULT NULL,
    `signature`   int(11)              NOT NULL DEFAULT '0',
    `alliance`    int(10) unsigned     NOT NULL DEFAULT '0',
    PRIMARY KEY (`guildid`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 14
  DEFAULT CHARSET = latin1
  ROW_FORMAT = FIXED;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `guilds`
--

LOCK TABLES `guilds` WRITE;
/*!40000 ALTER TABLE `guilds`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `guilds`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hiredmerch`
--

DROP TABLE IF EXISTS `hiredmerch`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hiredmerch`
(
    `PackageId`   int(10) unsigned NOT NULL AUTO_INCREMENT,
    `characterid` int(10) unsigned    DEFAULT '0',
    `accountid`   int(10) unsigned    DEFAULT NULL,
    `Mesos`       int(10) unsigned    DEFAULT '0',
    `time`        bigint(20) unsigned DEFAULT NULL,
    PRIMARY KEY (`PackageId`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 2
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hiredmerch`
--

LOCK TABLES `hiredmerch` WRITE;
/*!40000 ALTER TABLE `hiredmerch`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `hiredmerch`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hiredmerchequipment`
--

DROP TABLE IF EXISTS `hiredmerchequipment`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hiredmerchequipment`
(
    `inventoryequipmentid` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `inventoryitemid`      int(10) unsigned NOT NULL DEFAULT '0',
    `upgradeslots`         int(11)          NOT NULL DEFAULT '0',
    `level`                int(11)          NOT NULL DEFAULT '0',
    `str`                  int(11)          NOT NULL DEFAULT '0',
    `dex`                  int(11)          NOT NULL DEFAULT '0',
    `int`                  int(11)          NOT NULL DEFAULT '0',
    `luk`                  int(11)          NOT NULL DEFAULT '0',
    `hp`                   int(11)          NOT NULL DEFAULT '0',
    `mp`                   int(11)          NOT NULL DEFAULT '0',
    `watk`                 int(11)          NOT NULL DEFAULT '0',
    `matk`                 int(11)          NOT NULL DEFAULT '0',
    `wdef`                 int(11)          NOT NULL DEFAULT '0',
    `mdef`                 int(11)          NOT NULL DEFAULT '0',
    `acc`                  int(11)          NOT NULL DEFAULT '0',
    `avoid`                int(11)          NOT NULL DEFAULT '0',
    `hands`                int(11)          NOT NULL DEFAULT '0',
    `speed`                int(11)          NOT NULL DEFAULT '0',
    `jump`                 int(11)          NOT NULL DEFAULT '0',
    `ViciousHammer`        tinyint(2)       NOT NULL DEFAULT '0',
    `itemEXP`              int(11)          NOT NULL DEFAULT '0',
    `durability`           int(11)          NOT NULL DEFAULT '-1',
    `enhance`              tinyint(3)       NOT NULL DEFAULT '0',
    `potential1`           smallint(5)      NOT NULL DEFAULT '0',
    `potential2`           smallint(5)      NOT NULL DEFAULT '0',
    `potential3`           smallint(5)      NOT NULL DEFAULT '0',
    `hpR`                  smallint(5)      NOT NULL DEFAULT '0',
    `mpR`                  smallint(5)      NOT NULL DEFAULT '0',
    PRIMARY KEY (`inventoryequipmentid`),
    KEY `inventoryitemid` (`inventoryitemid`),
    CONSTRAINT `hiredmerchantequipment_ibfk_1` FOREIGN KEY (`inventoryitemid`) REFERENCES `hiredmerchitems` (`inventoryitemid`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  ROW_FORMAT = FIXED;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hiredmerchequipment`
--

LOCK TABLES `hiredmerchequipment` WRITE;
/*!40000 ALTER TABLE `hiredmerchequipment`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `hiredmerchequipment`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hiredmerchitems`
--

DROP TABLE IF EXISTS `hiredmerchitems`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hiredmerchitems`
(
    `inventoryitemid` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `characterid`     int(11)                   DEFAULT NULL,
    `accountid`       int(10)                   DEFAULT NULL,
    `packageid`       int(11)                   DEFAULT NULL,
    `itemid`          int(11)          NOT NULL DEFAULT '0',
    `inventorytype`   int(11)          NOT NULL DEFAULT '0',
    `position`        int(11)          NOT NULL DEFAULT '0',
    `quantity`        int(11)          NOT NULL DEFAULT '0',
    `owner`           tinytext,
    `GM_Log`          tinytext,
    `uniqueid`        int(11)          NOT NULL DEFAULT '-1',
    `flag`            int(2)           NOT NULL DEFAULT '0',
    `expiredate`      bigint(20)       NOT NULL DEFAULT '-1',
    `type`            tinyint(1)       NOT NULL DEFAULT '0',
    `sender`          varchar(13)      NOT NULL DEFAULT '',
    PRIMARY KEY (`inventoryitemid`),
    KEY `inventoryitems_ibfk_1` (`characterid`),
    KEY `characterid` (`characterid`),
    KEY `inventorytype` (`inventorytype`),
    KEY `accountid` (`accountid`),
    KEY `packageid` (`packageid`),
    KEY `characterid_2` (`characterid`, `inventorytype`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  ROW_FORMAT = FIXED;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hiredmerchitems`
--

LOCK TABLES `hiredmerchitems` WRITE;
/*!40000 ALTER TABLE `hiredmerchitems`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `hiredmerchitems`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventoryequipment`
--

DROP TABLE IF EXISTS `inventoryequipment`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inventoryequipment`
(
    `inventoryequipmentid` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `inventoryitemid`      int(10) unsigned NOT NULL DEFAULT '0',
    `upgradeslots`         int(11)          NOT NULL DEFAULT '0',
    `level`                int(11)          NOT NULL DEFAULT '0',
    `str`                  int(11)          NOT NULL DEFAULT '0',
    `dex`                  int(11)          NOT NULL DEFAULT '0',
    `int`                  int(11)          NOT NULL DEFAULT '0',
    `luk`                  int(11)          NOT NULL DEFAULT '0',
    `hp`                   int(11)          NOT NULL DEFAULT '0',
    `mp`                   int(11)          NOT NULL DEFAULT '0',
    `watk`                 int(11)          NOT NULL DEFAULT '0',
    `matk`                 int(11)          NOT NULL DEFAULT '0',
    `wdef`                 int(11)          NOT NULL DEFAULT '0',
    `mdef`                 int(11)          NOT NULL DEFAULT '0',
    `acc`                  int(11)          NOT NULL DEFAULT '0',
    `avoid`                int(11)          NOT NULL DEFAULT '0',
    `hands`                int(11)          NOT NULL DEFAULT '0',
    `speed`                int(11)          NOT NULL DEFAULT '0',
    `jump`                 int(11)          NOT NULL DEFAULT '0',
    `ViciousHammer`        tinyint(2)       NOT NULL DEFAULT '0',
    `itemEXP`              int(11)          NOT NULL DEFAULT '0',
    `durability`           int(11)          NOT NULL DEFAULT '-1',
    `enhance`              tinyint(3)       NOT NULL DEFAULT '0',
    `potential1`           smallint(5)      NOT NULL DEFAULT '0',
    `potential2`           smallint(5)      NOT NULL DEFAULT '0',
    `potential3`           smallint(5)      NOT NULL DEFAULT '0',
    `hpR`                  smallint(5)      NOT NULL DEFAULT '0',
    `mpR`                  smallint(5)      NOT NULL DEFAULT '0',
    PRIMARY KEY (`inventoryequipmentid`),
    KEY `inventoryitemid` (`inventoryitemid`),
    CONSTRAINT `inventoryequipment_ibfk_1` FOREIGN KEY (`inventoryitemid`) REFERENCES `inventoryitems` (`inventoryitemid`) ON DELETE CASCADE
) ENGINE = InnoDB
  AUTO_INCREMENT = 477574
  DEFAULT CHARSET = latin1
  ROW_FORMAT = FIXED;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventoryequipment`
--

LOCK TABLES `inventoryequipment` WRITE;
/*!40000 ALTER TABLE `inventoryequipment`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `inventoryequipment`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventoryitems`
--

DROP TABLE IF EXISTS `inventoryitems`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inventoryitems`
(
    `inventoryitemid` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `characterid`     int(11)                   DEFAULT NULL,
    `accountid`       int(10)                   DEFAULT NULL,
    `packageid`       int(11)                   DEFAULT NULL,
    `itemid`          int(11)          NOT NULL DEFAULT '0',
    `inventorytype`   int(11)          NOT NULL DEFAULT '0',
    `position`        int(11)          NOT NULL DEFAULT '0',
    `quantity`        int(11)          NOT NULL DEFAULT '0',
    `owner`           tinytext,
    `GM_Log`          tinytext,
    `uniqueid`        int(11)          NOT NULL DEFAULT '-1',
    `flag`            int(2)           NOT NULL DEFAULT '0',
    `expiredate`      bigint(20)       NOT NULL DEFAULT '-1',
    `type`            tinyint(1)       NOT NULL DEFAULT '0',
    `sender`          varchar(13)      NOT NULL DEFAULT '',
    PRIMARY KEY (`inventoryitemid`),
    KEY `inventoryitems_ibfk_1` (`characterid`),
    KEY `characterid` (`characterid`),
    KEY `inventorytype` (`inventorytype`),
    KEY `accountid` (`accountid`),
    KEY `packageid` (`packageid`),
    KEY `characterid_2` (`characterid`, `inventorytype`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1341877
  DEFAULT CHARSET = latin1
  ROW_FORMAT = FIXED;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventoryitems`
--

LOCK TABLES `inventoryitems` WRITE;
/*!40000 ALTER TABLE `inventoryitems`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `inventoryitems`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventoryslot`
--

DROP TABLE IF EXISTS `inventoryslot`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inventoryslot`
(
    `id`          int(10) unsigned NOT NULL AUTO_INCREMENT,
    `characterid` int(10) unsigned DEFAULT NULL,
    `equip`       int(10) unsigned DEFAULT NULL,
    `use`         int(10) unsigned DEFAULT NULL,
    `setup`       int(10) unsigned DEFAULT NULL,
    `etc`         int(10) unsigned DEFAULT NULL,
    `cash`        int(10) unsigned DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 11348
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventoryslot`
--

LOCK TABLES `inventoryslot` WRITE;
/*!40000 ALTER TABLE `inventoryslot`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `inventoryslot`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ipbans`
--

DROP TABLE IF EXISTS `ipbans`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ipbans`
(
    `ipbanid` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `ip`      varchar(40)      NOT NULL DEFAULT '',
    PRIMARY KEY (`ipbanid`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  ROW_FORMAT = FIXED;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ipbans`
--

LOCK TABLES `ipbans` WRITE;
/*!40000 ALTER TABLE `ipbans`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `ipbans`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ipvotelog`
--

DROP TABLE IF EXISTS `ipvotelog`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ipvotelog`
(
    `id`       int(11)     NOT NULL AUTO_INCREMENT,
    `ip`       varchar(30) NOT NULL,
    `accid`    int(11)     NOT NULL,
    `lastvote` int(11)     NOT NULL,
    `votetype` int(11)     NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ipvotelog`
--

LOCK TABLES `ipvotelog` WRITE;
/*!40000 ALTER TABLE `ipvotelog`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `ipvotelog`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `keymap`
--

DROP TABLE IF EXISTS `keymap`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `keymap`
(
    `id`          int(11)             NOT NULL AUTO_INCREMENT,
    `characterid` int(11)             NOT NULL DEFAULT '0',
    `key`         tinyint(3) unsigned NOT NULL DEFAULT '0',
    `type`        tinyint(3) unsigned NOT NULL DEFAULT '0',
    `action`      int(11)             NOT NULL DEFAULT '0',
    `fixed`       tinyint(3) unsigned NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `keymap_ibfk_1` (`characterid`),
    CONSTRAINT `keymap_ibfk_1` FOREIGN KEY (`characterid`) REFERENCES `characters` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  AUTO_INCREMENT = 418864
  DEFAULT CHARSET = latin1
  ROW_FORMAT = FIXED;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `keymap`
--

LOCK TABLES `keymap` WRITE;
/*!40000 ALTER TABLE `keymap`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `keymap`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `macbans`
--

DROP TABLE IF EXISTS `macbans`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `macbans`
(
    `macbanid` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `mac`      varchar(30)      NOT NULL,
    PRIMARY KEY (`macbanid`),
    UNIQUE KEY `mac_2` (`mac`)
) ENGINE = MEMORY
  DEFAULT CHARSET = latin1
  ROW_FORMAT = DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `macbans`
--

LOCK TABLES `macbans` WRITE;
/*!40000 ALTER TABLE `macbans`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `macbans`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `macfilters`
--

DROP TABLE IF EXISTS `macfilters`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `macfilters`
(
    `macfilterid` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `filter`      varchar(30)      NOT NULL,
    PRIMARY KEY (`macfilterid`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `macfilters`
--

LOCK TABLES `macfilters` WRITE;
/*!40000 ALTER TABLE `macfilters`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `macfilters`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `monsterbook`
--

DROP TABLE IF EXISTS `monsterbook`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `monsterbook`
(
    `id`     int(10)          NOT NULL AUTO_INCREMENT,
    `charid` int(10) unsigned NOT NULL DEFAULT '0',
    `cardid` int(10) unsigned NOT NULL DEFAULT '0',
    `level`  tinyint(2) unsigned       DEFAULT '1',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  ROW_FORMAT = FIXED;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `monsterbook`
--

LOCK TABLES `monsterbook` WRITE;
/*!40000 ALTER TABLE `monsterbook`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `monsterbook`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mountdata`
--

DROP TABLE IF EXISTS `mountdata`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mountdata`
(
    `id`          int(10) unsigned NOT NULL AUTO_INCREMENT,
    `characterid` int(10) unsigned          DEFAULT NULL,
    `Level`       int(3) unsigned  NOT NULL DEFAULT '0',
    `Exp`         int(10) unsigned NOT NULL DEFAULT '0',
    `Fatigue`     int(3) unsigned  NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`)
) ENGINE = MyISAM
  AUTO_INCREMENT = 277
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mountdata`
--

LOCK TABLES `mountdata` WRITE;
/*!40000 ALTER TABLE `mountdata`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `mountdata`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notes`
--

DROP TABLE IF EXISTS `notes`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `notes`
(
    `id`        int(11)             NOT NULL AUTO_INCREMENT,
    `to`        varchar(13)         NOT NULL DEFAULT '',
    `from`      varchar(13)         NOT NULL DEFAULT '',
    `message`   text                NOT NULL,
    `timestamp` bigint(20) unsigned NOT NULL,
    `gift`      tinyint(1)          NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 39
  DEFAULT CHARSET = latin1
  ROW_FORMAT = DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notes`
--

LOCK TABLES `notes` WRITE;
/*!40000 ALTER TABLE `notes`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `notes`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pets`
--

DROP TABLE IF EXISTS `pets`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pets`
(
    `petid`     int(10) unsigned NOT NULL AUTO_INCREMENT,
    `name`      varchar(13)               DEFAULT NULL,
    `level`     int(3) unsigned  NOT NULL,
    `closeness` int(6) unsigned  NOT NULL,
    `fullness`  int(3) unsigned  NOT NULL,
    `seconds`   int(11)          NOT NULL DEFAULT '0',
    `summoned`  tinyint(1)                DEFAULT NULL,
    PRIMARY KEY (`petid`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 32247
  DEFAULT CHARSET = latin1
  ROW_FORMAT = DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pets`
--

LOCK TABLES `pets` WRITE;
/*!40000 ALTER TABLE `pets`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `pets`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `playernpcs`
--

DROP TABLE IF EXISTS `playernpcs`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `playernpcs`
(
    `id`       int(11)     NOT NULL AUTO_INCREMENT,
    `name`     varchar(13) NOT NULL,
    `hair`     int(11)     NOT NULL,
    `face`     int(11)     NOT NULL,
    `skin`     int(11)     NOT NULL,
    `x`        int(11)     NOT NULL DEFAULT '0',
    `y`        int(11)     NOT NULL DEFAULT '0',
    `map`      int(11)     NOT NULL,
    `charid`   int(11)     NOT NULL,
    `scriptid` int(11)     NOT NULL,
    `foothold` int(11)     NOT NULL,
    `dir`      tinyint(1)  NOT NULL DEFAULT '0',
    `gender`   tinyint(1)  NOT NULL DEFAULT '0',
    `pets`     varchar(25)          DEFAULT '0,0,0',
    PRIMARY KEY (`id`),
    KEY `scriptid` (`scriptid`),
    KEY `playernpcs_ibfk_1` (`charid`),
    CONSTRAINT `playernpcs_ibfk_1` FOREIGN KEY (`charid`) REFERENCES `characters` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `playernpcs`
--

LOCK TABLES `playernpcs` WRITE;
/*!40000 ALTER TABLE `playernpcs`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `playernpcs`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `playernpcs_equip`
--

DROP TABLE IF EXISTS `playernpcs_equip`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `playernpcs_equip`
(
    `id`       int(11) NOT NULL AUTO_INCREMENT,
    `npcid`    int(11) NOT NULL,
    `equipid`  int(11) NOT NULL,
    `equippos` int(11) NOT NULL,
    `charid`   int(11) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `playernpcs_equip_ibfk_1` (`charid`),
    KEY `playernpcs_equip_ibfk_2` (`npcid`),
    CONSTRAINT `playernpcs_equip_ibfk_1` FOREIGN KEY (`charid`) REFERENCES `characters` (`id`) ON DELETE CASCADE,
    CONSTRAINT `playernpcs_equip_ibfk_2` FOREIGN KEY (`npcid`) REFERENCES `playernpcs` (`scriptid`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `playernpcs_equip`
--

LOCK TABLES `playernpcs_equip` WRITE;
/*!40000 ALTER TABLE `playernpcs_equip`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `playernpcs_equip`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `questinfo`
--

DROP TABLE IF EXISTS `questinfo`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `questinfo`
(
    `questinfoid` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `characterid` int(11)          NOT NULL DEFAULT '0',
    `quest`       int(6)           NOT NULL DEFAULT '0',
    `customData`  varchar(555)              DEFAULT NULL,
    PRIMARY KEY (`questinfoid`),
    KEY `characterid` (`characterid`),
    CONSTRAINT `questsinfo_ibfk_1` FOREIGN KEY (`characterid`) REFERENCES `characters` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  AUTO_INCREMENT = 9327
  DEFAULT CHARSET = latin1
  ROW_FORMAT = FIXED;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `questinfo`
--

LOCK TABLES `questinfo` WRITE;
/*!40000 ALTER TABLE `questinfo`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `questinfo`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `queststatus`
--

DROP TABLE IF EXISTS `queststatus`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `queststatus`
(
    `queststatusid` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `characterid`   int(11)          NOT NULL DEFAULT '0',
    `quest`         int(6)           NOT NULL DEFAULT '0',
    `status`        int(11)          NOT NULL DEFAULT '0',
    `time`          int(11)          NOT NULL DEFAULT '0',
    `forfeited`     int(11)          NOT NULL DEFAULT '0',
    `customData`    varchar(255)              DEFAULT NULL,
    PRIMARY KEY (`queststatusid`),
    KEY `characterid` (`characterid`),
    CONSTRAINT `queststatus_ibfk_1` FOREIGN KEY (`characterid`) REFERENCES `characters` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  AUTO_INCREMENT = 596211
  DEFAULT CHARSET = latin1
  ROW_FORMAT = FIXED;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `queststatus`
--

LOCK TABLES `queststatus` WRITE;
/*!40000 ALTER TABLE `queststatus`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `queststatus`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `queststatusmobs`
--

DROP TABLE IF EXISTS `queststatusmobs`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `queststatusmobs`
(
    `queststatusmobid` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `queststatusid`    int(10) unsigned NOT NULL DEFAULT '0',
    `mob`              int(11)          NOT NULL DEFAULT '0',
    `count`            int(11)          NOT NULL DEFAULT '0',
    PRIMARY KEY (`queststatusmobid`),
    KEY `queststatusid` (`queststatusid`),
    CONSTRAINT `queststatusmobs_ibfk_1` FOREIGN KEY (`queststatusid`) REFERENCES `queststatus` (`queststatusid`) ON DELETE CASCADE
) ENGINE = InnoDB
  AUTO_INCREMENT = 23560
  DEFAULT CHARSET = latin1
  ROW_FORMAT = DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `queststatusmobs`
--

LOCK TABLES `queststatusmobs` WRITE;
/*!40000 ALTER TABLE `queststatusmobs`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `queststatusmobs`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `regrocklocations`
--

DROP TABLE IF EXISTS `regrocklocations`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `regrocklocations`
(
    `trockid`     int(11) NOT NULL AUTO_INCREMENT,
    `characterid` int(11) DEFAULT NULL,
    `mapid`       int(11) DEFAULT NULL,
    PRIMARY KEY (`trockid`)
) ENGINE = MyISAM
  AUTO_INCREMENT = 12
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `regrocklocations`
--

LOCK TABLES `regrocklocations` WRITE;
/*!40000 ALTER TABLE `regrocklocations`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `regrocklocations`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reports`
--

DROP TABLE IF EXISTS `reports`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `reports`
(
    `reportid`    int(9)     NOT NULL AUTO_INCREMENT,
    `characterid` int(11)    NOT NULL DEFAULT '0',
    `type`        tinyint(2) NOT NULL DEFAULT '0',
    `count`       int(11)    NOT NULL DEFAULT '0',
    PRIMARY KEY (`reportid`, `characterid`),
    KEY `characterid` (`characterid`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reports`
--

LOCK TABLES `reports` WRITE;
/*!40000 ALTER TABLE `reports`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `reports`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rings`
--

DROP TABLE IF EXISTS `rings`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rings`
(
    `ringid`        int(11)      NOT NULL AUTO_INCREMENT,
    `partnerRingId` int(11)      NOT NULL DEFAULT '0',
    `partnerChrId`  int(11)      NOT NULL DEFAULT '0',
    `itemid`        int(11)      NOT NULL DEFAULT '0',
    `partnername`   varchar(255) NOT NULL,
    PRIMARY KEY (`ringid`)
) ENGINE = MyISAM
  AUTO_INCREMENT = 27529
  DEFAULT CHARSET = latin1
  ROW_FORMAT = FIXED;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rings`
--

LOCK TABLES `rings` WRITE;
/*!40000 ALTER TABLE `rings`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `rings`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `savedlocations`
--

DROP TABLE IF EXISTS `savedlocations`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `savedlocations`
(
    `id`           int(11) NOT NULL AUTO_INCREMENT,
    `characterid`  int(11) NOT NULL,
    `locationtype` int(11) NOT NULL DEFAULT '0',
    `map`          int(11) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `savedlocations_ibfk_1` (`characterid`),
    CONSTRAINT `savedlocations_ibfk_1` FOREIGN KEY (`characterid`) REFERENCES `characters` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  AUTO_INCREMENT = 3294
  DEFAULT CHARSET = latin1
  ROW_FORMAT = DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `savedlocations`
--

LOCK TABLES `savedlocations` WRITE;
/*!40000 ALTER TABLE `savedlocations`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `savedlocations`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `skillmacros`
--

DROP TABLE IF EXISTS `skillmacros`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `skillmacros`
(
    `id`          int(11)    NOT NULL AUTO_INCREMENT,
    `characterid` int(11)    NOT NULL DEFAULT '0',
    `position`    tinyint(1) NOT NULL DEFAULT '0',
    `skill1`      int(11)    NOT NULL DEFAULT '0',
    `skill2`      int(11)    NOT NULL DEFAULT '0',
    `skill3`      int(11)    NOT NULL DEFAULT '0',
    `name`        varchar(13)         DEFAULT NULL,
    `shout`       tinyint(1) NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`)
) ENGINE = MyISAM
  AUTO_INCREMENT = 241
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `skillmacros`
--

LOCK TABLES `skillmacros` WRITE;
/*!40000 ALTER TABLE `skillmacros`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `skillmacros`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `skills`
--

DROP TABLE IF EXISTS `skills`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `skills`
(
    `id`          int(11)    NOT NULL AUTO_INCREMENT,
    `skillid`     int(11)    NOT NULL DEFAULT '0',
    `characterid` int(11)    NOT NULL DEFAULT '0',
    `skilllevel`  tinyint(4) NOT NULL DEFAULT '0',
    `masterlevel` tinyint(4) NOT NULL DEFAULT '0',
    `expiration`  bigint(20) NOT NULL DEFAULT '-1',
    PRIMARY KEY (`id`),
    KEY `skills_ibfk_1` (`characterid`),
    CONSTRAINT `skills_ibfk_1` FOREIGN KEY (`characterid`) REFERENCES `characters` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1364192
  DEFAULT CHARSET = latin1
  ROW_FORMAT = DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `skills`
--

LOCK TABLES `skills` WRITE;
/*!40000 ALTER TABLE `skills`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `skills`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `skills_cooldowns`
--

DROP TABLE IF EXISTS `skills_cooldowns`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `skills_cooldowns`
(
    `id`        int(11)             NOT NULL AUTO_INCREMENT,
    `charid`    int(11)             NOT NULL,
    `SkillID`   int(11)             NOT NULL,
    `length`    bigint(20) unsigned NOT NULL,
    `StartTime` bigint(20) unsigned NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = MyISAM
  AUTO_INCREMENT = 93
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `skills_cooldowns`
--

LOCK TABLES `skills_cooldowns` WRITE;
/*!40000 ALTER TABLE `skills_cooldowns`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `skills_cooldowns`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `speedruns`
--

DROP TABLE IF EXISTS `speedruns`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `speedruns`
(
    `id`         int(11) unsigned NOT NULL AUTO_INCREMENT,
    `type`       varchar(13)      NOT NULL,
    `leader`     varchar(13)      NOT NULL,
    `timestring` varchar(1024)    NOT NULL,
    `time`       bigint(20)       NOT NULL DEFAULT '0',
    `members`    varchar(1024)    NOT NULL DEFAULT '',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 7
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `speedruns`
--

LOCK TABLES `speedruns` WRITE;
/*!40000 ALTER TABLE `speedruns`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `speedruns`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `storages`
--

DROP TABLE IF EXISTS `storages`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `storages`
(
    `storageid` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `accountid` int(11)          NOT NULL DEFAULT '0',
    `slots`     int(11)          NOT NULL DEFAULT '0',
    `meso`      int(11)          NOT NULL DEFAULT '0',
    PRIMARY KEY (`storageid`),
    KEY `accountid` (`accountid`),
    CONSTRAINT `storages_ibfk_1` FOREIGN KEY (`accountid`) REFERENCES `accounts` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  AUTO_INCREMENT = 93
  DEFAULT CHARSET = latin1
  ROW_FORMAT = FIXED;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `storages`
--

LOCK TABLES `storages` WRITE;
/*!40000 ALTER TABLE `storages`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `storages`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `trocklocations`
--

DROP TABLE IF EXISTS `trocklocations`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trocklocations`
(
    `trockid`     int(11) NOT NULL AUTO_INCREMENT,
    `characterid` int(11) DEFAULT NULL,
    `mapid`       int(11) DEFAULT NULL,
    PRIMARY KEY (`trockid`)
) ENGINE = MyISAM
  AUTO_INCREMENT = 324
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trocklocations`
--

LOCK TABLES `trocklocations` WRITE;
/*!40000 ALTER TABLE `trocklocations`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `trocklocations`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wishlist`
--

DROP TABLE IF EXISTS `wishlist`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `wishlist`
(
    `id`          int(11) NOT NULL AUTO_INCREMENT,
    `characterid` int(11) NOT NULL,
    `sn`          int(11) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wishlist`
--

LOCK TABLES `wishlist` WRITE;
/*!40000 ALTER TABLE `wishlist`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `wishlist`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wz_customlife`
--

DROP TABLE IF EXISTS `wz_customlife`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `wz_customlife`
(
    `id`      int(11)    NOT NULL AUTO_INCREMENT,
    `dataid`  int(11)    NOT NULL,
    `f`       int(11)    NOT NULL,
    `hide`    tinyint(1) NOT NULL DEFAULT '0',
    `fh`      int(11)    NOT NULL,
    `type`    varchar(1) NOT NULL,
    `cy`      int(11)    NOT NULL,
    `rx0`     int(11)    NOT NULL,
    `rx1`     int(11)    NOT NULL,
    `x`       int(11)    NOT NULL,
    `y`       int(11)    NOT NULL,
    `mobtime` int(11)             DEFAULT '1000',
    `mid`     int(11)    NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 4
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wz_customlife`
--

LOCK TABLES `wz_customlife` WRITE;
/*!40000 ALTER TABLE `wz_customlife`
    DISABLE KEYS */;
INSERT INTO `wz_customlife`
VALUES (1, 1012117, 0, 0, 115, 'n', -17, 362, 362, 362, -17, 1000, 910000000),
       (2, 2091006, 0, 0, 179, 'n', 51, 1334, 1334, 1334, 51, 1000, 250000000),
       (3, 2091009, 0, 0, 45, 'n', -218, 1209, 1209, 1209, -218, 1000, 250020300);
/*!40000 ALTER TABLE `wz_customlife`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wz_fixedskills`
--

DROP TABLE IF EXISTS `wz_fixedskills`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `wz_fixedskills`
(
    `id`          int(11)    NOT NULL AUTO_INCREMENT,
    `skillid`     int(11)    NOT NULL DEFAULT '0',
    `skilllevel`  tinyint(4) NOT NULL DEFAULT '0',
    `masterlevel` tinyint(4) NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 485
  DEFAULT CHARSET = latin1
  ROW_FORMAT = DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wz_fixedskills`
--

LOCK TABLES `wz_fixedskills` WRITE;
/*!40000 ALTER TABLE `wz_fixedskills`
    DISABLE KEYS */;
INSERT INTO `wz_fixedskills`
VALUES (1, 1000000, 16, 16),
       (2, 1000001, 10, 10),
       (3, 1000002, 8, 8),
       (4, 1001003, 20, 20),
       (5, 1001004, 20, 20),
       (6, 1001005, 20, 20),
       (7, 1100000, 20, 20),
       (8, 1100001, 20, 20),
       (9, 1100002, 30, 30),
       (10, 1100003, 30, 30),
       (11, 1101004, 20, 20),
       (12, 1101005, 20, 20),
       (13, 1101006, 20, 20),
       (14, 1101007, 30, 30),
       (15, 1110000, 20, 20),
       (16, 1110001, 20, 20),
       (17, 1111002, 30, 30),
       (18, 1111003, 30, 30),
       (19, 1111004, 30, 30),
       (20, 1111005, 30, 30),
       (21, 1111006, 30, 30),
       (22, 1111007, 20, 20),
       (23, 1111008, 30, 30),
       (24, 1120003, 30, 30),
       (25, 1120004, 30, 30),
       (26, 1120005, 30, 30),
       (27, 1121000, 30, 30),
       (28, 1121001, 30, 30),
       (29, 1121002, 30, 30),
       (30, 1121006, 30, 30),
       (31, 1121008, 30, 30),
       (32, 1121010, 30, 30),
       (33, 1121011, 5, 5),
       (34, 1200000, 20, 20),
       (35, 1200001, 20, 20),
       (36, 1200002, 30, 30),
       (37, 1200003, 30, 30),
       (38, 1201004, 20, 20),
       (39, 1201005, 20, 20),
       (40, 1201006, 20, 20),
       (41, 1201007, 30, 30),
       (42, 1210000, 20, 20),
       (43, 1210001, 20, 20),
       (44, 1211002, 30, 30),
       (45, 1211003, 30, 30),
       (46, 1211004, 30, 30),
       (47, 1211005, 30, 30),
       (48, 1211006, 30, 30),
       (49, 1211007, 30, 30),
       (50, 1211008, 30, 30),
       (51, 1211009, 20, 20),
       (52, 1220005, 30, 30),
       (53, 1220006, 30, 30),
       (54, 1220010, 10, 10),
       (55, 1221000, 30, 30),
       (56, 1221001, 30, 30),
       (57, 1221002, 30, 30),
       (58, 1221003, 20, 20),
       (59, 1221004, 20, 20),
       (60, 1221007, 30, 30),
       (61, 1221009, 30, 30),
       (62, 1221011, 30, 30),
       (63, 1221012, 5, 5),
       (64, 1300000, 20, 20),
       (65, 1300001, 20, 20),
       (66, 1300002, 30, 30),
       (67, 1300003, 30, 30),
       (68, 1301004, 20, 20),
       (69, 1301005, 20, 20),
       (70, 1301006, 20, 20),
       (71, 1301007, 30, 30),
       (72, 1310000, 20, 20),
       (73, 1311001, 30, 30),
       (74, 1311002, 30, 30),
       (75, 1311003, 30, 30),
       (76, 1311004, 30, 30),
       (77, 1311005, 30, 30),
       (78, 1311006, 30, 30),
       (79, 1311007, 20, 20),
       (80, 1311008, 20, 20),
       (81, 1320005, 30, 30),
       (82, 1320006, 30, 30),
       (83, 1320008, 25, 25),
       (84, 1320009, 25, 25),
       (85, 1321000, 30, 30),
       (86, 1321001, 30, 30),
       (87, 1321002, 30, 30),
       (88, 1321003, 30, 30),
       (89, 1321007, 10, 10),
       (90, 1321010, 5, 5),
       (91, 2000000, 16, 16),
       (92, 2000001, 10, 10),
       (93, 2001002, 20, 20),
       (94, 2001003, 20, 20),
       (95, 2001004, 20, 20),
       (96, 2001005, 20, 20),
       (97, 2100000, 20, 20),
       (98, 2101001, 20, 20),
       (99, 2101002, 20, 20),
       (100, 2101003, 20, 20),
       (101, 2101004, 30, 30),
       (102, 2101005, 30, 30),
       (103, 2110000, 20, 20),
       (104, 2110001, 30, 30),
       (105, 2111002, 30, 30),
       (106, 2111003, 30, 30),
       (107, 2111004, 20, 20),
       (108, 2111005, 20, 20),
       (109, 2111006, 30, 30),
       (110, 2121000, 30, 30),
       (111, 2121001, 30, 30),
       (112, 2121002, 30, 30),
       (113, 2121003, 30, 30),
       (114, 2121004, 30, 30),
       (115, 2121005, 30, 30),
       (116, 2121006, 30, 30),
       (117, 2121007, 30, 30),
       (118, 2121008, 5, 5),
       (119, 2200000, 20, 20),
       (120, 2201001, 20, 20),
       (121, 2201002, 20, 20),
       (122, 2201003, 20, 20),
       (123, 2201004, 30, 30),
       (124, 2201005, 30, 30),
       (125, 2210000, 20, 20),
       (126, 2210001, 30, 30),
       (127, 2211002, 30, 30),
       (128, 2211003, 30, 30),
       (129, 2211004, 20, 20),
       (130, 2211005, 20, 20),
       (131, 2211006, 30, 30),
       (132, 2221000, 30, 30),
       (133, 2221001, 30, 30),
       (134, 2221002, 30, 30),
       (135, 2221003, 30, 30),
       (136, 2221004, 30, 30),
       (137, 2221005, 30, 30),
       (138, 2221006, 30, 30),
       (139, 2221007, 30, 30),
       (140, 2221008, 5, 5),
       (141, 2300000, 20, 20),
       (142, 2301001, 20, 20),
       (143, 2301002, 30, 30),
       (144, 2301003, 20, 20),
       (145, 2301004, 20, 20),
       (146, 2301005, 30, 30),
       (147, 2310000, 20, 20),
       (148, 2311001, 20, 20),
       (149, 2311002, 20, 20),
       (150, 2311003, 30, 30),
       (151, 2311004, 30, 30),
       (152, 2311005, 30, 30),
       (153, 2311006, 30, 30),
       (154, 2321000, 30, 30),
       (155, 2321001, 30, 30),
       (156, 2321002, 30, 30),
       (157, 2321003, 30, 30),
       (158, 2321004, 30, 30),
       (159, 2321005, 30, 30),
       (160, 2321006, 10, 10),
       (161, 2321007, 30, 30),
       (162, 2321008, 30, 30),
       (163, 2321009, 5, 5),
       (164, 3000000, 16, 16),
       (165, 3000001, 20, 20),
       (166, 3000002, 8, 8),
       (167, 3001003, 20, 20),
       (168, 3001004, 20, 20),
       (169, 3001005, 20, 20),
       (170, 3100000, 20, 20),
       (171, 3100001, 30, 30),
       (172, 3101002, 20, 20),
       (173, 3101003, 20, 20),
       (174, 3101004, 20, 20),
       (175, 3101005, 30, 30),
       (176, 3110000, 20, 20),
       (177, 3110001, 20, 20),
       (178, 3111002, 20, 20),
       (179, 3111003, 30, 30),
       (180, 3111004, 30, 30),
       (181, 3111005, 30, 30),
       (182, 3111006, 30, 30),
       (183, 3120005, 30, 30),
       (184, 3121000, 30, 30),
       (185, 3121002, 30, 30),
       (186, 3121003, 30, 30),
       (187, 3121004, 30, 30),
       (188, 3121006, 30, 30),
       (189, 3121007, 30, 30),
       (190, 3121008, 30, 30),
       (191, 3121009, 5, 5),
       (192, 3200000, 20, 20),
       (193, 3200001, 30, 30),
       (194, 3201002, 20, 20),
       (195, 3201003, 20, 20),
       (196, 3201004, 20, 20),
       (197, 3201005, 30, 30),
       (198, 3210000, 20, 20),
       (199, 3210001, 20, 20),
       (200, 3211002, 20, 20),
       (201, 3211003, 30, 30),
       (202, 3211004, 30, 30),
       (203, 3211005, 30, 30),
       (204, 3211006, 30, 30),
       (205, 3220004, 30, 30),
       (206, 3221000, 30, 30),
       (207, 3221001, 30, 30),
       (208, 3221002, 30, 30),
       (209, 3221003, 30, 30),
       (210, 3221005, 30, 30),
       (211, 3221006, 30, 30),
       (212, 3221007, 30, 30),
       (213, 3221008, 5, 5),
       (214, 4000000, 20, 20),
       (215, 4000001, 8, 8),
       (216, 4001002, 20, 20),
       (217, 4001003, 20, 20),
       (218, 4001334, 20, 20),
       (219, 4001344, 20, 20),
       (220, 4100000, 20, 20),
       (221, 4100001, 30, 30),
       (222, 4100002, 20, 20),
       (223, 4101003, 20, 20),
       (224, 4101004, 20, 20),
       (225, 4101005, 30, 30),
       (226, 4110000, 20, 20),
       (227, 4111001, 20, 20),
       (228, 4111002, 30, 30),
       (229, 4111003, 20, 20),
       (230, 4111004, 30, 30),
       (231, 4111005, 30, 30),
       (232, 4111006, 20, 20),
       (233, 4120002, 30, 30),
       (234, 4120005, 30, 30),
       (235, 4121000, 30, 30),
       (236, 4121003, 30, 30),
       (237, 4121004, 30, 30),
       (238, 4121006, 30, 30),
       (239, 4121007, 30, 30),
       (240, 4121008, 30, 30),
       (241, 4121009, 5, 5),
       (242, 4200000, 20, 20),
       (243, 4200001, 20, 20),
       (244, 4201002, 20, 20),
       (245, 4201003, 20, 20),
       (246, 4201004, 30, 30),
       (247, 4201005, 30, 30),
       (248, 4210000, 20, 20),
       (249, 4211001, 30, 30),
       (250, 4211002, 30, 30),
       (251, 4211003, 20, 20),
       (252, 4211004, 30, 30),
       (253, 4211005, 20, 20),
       (254, 4211006, 30, 30),
       (255, 4220002, 30, 30),
       (256, 4220005, 30, 30),
       (257, 4221000, 30, 30),
       (258, 4221001, 30, 30),
       (259, 4221003, 30, 30),
       (260, 4221004, 30, 30),
       (261, 4221006, 30, 30),
       (262, 4221007, 30, 30),
       (263, 4221008, 5, 5),
       (264, 4300000, 20, 20),
       (265, 4301001, 10, 10),
       (266, 4301002, 20, 20),
       (267, 4310000, 20, 20),
       (268, 4311001, 20, 20),
       (269, 4311002, 20, 20),
       (270, 4311003, 20, 20),
       (271, 4321000, 20, 20),
       (272, 4321001, 20, 20),
       (273, 4321002, 20, 20),
       (274, 4321003, 20, 20),
       (275, 4330001, 20, 20),
       (276, 4331000, 10, 10),
       (277, 4331002, 30, 30),
       (278, 4331003, 20, 20),
       (279, 4331004, 20, 20),
       (280, 4331005, 20, 20),
       (281, 4340001, 30, 30),
       (282, 4341000, 30, 30),
       (283, 4341002, 30, 30),
       (284, 4341003, 30, 30),
       (285, 4341004, 30, 30),
       (286, 4341005, 30, 30),
       (287, 4341006, 30, 30),
       (288, 4341007, 30, 30),
       (289, 4341008, 5, 5),
       (290, 5000000, 20, 20),
       (291, 5001001, 20, 20),
       (292, 5001002, 20, 20),
       (293, 5001003, 20, 20),
       (294, 5001005, 10, 10),
       (295, 5100000, 10, 10),
       (296, 5100001, 20, 20),
       (297, 5101002, 20, 20),
       (298, 5101003, 20, 20),
       (299, 5101004, 20, 20),
       (300, 5101005, 10, 10),
       (301, 5101006, 20, 20),
       (302, 5101007, 10, 10),
       (303, 5110000, 20, 20),
       (304, 5110001, 40, 40),
       (305, 5111002, 30, 30),
       (306, 5111004, 20, 20),
       (307, 5111005, 20, 20),
       (308, 5111006, 30, 30),
       (309, 5121000, 30, 30),
       (310, 5121001, 30, 30),
       (311, 5121002, 30, 30),
       (312, 5121003, 20, 20),
       (313, 5121004, 30, 30),
       (314, 5121005, 30, 30),
       (315, 5121007, 30, 30),
       (316, 5121008, 5, 5),
       (317, 5121009, 20, 20),
       (318, 5121010, 30, 30),
       (319, 5200000, 20, 20),
       (320, 5201001, 20, 20),
       (321, 5201002, 20, 20),
       (322, 5201003, 20, 20),
       (323, 5201004, 20, 20),
       (324, 5201005, 10, 10),
       (325, 5201006, 20, 20),
       (326, 5210000, 20, 20),
       (327, 5211001, 30, 30),
       (328, 5211002, 30, 30),
       (329, 5211004, 30, 30),
       (330, 5211005, 30, 30),
       (331, 5211006, 30, 30),
       (332, 5220001, 30, 30),
       (333, 5220002, 20, 20),
       (334, 5220011, 20, 20),
       (335, 5221000, 30, 30),
       (336, 5221003, 30, 30),
       (337, 5221004, 30, 30),
       (338, 5221006, 10, 10),
       (339, 5221007, 30, 30),
       (340, 5221008, 30, 30),
       (341, 5221009, 20, 20),
       (342, 5221010, 5, 5),
       (343, 8001000, 1, 1),
       (344, 8001001, 1, 1),
       (345, 9001000, 1, 1),
       (346, 9001001, 1, 1),
       (347, 9001002, 1, 1),
       (348, 9101000, 1, 1),
       (349, 9101001, 1, 1),
       (350, 9101002, 1, 1),
       (351, 9101003, 1, 1),
       (352, 9101004, 1, 1),
       (353, 9101005, 1, 1),
       (354, 9101006, 1, 1),
       (355, 9101007, 1, 1),
       (356, 9101008, 1, 1),
       (357, 11000000, 10, 10),
       (358, 11001001, 10, 10),
       (359, 11001002, 20, 20),
       (360, 11001003, 20, 20),
       (361, 11001004, 20, 20),
       (362, 11100000, 20, 20),
       (363, 11101001, 20, 20),
       (364, 11101002, 30, 30),
       (365, 11101003, 20, 20),
       (366, 11101004, 30, 30),
       (367, 11101005, 10, 10),
       (368, 11110000, 20, 20),
       (369, 11110005, 20, 20),
       (370, 11111001, 20, 20),
       (371, 11111002, 20, 20),
       (372, 11111003, 20, 20),
       (373, 11111004, 30, 30),
       (374, 11111006, 30, 30),
       (375, 11111007, 20, 20),
       (376, 12000000, 10, 10),
       (377, 12001001, 10, 10),
       (378, 12001002, 10, 10),
       (379, 12001003, 20, 20),
       (380, 12001004, 20, 20),
       (381, 12101000, 20, 20),
       (382, 12101001, 20, 20),
       (383, 12101002, 20, 20),
       (384, 12101003, 20, 20),
       (385, 12101004, 20, 20),
       (386, 12101005, 20, 20),
       (387, 12101006, 20, 20),
       (388, 12110000, 20, 20),
       (389, 12110001, 20, 20),
       (390, 12111002, 20, 20),
       (391, 12111003, 20, 20),
       (392, 12111004, 20, 20),
       (393, 12111005, 30, 30),
       (394, 12111006, 30, 30),
       (395, 13000000, 20, 20),
       (396, 13000001, 8, 8),
       (397, 13001002, 10, 10),
       (398, 13001003, 20, 20),
       (399, 13001004, 20, 20),
       (400, 13100000, 20, 20),
       (401, 13100004, 20, 20),
       (402, 13101001, 20, 20),
       (403, 13101002, 30, 30),
       (404, 13101003, 20, 20),
       (405, 13101005, 20, 20),
       (406, 13101006, 10, 10),
       (407, 13110003, 20, 20),
       (408, 13111000, 20, 20),
       (409, 13111001, 30, 30),
       (410, 13111002, 20, 20),
       (411, 13111004, 20, 20),
       (412, 13111005, 10, 10),
       (413, 13111006, 20, 20),
       (414, 13111007, 20, 20),
       (415, 14000000, 10, 10),
       (416, 14000001, 8, 8),
       (417, 14001002, 10, 10),
       (418, 14001003, 10, 10),
       (419, 14001004, 20, 20),
       (420, 14001005, 20, 20),
       (421, 14100000, 20, 20),
       (422, 14100001, 30, 30),
       (423, 14100005, 10, 10),
       (424, 14101002, 20, 20),
       (425, 14101003, 20, 20),
       (426, 14101004, 20, 20),
       (427, 14101006, 20, 20),
       (428, 14110003, 20, 20),
       (429, 14110004, 20, 20),
       (430, 14111000, 30, 30),
       (431, 14111001, 20, 20),
       (432, 14111002, 30, 30),
       (433, 14111005, 20, 20),
       (434, 14111006, 30, 30),
       (435, 15000000, 10, 10),
       (436, 15001001, 20, 20),
       (437, 15001002, 20, 20),
       (438, 15001003, 10, 10),
       (439, 15001004, 20, 20),
       (440, 15100000, 10, 10),
       (441, 15100001, 20, 20),
       (442, 15100004, 20, 20),
       (443, 15101002, 20, 20),
       (444, 15101003, 20, 20),
       (445, 15101005, 20, 20),
       (446, 15101006, 20, 20),
       (447, 15110000, 20, 20),
       (448, 15111001, 20, 20),
       (449, 15111002, 10, 10),
       (450, 15111003, 20, 20),
       (451, 15111004, 20, 20),
       (452, 15111005, 20, 20),
       (453, 15111006, 20, 20),
       (454, 15111007, 30, 30),
       (455, 21000000, 10, 10),
       (456, 21000002, 20, 20),
       (457, 21001001, 15, 15),
       (458, 21001003, 20, 20),
       (459, 21100000, 20, 20),
       (460, 21100001, 20, 20),
       (461, 21100002, 30, 30),
       (462, 21100004, 20, 20),
       (463, 21100005, 20, 20),
       (464, 21101003, 20, 20),
       (465, 21110000, 20, 20),
       (466, 21110002, 20, 20),
       (467, 21110003, 30, 30),
       (468, 21110004, 30, 30),
       (469, 21110006, 20, 20),
       (470, 21110007, 20, 20),
       (471, 21110008, 20, 20),
       (472, 21111001, 20, 20),
       (473, 21111005, 20, 20),
       (474, 21120001, 30, 30),
       (475, 21120002, 30, 30),
       (476, 21120004, 30, 30),
       (477, 21120005, 30, 30),
       (478, 21120006, 30, 30),
       (479, 21120007, 30, 30),
       (480, 21120009, 30, 30),
       (481, 21120010, 30, 30),
       (482, 21121000, 30, 30),
       (483, 21121003, 30, 30),
       (484, 21121008, 5, 5);
/*!40000 ALTER TABLE `wz_fixedskills`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wz_oxdata`
--

DROP TABLE IF EXISTS `wz_oxdata`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `wz_oxdata`
(
    `questionset` smallint(6)    NOT NULL DEFAULT '0',
    `questionid`  smallint(6)    NOT NULL DEFAULT '0',
    `question`    varchar(200)   NOT NULL DEFAULT '',
    `display`     varchar(200)   NOT NULL DEFAULT '',
    `answer`      enum ('o','x') NOT NULL,
    PRIMARY KEY (`questionset`, `questionid`)
) ENGINE = MyISAM
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;



/*!40103 SET TIME_ZONE = @OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE = @OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS = @OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT = @OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS = @OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION = @OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES = @OLD_SQL_NOTES */;

-- Dump completed on 2020-06-18 13:40:38
