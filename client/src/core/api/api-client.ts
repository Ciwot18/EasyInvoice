import { ApiConfig } from "./api-config";
import { FetchHttpClient } from "./fetch-http-client";
import {
  HttpClient,
  HttpError,
  HttpMethod,
  HttpRequest,
  HttpResponse,
  QueryParams,
  ResponseType,
} from "./http-types";
import { emitApiError } from "./api-events";

export interface RequestOptions<TBody = unknown> {
  /** Optional body payload. */
  body?: TBody;
  /** Optional custom headers. */
  headers?: Record<string, string>;
  /** Optional query string params. */
  query?: QueryParams;
  /** Optional abort signal. */
  signal?: AbortSignal;
  /** Response parsing strategy. */
  responseType?: ResponseType;
}

/** High-level API client that builds requests and handles auth/headers. */
export class ApiClient {
  private readonly config: ApiConfig;
  private readonly http: HttpClient;

  /**
   * @param config API configuration (base URL, token provider)
   * @param http optional HTTP client implementation
   */
  constructor(config: ApiConfig, http: HttpClient = new FetchHttpClient()) {
    this.config = config;
    this.http = http;
  }

  /**
   * Sends a GET/DELETE request.
   * @param method HTTP method
   * @param path path relative to base URL
   * @param options optional request options
   */
  request<TResponse>(method: "GET" | "DELETE", path: string, options?: RequestOptions): Promise<TResponse>;
  /**
   * Sends a POST/PATCH/PUT request.
   * @param method HTTP method
   * @param path path relative to base URL
   * @param options optional request options (including body)
   */
  request<TResponse, TBody>(method: "POST" | "PATCH" | "PUT", path: string, options?: RequestOptions<TBody>): Promise<TResponse>;
  /**
   * Sends an HTTP request and returns the parsed response data.
   * @param method HTTP method
   * @param path path relative to base URL
   * @param options request options
   */
  async request<TResponse, TBody>(
    method: HttpMethod,
    path: string,
    options: RequestOptions<TBody> = {}
  ): Promise<TResponse> {
    const url = this.buildUrl(path, options.query);
    const headers = this.buildHeaders(options.headers, options.body);

    const request: HttpRequest<TBody> = {
      method,
      url,
      headers,
      body: options.body,
      signal: options.signal,
      responseType: options.responseType,
    };

    const response: HttpResponse<TResponse> = await this.http.send<TResponse, TBody>(request);

    if (!response.ok) {
      const error = new HttpError(`Request failed with status ${response.status}`, response.status, response.data);
      emitApiError({
        message: error.message,
        status: error.status,
        path,
        payload: response.data,
      });
      throw error;
    }

    return response.data;
  }

  /**
   * Builds headers including JSON and Authorization if available.
   * @param headers custom headers
   * @param body optional body to decide content type
   */
  private buildHeaders(headers?: Record<string, string>, body?: unknown): Record<string, string> {
    const token = this.config.tokenProvider?.getToken();
    const baseHeaders: Record<string, string> = {
      Accept: "application/json",
    };

    if (!(body instanceof FormData) && body !== undefined) {
      baseHeaders["Content-Type"] = "application/json";
    }

    if (token) {
      baseHeaders.Authorization = `Bearer ${token}`;
    }

    return {
      ...baseHeaders,
      ...(headers ?? {}),
    };
  }

  /**
   * Builds a full URL from base path and query parameters.
   * @param path path relative to base URL
   * @param query optional query params
   */
  private buildUrl(path: string, query?: QueryParams): string {
    const base = this.config.baseUrl.replace(/\/$/, "");
    const trimmedPath = path.startsWith("/") ? path : `/${path}`;
    const url = new URL(`${base}${trimmedPath}`, window.location.origin);

    if (query) {
      for (const [key, value] of Object.entries(query)) {
        if (value === undefined || value === null) continue;
        url.searchParams.set(key, String(value));
      }
    }

    return url.toString();
  }
}
