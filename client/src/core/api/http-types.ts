/** Allowed HTTP methods for API requests. */
export type HttpMethod = "GET" | "POST" | "PATCH" | "PUT" | "DELETE";

/** Query string parameters appended to the request URL. */
export type QueryParams = Record<string, string | number | boolean | null | undefined>;

/** Expected response payload type for parsing the body. */
export type ResponseType = "json" | "text" | "blob" | "arrayBuffer";

/** Normalized HTTP request configuration. */
export interface HttpRequest<TBody = unknown> {
  /** HTTP method to call. */
  method: HttpMethod;
  /** Absolute URL for the request. */
  url: string;
  /** Optional request headers. */
  headers?: Record<string, string>;
  /** Optional request body. */
  body?: TBody;
  /** Optional abort signal for cancelling the request. */
  signal?: AbortSignal;
  /** Response parsing strategy. */
  responseType?: ResponseType;
}

/** Normalized HTTP response returned by the client. */
export interface HttpResponse<T = unknown> {
  /** True if the status code is in the 2xx range. */
  ok: boolean;
  /** HTTP status code. */
  status: number;
  /** Response headers. */
  headers: Headers;
  /** Parsed response payload. */
  data: T;
}

/** Minimal HTTP client abstraction to allow different transports. */
export interface HttpClient {
  /**
   * Sends an HTTP request and returns a normalized response.
   * @param request request configuration
   */
  send<TResponse, TBody = unknown>(request: HttpRequest<TBody>): Promise<HttpResponse<TResponse>>;
}

/** Error thrown when an HTTP response is not ok (non-2xx). */
export class HttpError extends Error {
  /** HTTP status code from the response. */
  readonly status: number;
  /** Parsed response payload (if any). */
  readonly payload: unknown;

  /**
   * @param message error description
   * @param status HTTP status code
   * @param payload response payload (parsed)
   */
  constructor(message: string, status: number, payload: unknown) {
    super(message);
    this.name = "HttpError";
    this.status = status;
    this.payload = payload;
  }
}
