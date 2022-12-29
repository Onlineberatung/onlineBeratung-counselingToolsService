CREATE TABLE `counselingtoolsservice`.`user_tools` (
  `user_id` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `tools` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;