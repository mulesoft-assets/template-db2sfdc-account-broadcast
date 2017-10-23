CREATE TABLE `Account` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `AccountNumber` varchar(100) DEFAULT NULL,
  `AnnualRevenue` int(15)  NULL,
  `Description` varchar(2000) DEFAULT NULL,
  `Industry` varchar(200) DEFAULT NULL,
  `Phone` varchar(200) DEFAULT NULL,
  `LastModifiedDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `Name` varchar(200) DEFAULT NULL,
  `NumberOfEmployees` int(10) DEFAULT NULL,
  `TickerSymbol` varchar(200)   DEFAULT NULL,
  `Type` varchar(200) DEFAULT NULL,
  `Website` varchar(200) DEFAULT NULL,
  `SalesforceId` varchar(200) DEFAULT '',
  `LastModifiedById` varchar(200) NOT NULL DEFAULT 'mule@localhost',
  PRIMARY KEY (`id`)
)