CREATE TABLE `passwords` (
	`id` integer PRIMARY KEY AUTOINCREMENT NOT NULL,
	`domain` text NOT NULL,
	`username` text NOT NULL,
	`password` text NOT NULL,
	`notes` text,
	`url` text,
	`created_at` text DEFAULT (CURRENT_TIMESTAMP),
	`updated_at` text DEFAULT (CURRENT_TIMESTAMP)
);
