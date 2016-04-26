-- MySQL dump 10.13  Distrib 5.5.43, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: ADRMinedb
-- ------------------------------------------------------
-- Server version	5.5.43-0ubuntu0.14.04.1

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
-- Table structure for table `Artifact`
--

DROP TABLE IF EXISTS `Artifact`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Artifact` (
  `artifactId` int(11) NOT NULL,
  `POS` varchar(255) DEFAULT NULL,
  `artifactType` int(11) DEFAULT NULL,
  `associatedFilePath` varchar(255) DEFAULT NULL,
  `content` varchar(2000) DEFAULT NULL,
  `corpusName` varchar(255) DEFAULT NULL,
  `endIndex` int(11) DEFAULT NULL,
  `forDemo` tinyint(1) NOT NULL,
  `forTrain` tinyint(1) NOT NULL,
  `lineIndex` int(11) DEFAULT NULL,
  `stanDependency` varchar(2000) DEFAULT NULL,
  `stanPennTree` varchar(2000) DEFAULT NULL,
  `startIndex` int(11) DEFAULT NULL,
  `wordIndex` int(11) DEFAULT NULL,
  `nextArtifact` int(11) DEFAULT NULL,
  `parentArtifact` int(11) DEFAULT NULL,
  `previousArtifact` int(11) DEFAULT NULL,
  PRIMARY KEY (`artifactId`),
  KEY `index_word_index` (`wordIndex`),
  KEY `index_line_index` (`lineIndex`),
  KEY `index_end_index` (`endIndex`),
  KEY `index_content` (`content`(767)),
  KEY `index_start_index` (`startIndex`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Artifact`
--

LOCK TABLES `Artifact` WRITE;
/*!40000 ALTER TABLE `Artifact` DISABLE KEYS */;
/*!40000 ALTER TABLE `Artifact` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `FeatureValuePair`
--

DROP TABLE IF EXISTS `FeatureValuePair`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FeatureValuePair` (
  `featureValuePairId` int(11) NOT NULL,
  `featureName` varchar(255) DEFAULT NULL,
  `featureValue` varchar(255) DEFAULT NULL,
  `featureValueAuxiliary` varchar(255) DEFAULT NULL,
  `tempFeatureIndex` int(11) NOT NULL,
  PRIMARY KEY (`featureValuePairId`),
  UNIQUE KEY `featureName` (`featureName`,`featureValue`,`featureValueAuxiliary`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `FeatureValuePair`
--

LOCK TABLES `FeatureValuePair` WRITE;
/*!40000 ALTER TABLE `FeatureValuePair` DISABLE KEYS */;
/*!40000 ALTER TABLE `FeatureValuePair` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `MLExample`
--

DROP TABLE IF EXISTS `MLExample`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `MLExample` (
  `exampleId` int(11) NOT NULL,
  `associatedFilePath` varchar(255) DEFAULT NULL,
  `corpusName` varchar(255) DEFAULT NULL,
  `expectedClass` int(11) NOT NULL,
  `expectedClosure` int(11) NOT NULL,
  `expectedIntegrated` int(11) NOT NULL,
  `expectedReal` int(11) NOT NULL,
  `forTrain` tinyint(1) NOT NULL,
  `predictedClass` int(11) NOT NULL,
  `predictionEngine` varchar(255) DEFAULT NULL,
  `predictionWeight` double NOT NULL,
  `relatedArtifact` int(11) DEFAULT NULL,
  `relatedChunk` int(11) DEFAULT NULL,
  `relatedPhrase` int(11) DEFAULT NULL,
  `relatedPhraseLink` int(11) DEFAULT NULL,
  PRIMARY KEY (`exampleId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `MLExample`
--

LOCK TABLES `MLExample` WRITE;
/*!40000 ALTER TABLE `MLExample` DISABLE KEYS */;
/*!40000 ALTER TABLE `MLExample` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `MLExampleFeature`
--

DROP TABLE IF EXISTS `MLExampleFeature`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `MLExampleFeature` (
  `exampleFeatureId` int(11) NOT NULL,
  `featureValuePair` int(11) DEFAULT NULL,
  `relatedExample` int(11) DEFAULT NULL,
  PRIMARY KEY (`exampleFeatureId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `MLExampleFeature`
--

LOCK TABLES `MLExampleFeature` WRITE;
/*!40000 ALTER TABLE `MLExampleFeature` DISABLE KEYS */;
/*!40000 ALTER TABLE `MLExampleFeature` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Phrase`
--

DROP TABLE IF EXISTS `Phrase`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Phrase` (
  `phraseId` int(11) NOT NULL,
  `altEndWordIndex` int(11) DEFAULT NULL,
  `altID` varchar(255) DEFAULT NULL,
  `altLineIndex` int(11) DEFAULT NULL,
  `altStartWordIndex` int(11) DEFAULT NULL,
  `endCharOffset` int(11) NOT NULL,
  `normalOffset` int(11) DEFAULT NULL,
  `normalizedHead` varchar(255) DEFAULT NULL,
  `phraseContent` varchar(1000) NOT NULL,
  `phraseEntityType` varchar(255) DEFAULT NULL,
  `startCharOffset` int(11) NOT NULL,
  `endArtifact` int(11) DEFAULT NULL,
  `govVerb` int(11) DEFAULT NULL,
  `headArtifact` int(11) DEFAULT NULL,
  `startArtifact` int(11) DEFAULT NULL,
  PRIMARY KEY (`phraseId`),
  KEY `phraseContent` (`phraseContent`(767))
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Phrase`
--

LOCK TABLES `Phrase` WRITE;
/*!40000 ALTER TABLE `Phrase` DISABLE KEYS */;
/*!40000 ALTER TABLE `Phrase` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `PhraseLink`
--

DROP TABLE IF EXISTS `PhraseLink`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PhraseLink` (
  `phraseLinkId` int(11) NOT NULL,
  `altLinkID` varchar(255) DEFAULT NULL,
  `linkType` int(11) DEFAULT NULL,
  `linkTypeClosure` int(11) DEFAULT NULL,
  `linkTypeIntegrated` int(11) DEFAULT NULL,
  `linkTypeReal` int(11) DEFAULT NULL,
  `fromPhrase` int(11) DEFAULT NULL,
  `toPhrase` int(11) DEFAULT NULL,
  PRIMARY KEY (`phraseLinkId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `PhraseLink`
--

LOCK TABLES `PhraseLink` WRITE;
/*!40000 ALTER TABLE `PhraseLink` DISABLE KEYS */;
/*!40000 ALTER TABLE `PhraseLink` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SentenceChunk`
--

DROP TABLE IF EXISTS `SentenceChunk`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SentenceChunk` (
  `chunkId` int(11) NOT NULL,
  `chunkContent` varchar(2000) DEFAULT NULL,
  `hasADR` tinyint(1) NOT NULL,
  `endArtifact` int(11) DEFAULT NULL,
  `nextChunk` int(11) DEFAULT NULL,
  `parentSentence` int(11) DEFAULT NULL,
  `previousChunk` int(11) DEFAULT NULL,
  `startArtifact` int(11) DEFAULT NULL,
  PRIMARY KEY (`chunkId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SentenceChunk`
--

LOCK TABLES `SentenceChunk` WRITE;
/*!40000 ALTER TABLE `SentenceChunk` DISABLE KEYS */;
/*!40000 ALTER TABLE `SentenceChunk` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-04-25 12:57:53
