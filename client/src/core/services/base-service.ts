import { ApiClient, RequestOptions } from "@/src/core/api";

/** Base class for API services with helper request methods. */
export abstract class BaseService {
  /** Shared API client instance. */
  protected readonly client: ApiClient;

  /**
   * @param client API client used for HTTP requests
   */
  constructor(client: ApiClient) {
    this.client = client;
  }

  /** Returns the base path for the resource (e.g., "/invoices"). */
  protected abstract basePath(): string;

  /**
   * Builds an endpoint by combining base path and sub-path.
   * @param path optional sub-path
   */
  protected endpoint(path: string = ""): string {
    const base = this.basePath().replace(/\/$/, "");
    const suffix = path.startsWith("/") ? path : `/${path}`;
    return path ? `${base}${suffix}` : base;
  }

  /**
   * Performs a GET request.
   * @param path endpoint path
   * @param options request options
   */
  protected get<TResponse>(path: string, options?: RequestOptions): Promise<TResponse> {
    return this.client.request<TResponse>("GET", path, options);
  }

  /**
   * Performs a DELETE request.
   * @param path endpoint path
   * @param options request options
   */
  protected delete<TResponse>(path: string, options?: RequestOptions): Promise<TResponse> {
    return this.client.request<TResponse>("DELETE", path, options);
  }

  /**
   * Performs a POST request.
   * @param path endpoint path
   * @param options request options
   */
  protected post<TResponse, TBody>(path: string, options?: RequestOptions<TBody>): Promise<TResponse> {
    return this.client.request<TResponse, TBody>("POST", path, options);
  }

  /**
   * Performs a PATCH request.
   * @param path endpoint path
   * @param options request options
   */
  protected patch<TResponse, TBody>(path: string, options?: RequestOptions<TBody>): Promise<TResponse> {
    return this.client.request<TResponse, TBody>("PATCH", path, options);
  }
}
