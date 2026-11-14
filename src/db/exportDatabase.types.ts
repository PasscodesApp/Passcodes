export type ExportDatabaseResult =
  /** The user chose a place and the file was written / the download started. */
  | {
      status: "saved";
      /**
       * `true` when a single consistent .db file could not be produced and the
       * unmerged write-ahead log was saved next to it, so no data is lost.
       */
      includesWalFile: boolean;
    }
  /** The user closed the save dialog. Not an error. */
  | { status: "cancelled" };
