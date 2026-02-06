import { BaseService } from "./base-service";

/** Payload for login endpoint. */
export interface LoginRequest {
  /** Company identifier (optional depending on auth flow). */
  companyId?: number | null;
  /** User email. */
  email: string;
  /** User password. */
  password: string;
}

/** Response payload for login endpoint. */
export interface LoginResponse {
  /** JWT token string. */
  token: string;
  /** User id. */
  userId: number;
  /** Company id. */
  companyId: number;
  /** Role enum string from backend. */
  role: "PLATFORM_ADMIN" | "COMPANY_MANAGER" | "BACK_OFFICE" | string;
}

/** Response payload for current user profile. */
export interface ProfileResponse {
  /** User id. */
  id: number;
  /** User email. */
  email: string;
  /** User name. */
  name: string;
  /** Role enum string from backend. */
  role: "PLATFORM_ADMIN" | "COMPANY_MANAGER" | "BACK_OFFICE" | string;
  /** Enabled flag. */
  enabled: boolean;
  /** Created date-time. */
  createdAt: string;
}

/** Authentication API service. */
export class AuthService extends BaseService {
  /** Base path for auth endpoints. */
  protected basePath(): string {
    return "/auth";
  }

  /**
   * Authenticates a user and returns a token.
   * @param request login payload
   */
  login(request: LoginRequest): Promise<LoginResponse> {
    return this.post<LoginResponse, LoginRequest>(this.endpoint("/login"), { body: request });
  }

  /**
   * Returns the current authenticated user profile.
   */
  me(): Promise<ProfileResponse> {
    return this.get<ProfileResponse>(this.endpoint("/me"));
  }
}
