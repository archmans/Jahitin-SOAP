CREATE TABLE IF NOT EXISTS `logging` (
  `id` int NOT NULL AUTO_INCREMENT,
  `description` varchar(255) NOT NULL,
  `ip` varchar(128) NOT NULL,
  `endpoint` varchar(255) NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (id)
) 

CREATE TABLE IF NOT EXISTS `subscription` (
  `penjahit_id` int NOT NULL,
  `subscriber_id` int NOT NULL,
  `status` enum('ACCEPTED','REJECTED','PENDING') NOT NULL DEFAULT 'PENDING',
  PRIMARY KEY (penjahit_id, subscriber_id)
) 

INSERT INTO `logging` (`id`, `description`, `ip`, `endpoint`, `timestamp`) VALUES
('123123', 'ini testing saja','192.168.0.1','localhost','2022-11-27 21:00:00'),
('123124', 'ini testing saja','192.168.0.1','localhost','2022-11-27 22:00:00'),
('123125', 'ini testing saja','192.168.0.1','localhost','2022-11-27 23:00:00'),
('123126', 'ini testing saja','192.168.0.1','localhost','2022-11-27 01:00:00')