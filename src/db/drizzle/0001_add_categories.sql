CREATE TABLE `categories` (
	`id` integer PRIMARY KEY AUTOINCREMENT NOT NULL,
	`name` text NOT NULL,
	`created_at` text DEFAULT (CURRENT_TIMESTAMP),
	`updated_at` text DEFAULT (CURRENT_TIMESTAMP)
);
--> statement-breakpoint
ALTER TABLE `passwords` ADD `category_id` integer REFERENCES categories(id) ON DELETE set null;--> statement-breakpoint
CREATE INDEX `passwords_category_id_idx` ON `passwords` (`category_id`);
