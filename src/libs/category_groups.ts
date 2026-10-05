import {
  useCategoryRepository,
  usePasswordRepository,
} from "@/contexts/RepositoryContext";
import type { passwords } from "@/db/schema";
import { UNCATEGORIZED_LABEL } from "@/repositories/CategoryRepository";
import { useLiveQuery } from "drizzle-orm/expo-sqlite";
import { useMemo } from "react";

export type Password = typeof passwords.$inferSelect;

export const UNCATEGORIZED_KEY = "uncategorized";

/** One collapsible section of the grouped layout. */
export type CategoryGroup = {
  key: string;
  /** `null` = Uncategorized */
  categoryId: number | null;
  name: string;
  count: number;
  passwords: Password[];
};

/** One folder of the folders layout (count = ALL passwords of it). */
export type FolderData = {
  key: string;
  categoryId: number | null;
  name: string;
  count: number;
};

/**
 * Single source of truth for BOTH home layouts: loads passwords + categories
 * (live) and derives sections, folders and counts, so the two layouts never
 * disagree.
 *
 * A password whose category no longer exists is treated as Uncategorized, so
 * it can never silently disappear.
 */
export function useCategoryGroups(query: string, refreshKey = 0) {
  const passwordRepository = usePasswordRepository();
  const categoryRepository = useCategoryRepository();

  const { data: passwordList = [] } = useLiveQuery(
    passwordRepository.observeAll(),
    [refreshKey],
  );

  const { data: categoryList = [] } = useLiveQuery(
    categoryRepository.observeAll(),
    [],
  );

  // xxxxx folders + counts (never depends on the search query) xxxxx
  const { folders, resolveCategoryId } = useMemo(() => {
    const nameById = new Map(categoryList.map((c) => [c.id, c.name]));

    const resolve = (password: Password): number | null =>
      password.categoryId !== null && nameById.has(password.categoryId)
        ? password.categoryId
        : null;

    const counts = new Map<number | null, number>();

    for (const password of passwordList) {
      const id = resolve(password);
      counts.set(id, (counts.get(id) ?? 0) + 1);
    }

    const folderList: FolderData[] = categoryList.map((category) => ({
      key: String(category.id),
      categoryId: category.id,
      name: category.name,
      count: counts.get(category.id) ?? 0,
    }));

    // Uncategorized folder is always there, so there is always a place to
    // add a password, even before any category exists.
    folderList.push({
      key: UNCATEGORIZED_KEY,
      categoryId: null,
      name: UNCATEGORIZED_LABEL,
      count: counts.get(null) ?? 0,
    });

    return { folders: folderList, resolveCategoryId: resolve };
  }, [passwordList, categoryList]);

  // xxxxx sections + visible ids (depends on the search query) xxxxx
  const { groups, visibleIds } = useMemo(() => {
    const nameById = new Map(categoryList.map((c) => [c.id, c.name]));

    const filtered = query
      ? passwordList.filter((password) => {
          const categoryName = (
            (password.categoryId !== null
              ? nameById.get(password.categoryId)
              : undefined) ?? UNCATEGORIZED_LABEL
          ).toLowerCase();

          return (
            (password.domain?.toLowerCase() ?? "").includes(query) ||
            (password.username?.toLowerCase() ?? "").includes(query) ||
            (password.url?.toLowerCase() ?? "").includes(query) ||
            (password.notes?.toLowerCase() ?? "").includes(query) ||
            categoryName.includes(query)
          );
        })
      : passwordList;

    const byCategory = new Map<number | null, Password[]>();

    for (const password of filtered) {
      const id = resolveCategoryId(password);
      const bucket = byCategory.get(id);

      if (bucket) {
        bucket.push(password);
      } else {
        byCategory.set(id, [password]);
      }
    }

    const groupList: CategoryGroup[] = [];

    for (const category of categoryList) {
      const items = byCategory.get(category.id) ?? [];

      // Empty categories are listed too (so a new category is visible),
      // but not while searching.
      if (items.length > 0 || !query) {
        groupList.push({
          key: String(category.id),
          categoryId: category.id,
          name: category.name,
          count: items.length,
          passwords: items,
        });
      }
    }

    const uncategorized = byCategory.get(null) ?? [];

    if (uncategorized.length > 0) {
      groupList.push({
        key: UNCATEGORIZED_KEY,
        categoryId: null,
        name: UNCATEGORIZED_LABEL,
        count: uncategorized.length,
        passwords: uncategorized,
      });
    }

    return { groups: groupList, visibleIds: filtered.map((p) => p.id) };
  }, [passwordList, categoryList, query, resolveCategoryId]);

  return {
    groups,
    folders,
    visibleIds,
    hasPasswords: passwordList.length > 0,
    hasCategories: categoryList.length > 0,
  };
}
