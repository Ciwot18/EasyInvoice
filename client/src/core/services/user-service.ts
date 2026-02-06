import { BaseService } from "./base-service";

/** Payload for creating a backoffice user. */
export interface CreateBackofficeUserRequest {
  /** User email. */
  email: string;
  /** User full name. */
  name: string;
  /** User password. */
  password: string;
}

/** Summary of a user. */
export interface UserSummary {
  /** User identifier. */
  id: number;
  /** User email. */
  email: string;
  /** User name. */
  name: string;
  /** Role enum string from backend. */
  role: "PLATFORM_ADMIN" | "COMPANY_MANAGER" | "BACK_OFFICE" | string;
  /** Enabled flag. */
  enabled: boolean;
}

/** Response payload for profile endpoints. */
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

/** User-related API service. */
export class UserService extends BaseService {
  /** Base path for user endpoints (empty because paths are absolute). */
  protected basePath(): string {
    return "";
  }

  /**
   * Creates a backoffice user.
   * @param request creation payload
   */
  createBackofficeUser(request: CreateBackofficeUserRequest): Promise<UserSummary> {
    return this.post<UserSummary, CreateBackofficeUserRequest>("/manager/backoffice-users", { body: request });
  }

  /**
   * Lists users for the current company.
   */
  listCompanyUsers(): Promise<UserSummary[]> {
    return this.get<UserSummary[]>("/manager/users");
  }

  /**
   * Lists users across platform scope (platform admin).
   */
  listPlatformUsers(): Promise<UserSummary[]> {
    return this.get<UserSummary[]>("/platform/users");
  }

  /**
   * Disables a user by id.
   * @param userId target user id
   */
  disableUser(userId: number | string): Promise<void> {
    return this.patch<void, unknown>(`/manager/users/${userId}/disable`);
  }

  /**
   * Returns the current backoffice user profile.
   */
  getBackofficeProfile(): Promise<ProfileResponse> {
    return this.get<ProfileResponse>("/backoffice/profile");
  }
}
