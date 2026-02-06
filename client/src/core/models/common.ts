/** Generic pagination response matching Spring Data Page. */
export interface Page<T> {
  /** List of items for the current page. */
  content: T[];
  /** Total items available. */
  totalElements: number;
  /** Total number of pages. */
  totalPages: number;
  /** Current page index (0-based). */
  number: number;
  /** Page size. */
  size: number;
}

/** Common pagination query parameters. */
export interface PageQuery {
  /** Page index (0-based). */
  page?: number;
  /** Page size. */
  size?: number;
  /** Sort specification (e.g. "field,asc"). */
  sort?: string;
  /** Optional search query. */
  q?: string;
  [key: string]: string | number | boolean | null | undefined;
}

/** Shared ID type. */
export type Id = number | string;
