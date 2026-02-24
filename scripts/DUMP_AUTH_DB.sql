-- MySQL dump 10.13  Distrib 8.0.40, for Linux (x86_64)
--
-- Host: localhost    Database: auth
-- ------------------------------------------------------
-- Server version	8.0.40

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
-- Table structure for table `hibernate_sequence`
--

DROP TABLE IF EXISTS `hibernate_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hibernate_sequence` (
                                      `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hibernate_sequence`
--

LOCK TABLES `hibernate_sequence` WRITE;
/*!40000 ALTER TABLE `hibernate_sequence` DISABLE KEYS */;
INSERT INTO `hibernate_sequence` VALUES (29);
/*!40000 ALTER TABLE `hibernate_sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `PERMISSION`
--

DROP TABLE IF EXISTS `PERMISSION`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `PERMISSION` (
                              `id` bigint NOT NULL AUTO_INCREMENT,
                              `name` varchar(100) NOT NULL,
                              `description` varchar(255) DEFAULT NULL,
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=267 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `PERMISSION`
--

LOCK TABLES `PERMISSION` WRITE;
/*!40000 ALTER TABLE `PERMISSION` DISABLE KEYS */;
INSERT INTO `PERMISSION` VALUES (181,'users.view','Consente laccesso alla gestione dei dipendenti'),(182,'roles.view','Consente laccesso alla gestione dei ruoli'),(183,'ordini_clienti.view','Permette la visualizzazione degli ordini clienti'),(184,'ordini_clienti.processare','Permette di accedere alla sezione Da processare degli ordini clienti'),(185,'ordini_fornitori.view','Permette la visualizzazione e gestione degli ordini fornitori'),(186,'analisi_dati.view','Permette laccesso alla sezione Analisi dati'),(187,'analisi_dati.situazione_ordini','Mostra la pagina Situazione Ordini (monitor fornitore)'),(188,'contabilita.view','Consente laccesso al modulo di contabilit'),(213,'ordini_clienti.pronto_consegna','Visualizza e usa filtro Pronto Consegna'),(214,'ordini_clienti.filtro_venditore','Visualizza e usa filtro Venditore'),(215,'ordini_clienti.edit_stato','Modifica stato ordine cliente'),(216,'ordini_clienti.sblocca','Sblocca ordine cliente'),(217,'ordini_clienti.download','Scarica documentazione ordine cliente'),(218,'ordini_clienti.firma_cliente','Gestisce la firma ordine cliente'),(219,'ordini_clienti.invia_email','Invia email ordine cliente'),(220,'ordini_clienti.riapri','Riapre ordini clienti non processati'),(221,'ordini_clienti.warn_non_firmato','Visualizza warning ordine non firmato'),(222,'ordini_clienti.note_logistica','Gestione note logistica ordine cliente'),(223,'login.redirect.amministrativo','Redirect iniziale login  amministrativo'),(224,'login.redirect.logistica','Redirect iniziale login  logistica'),(225,'login.redirect.default','Redirect iniziale login  default'),(231,'oaf.view','Permette di visualizzare il dettaglio degli ordini a fornitore'),(232,'oaf.edit','Permette di modificare il dettaglio degli ordini a fornitore'),(233,'ordini_fornitori.unisci','Permette di unire ordini sospesi'),(234,'ordini_fornitori.flInviato.edit','Permette di modificare il flag flInviato'),(235,'ordini_fornitori.riapri','Permette di riprire un ordine fornitore'),(236,'ordini_fornitori.elimina','Permette di eliminare un ordine fornitore'),(237,'ordini_fornitori.salva','Permette di salvare la lista degli ordini fornitore'),(238,'oaf.override_status','Modifica OAF indipendentemente dallo status'),(239,'logistica.update_stato','Aggiornare lo stato degli ordini di consegna'),(240,'logistica.update_veicolo','Aggiornare veicolo consegna'),(241,'logistica.cerca_bolle','Ricerca bolle'),(242,'logistica.default_completo','Imposta stato di default = COMPLETO nella pagina logistica ordini'),(243,'ordine.bolla.seleziona','Permette di selezionare articoli nella lista bolla'),(244,'articoli.crea_ordine_fornitore','Permette di creare ordini a fornitore dagli articoli dellordine cliente'),(245,'articoli.carica_magazzino','Permette di effettuare il carico di magazzino degli articoli selezionati'),(246,'articoli.codifica_articoli','Permette di eseguire la codifica degli articoli speciali (*PZ, *MQ, *ML)'),(247,'articoli.crea_fattura_acconto','Permette di creare fatture di acconto per gli articoli selezionati'),(248,'articoli.filtro_non_disponibile','Permette di visualizzare e usare il filtro Non disponibile'),(249,'articoli.filtro_consegna_admin','Esclude la visualizzazione del filtro consegna per utenti amministrativi'),(250,'articoli.default_filtro_non_disponibile','Applica automaticamente il filtro Non Disponibile in stato DA_ORDINARE'),(251,'articoli.edit_descrizione','Permette di modificare la descrizione articolo'),(252,'articoli.edit_codice_fornitore','Permette di modificare il codice fornitore dellarticolo'),(253,'articoli.edit_quantita','Permette di modificare la quantit dellarticolo'),(254,'articoli.edit_tono','Permette di modificare il tono dellarticolo'),(255,'articoli.edit_qta_riservata','Permette di modificare la quantit riservata'),(256,'articoli.edit_qta_pronto_consegna','Permette di modificare la quantit pronto consegna'),(257,'articoli.edit_non_disponibile','Permette di modificare il flag Non Disponibile'),(258,'articoli.edit_flag_ordinato','Permette di modificare il flag Ordinato per tutti gli articoli'),(259,'articoli.edit_flag_ordinato_943','Permette di modificare il flag Ordinato solo per articoli con codice che inizia per 943'),(260,'articoli.edit_flag_pronto_consegna','Permette di modificare il flag Pronto Consegna'),(261,'articoli.edit_flag_consegnato','Permette di modificare il flag Consegnato'),(262,'articoli.edit_qta_senza_bolla','Permette di modificare la quantit consegnata senza bolla'),(263,'articoli.associa_fornitore','Permette di associare un fornitore a un articolo privo di codifica'),(264,'articoli.view_note_oaf','Permette di visualizzare le note dellordine a fornitore'),(265,'articoli.salva','Permette di salvare le modifiche agli articoli'),(266,'articoli.chiudi','Permette di chiudere lordine cliente dalla pagina articoli');
/*!40000 ALTER TABLE `PERMISSION` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ROLE`
--

DROP TABLE IF EXISTS `ROLE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ROLE` (
                        `id` bigint NOT NULL,
                        `name` varchar(255) NOT NULL,
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `UK_9glod3qre7ighyp4ci4t6fcoy` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ROLE`
--

LOCK TABLES `ROLE` WRITE;
/*!40000 ALTER TABLE `ROLE` DISABLE KEYS */;
INSERT INTO `ROLE` VALUES (1,'Admin'),(5,'Amministrativo'),(6,'Logistica'),(3,'Magazziniere'),(2,'User'),(4,'Venditore');
/*!40000 ALTER TABLE `ROLE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ROLE_PERMISSION`
--

DROP TABLE IF EXISTS `ROLE_PERMISSION`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ROLE_PERMISSION` (
                                   `role_id` bigint NOT NULL,
                                   `permission_id` bigint NOT NULL,
                                   PRIMARY KEY (`role_id`,`permission_id`),
                                   KEY `fk_role_perm_perm` (`permission_id`),
                                   CONSTRAINT `fk_role_perm_perm` FOREIGN KEY (`permission_id`) REFERENCES `PERMISSION` (`id`),
                                   CONSTRAINT `fk_role_perm_role` FOREIGN KEY (`role_id`) REFERENCES `ROLE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ROLE_PERMISSION`
--

LOCK TABLES `ROLE_PERMISSION` WRITE;
/*!40000 ALTER TABLE `ROLE_PERMISSION` DISABLE KEYS */;
INSERT INTO `ROLE_PERMISSION` VALUES (1,181),(1,182),(1,183),(3,183),(4,183),(5,183),(1,184),(4,184),(1,185),(3,185),(5,185),(6,185),(1,186),(4,186),(6,186),(1,187),(1,188),(1,213),(6,213),(1,214),(4,214),(1,215),(1,216),(1,217),(4,217),(5,217),(1,218),(4,218),(1,219),(4,219),(1,220),(3,220),(5,220),(1,221),(4,221),(1,222),(3,222),(4,222),(5,222),(1,223),(5,223),(1,224),(6,224),(1,225),(3,225),(4,225),(1,231),(3,231),(4,231),(6,231),(1,232),(5,232),(1,233),(5,233),(1,234),(5,234),(1,235),(5,235),(1,236),(5,236),(1,237),(5,237),(1,238),(1,239),(1,240),(4,240),(6,240),(1,241),(4,241),(1,242),(6,242),(1,243),(6,243),(1,244),(5,244),(1,245),(5,245),(1,246),(5,246),(1,247),(5,247),(1,248),(5,248),(1,249),(1,250),(5,250),(1,251),(5,251),(1,252),(5,252),(1,253),(5,253),(1,254),(5,254),(1,255),(5,255),(1,256),(5,256),(1,257),(5,257),(1,258),(5,258),(1,259),(1,260),(5,260),(1,261),(1,262),(1,263),(5,263),(1,264),(4,264),(5,264),(6,264),(1,265),(5,265),(1,266),(5,266);
/*!40000 ALTER TABLE `ROLE_PERMISSION` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role_sequence`
--

DROP TABLE IF EXISTS `role_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role_sequence` (
                                 `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role_sequence`
--

LOCK TABLES `role_sequence` WRITE;
/*!40000 ALTER TABLE `role_sequence` DISABLE KEYS */;
/*!40000 ALTER TABLE `role_sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `USER`
--

DROP TABLE IF EXISTS `USER`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `USER` (
                        `id` bigint NOT NULL,
                        `codVenditore` varchar(3) DEFAULT NULL,
                        `dataNascita` datetime(6) DEFAULT NULL,
                        `email` varchar(255) DEFAULT NULL,
                        `lastname` varchar(255) NOT NULL,
                        `name` varchar(255) NOT NULL,
                        `password` varchar(255) NOT NULL,
                        `username` varchar(255) NOT NULL,
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `UK_pc8a3iwr0i5534lmd6b1jdg1w` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `USER`
--

LOCK TABLES `USER` WRITE;
/*!40000 ALTER TABLE `USER` DISABLE KEYS */;
INSERT INTO `USER` VALUES (1,NULL,NULL,'paolo.convertini@gmail.com','convertini','paolo','nSWipKvnLiiYxAnuFusj9A==','pconvertini'),(2,'11',NULL,'damiano@calolenocifrancesco.it','calolenoci','damiano','W54q1QK2gVP+cG6Hc4P4kQ==','dcalolenoci'),(3,NULL,NULL,'','amministrazione','franco','nSWipKvnLiiYxAnuFusj9A==','franco'),(4,NULL,NULL,'','magazzino','francesco','Z85uL5v6qcScKnK0mL3v+Q==','francesco'),(5,'01',NULL,'piero@calolenocifrancesco.it','calolenoci','piero','W54q1QK2gVP+cG6Hc4P4kQ==','pcalolenoci'),(6,NULL,NULL,'','logistica','daniele','nSWipKvnLiiYxAnuFusj9A==','daniele'),(7,'03',NULL,'piero@calolenocifrancesco.it','scialpi','lorenzo','W54q1QK2gVP+cG6Hc4P4kQ==','lscialpi'),(8,'08',NULL,'antonianna@calolenocifrancesco.it','fumarola','antonianna','NybY1fVDNymnufVCOW8D2Q==','afumarola'),(9,'10',NULL,'roccocalolenoci@gmail.com','calolenoci','rocco','W54q1QK2gVP+cG6Hc4P4kQ==','rcalolenoci'),(10,'13',NULL,'calolenoci.fra@gmail.com','devincienti','francesco','nSWipKvnLiiYxAnuFusj9A==','fdevincienti'),(11,'15',NULL,'','semeraro','angela','nSWipKvnLiiYxAnuFusj9A==','asemeraro'),(12,'16',NULL,'info@calolenocifrancesco.it','a','mariagrazia','NybY1fVDNymnufVCOW8D2Q==','mariagrazia'),(13,NULL,NULL,NULL,'zermo','nico','NybY1fVDNymnufVCOW8D2Q==','nico'),(14,NULL,NULL,NULL,'petrosino','giorgio','nSWipKvnLiiYxAnuFusj9A==','giorgio'),(15,NULL,NULL,NULL,'Ugienti','Antonio','nSWipKvnLiiYxAnuFusj9A==','antonio'),(16,NULL,NULL,NULL,'francesco','Giancola','nSWipKvnLiiYxAnuFusj9A==','giancola'),(17,NULL,NULL,NULL,'Cofano','Anna','nSWipKvnLiiYxAnuFusj9A==','anna'),(18,NULL,NULL,NULL,'magazzino','alessandro','nSWipKvnLiiYxAnuFusj9A==','alessandro'),(20,NULL,NULL,NULL,'iero','p','NybY1fVDNymnufVCOW8D2Q==','piero'),(22,NULL,NULL,NULL,'rancobollo','franco','nSWipKvnLiiYxAnuFusj9A==','francobollo'),(23,'14',NULL,'test@email.it','siccardi','rosanna','NybY1fVDNymnufVCOW8D2Q==','rosanna'),(24,'17',NULL,'test@email.it','ugenti','francesco','NybY1fVDNymnufVCOW8D2Q==','fugenti'),(25,'20',NULL,'rosanna@calolenocifrancesco.it','BIASCO','Alessandra','NybY1fVDNymnufVCOW8D2Q==','ABIASCO'),(26,'18',NULL,NULL,'Scatigna','Madia','NybY1fVDNymnufVCOW8D2Q==','MScatigna'),(27,'21A',NULL,NULL,'Rubino','vitania','NybY1fVDNymnufVCOW8D2Q==','vRubino');
/*!40000 ALTER TABLE `USER` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `USER_ROLE`
--

DROP TABLE IF EXISTS `USER_ROLE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `USER_ROLE` (
                             `user_id` bigint NOT NULL,
                             `role_id` bigint NOT NULL,
                             PRIMARY KEY (`role_id`,`user_id`),
                             KEY `FKsn0101oegd6lsfnumn271ch3i` (`user_id`),
                             CONSTRAINT `FK52veh3j2tgvkrogjw2i0tucac` FOREIGN KEY (`role_id`) REFERENCES `ROLE` (`id`),
                             CONSTRAINT `FKsn0101oegd6lsfnumn271ch3i` FOREIGN KEY (`user_id`) REFERENCES `USER` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `USER_ROLE`
--

LOCK TABLES `USER_ROLE` WRITE;
/*!40000 ALTER TABLE `USER_ROLE` DISABLE KEYS */;
INSERT INTO `USER_ROLE` VALUES (1,1),(2,2),(2,4),(3,2),(3,5),(4,2),(4,3),(5,1),(5,2),(5,4),(6,2),(6,6),(7,2),(7,4),(8,2),(8,4),(9,2),(9,4),(10,2),(10,4),(11,2),(11,4),(12,2),(12,4),(13,1),(13,2),(14,2),(14,5),(15,2),(15,3),(16,2),(16,3),(17,2),(18,2),(18,3),(20,1),(20,2),(22,2),(22,6),(23,2),(23,4),(24,2),(24,4),(25,2),(25,4),(26,2),(26,4),(27,2),(27,4);
/*!40000 ALTER TABLE `USER_ROLE` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-02-17 12:44:57

