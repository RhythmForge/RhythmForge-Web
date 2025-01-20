CREATE TABLE `userpages` IF NOT EXISTS (
  `id` int NOT NULL,
  `user_id` int NOT NULL,
  `html` text,
  `raw` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE `userpages`
  ADD PRIMARY KEY (`id`);

ALTER TABLE `userpages`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;
COMMIT;