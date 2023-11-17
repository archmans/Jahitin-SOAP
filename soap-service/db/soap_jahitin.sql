CREATE TABLE IF NOT EXISTS `logging` (
  `id` int NOT NULL AUTO_INCREMENT,
  `description` varchar(255) NOT NULL,
  `ip` varchar(128) NOT NULL,
  `endpoint` varchar(255) NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS `subscription` (
  `subscriber_id` int AUTO_INCREMENT NOT NULL,
  `user_id` int NOT NULL,
  `status` enum('ACCEPTED','REJECTED','PENDING') NOT NULL DEFAULT 'PENDING',
  PRIMARY KEY (subscriber_id)
);