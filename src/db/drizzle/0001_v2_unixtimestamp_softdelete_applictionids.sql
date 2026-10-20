CREATE TABLE `categories` (
	`id` integer PRIMARY KEY AUTOINCREMENT NOT NULL,
	`name` text NOT NULL,
	`created_at` integer DEFAULT (CAST(strftime('%s', 'now') AS INTEGER)) NOT NULL,
	`updated_at` integer DEFAULT (CAST(strftime('%s', 'now') AS INTEGER)) NOT NULL
);
--> statement-breakpoint
PRAGMA foreign_keys=OFF;--> statement-breakpoint
CREATE TABLE `__new_passwords` (
	`id` integer PRIMARY KEY AUTOINCREMENT NOT NULL,
	`domain` text NOT NULL,
	`username` text NOT NULL,
	`password` text NOT NULL,
	`notes` text,
	`url` text,
	`created_at` integer DEFAULT (CAST(strftime('%s', 'now') AS INTEGER)) NOT NULL,
	`updated_at` integer DEFAULT (CAST(strftime('%s', 'now') AS INTEGER)) NOT NULL,
	`deleted_at` integer,
	`application_ids` text,
	`source` text,
	`category_id` integer,
	FOREIGN KEY (`category_id`) REFERENCES `categories`(`id`) ON UPDATE no action ON DELETE set null
);
--> statement-breakpoint
INSERT INTO `__new_passwords`("id", "domain", "username", "password", "notes", "url", "created_at", "updated_at") SELECT "id", "domain", "username", "password", "notes", "url", COALESCE(CAST(strftime('%s', `created_at`) AS INTEGER),CAST(strftime('%s', 'now') AS INTEGER)), COALESCE(CAST(strftime('%s', `updated_at`) AS INTEGER), CAST(strftime('%s', 'now') AS INTEGER)) FROM `passwords`;--> statement-breakpoint
DROP TABLE `passwords`;--> statement-breakpoint
ALTER TABLE `__new_passwords` RENAME TO `passwords`;--> statement-breakpoint
PRAGMA foreign_keys=ON;--> statement-breakpoint
CREATE INDEX `passwords_category_id_idx` ON `passwords` (`category_id`);
