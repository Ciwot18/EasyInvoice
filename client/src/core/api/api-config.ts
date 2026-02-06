/** Provides the current auth token for API calls. */
export interface AuthTokenProvider {
  /** Returns the token string or null if not available. */
  getToken(): string | null;
}

/** Configuration options for ApiConfig. */
export interface ApiConfigOptions {
  /** Base URL for API calls (default: "/api"). */
  baseUrl?: string;
  /** Optional provider used to attach Authorization header. */
  tokenProvider?: AuthTokenProvider;
}

/** Immutable API configuration used by ApiClient. */
export class ApiConfig {
  /** Base URL for API calls. */
  readonly baseUrl: string;
  /** Optional auth token provider. */
  readonly tokenProvider?: AuthTokenProvider;

  /**
   * @param options config options including baseUrl and token provider
   */
  constructor(options: ApiConfigOptions = {}) {
    this.baseUrl = options.baseUrl ?? "/api";
    this.tokenProvider = options.tokenProvider;
  }

  /**
   * Creates config reading base URL from Vite env `VITE_API_BASE_URL`.
   * @param options additional options (e.g., token provider)
   */
  static fromEnv(options: Omit<ApiConfigOptions, "baseUrl"> = {}): ApiConfig {
    const envBase = import.meta.env.VITE_API_BASE_URL as string | undefined;
    return new ApiConfig({
      baseUrl: envBase ?? "/api",
      tokenProvider: options.tokenProvider,
    });
  }
}
