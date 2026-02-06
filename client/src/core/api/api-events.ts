export type ApiErrorEventDetail = {
  message: string;
  status: number;
  path?: string;
  payload?: unknown;
};

const API_ERROR_EVENT = "ei:api-error";

export function emitApiError(detail: ApiErrorEventDetail): void {
  if (typeof window === "undefined") return;
  window.dispatchEvent(new CustomEvent<ApiErrorEventDetail>(API_ERROR_EVENT, { detail }));
}

export function onApiError(handler: (detail: ApiErrorEventDetail) => void): () => void {
  if (typeof window === "undefined") return () => {};
  const listener = (event: Event) => {
    const custom = event as CustomEvent<ApiErrorEventDetail>;
    handler(custom.detail);
  };
  window.addEventListener(API_ERROR_EVENT, listener);
  return () => window.removeEventListener(API_ERROR_EVENT, listener);
}
