/**
 * Picks the columns an edit is allowed to change.
 *
 * Callers sometimes pass more than the editable fields (the details screen
 * passes its whole form state, which includes the formatted "Updated" text).
 * `updated_at` is maintained by the database layer, so anything outside this
 * list must never reach the UPDATE statement.
 *
 * Pure function, no database imports, so it runs in plain Node tests.
 */

export interface EditablePasswordFields {
  domain?: string;
  username?: string;
  password?: string;
  notes?: string | null;
  url?: string | null;
  /** `null` = move to Uncategorized. */
  categoryId?: number | null;
}

export function pickEditablePasswordFields(
  data: EditablePasswordFields,
): EditablePasswordFields {
  const changes: EditablePasswordFields = {};

  if (data.domain !== undefined) changes.domain = data.domain;
  if (data.username !== undefined) changes.username = data.username;
  if (data.password !== undefined) changes.password = data.password;
  if (data.notes !== undefined) changes.notes = data.notes;
  if (data.url !== undefined) changes.url = data.url;
  if (data.categoryId !== undefined) changes.categoryId = data.categoryId;

  return changes;
}
