import { BaseService } from "./base-service";

/** Payload for creating a company. */
export interface CreateCompanyRequest {
  /** Company name. */
  name: string;
  /** VAT number (optional). */
  vatNumber?: string;
}

/** Payload for creating a company manager. */
export interface CreateCompanyManagerRequest {
  /** Manager email. */
  email: string;
  /** Manager full name. */
  name: string;
  /** Manager password. */
  password: string;
}

/** Payload for creating a manager from platform scope. */
export interface CreatePlatformCompanyManagerRequest extends CreateCompanyManagerRequest {
  /** Company identifier to assign manager to. */
  companyId: number;
}

/** Summary response for a company. */
export interface CompanySummaryResponse {
  /** Company identifier. */
  id: number;
  /** Company name. */
  name: string;
  /** VAT number (optional). */
  vatNumber?: string | null;
  /** Created date-time. */
  createdAt?: string | null;
}

/** Detailed company response. */
export interface CompanyDetailResponse extends CompanySummaryResponse {
  /** Created date-time. */
  createdAt?: string | null;
}

/** Summary of a user (manager). */
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

/** Company-related API service (platform/admin). */
export class CompanyService extends BaseService {
  /** Base path for platform companies endpoints. */
  protected basePath(): string {
    return "/platform/companies";
  }

  /**
   * Creates a new company.
   * @param request creation payload
   */
  createCompany(request: CreateCompanyRequest): Promise<CompanySummaryResponse> {
    return this.post<CompanySummaryResponse, CreateCompanyRequest>(this.endpoint(), { body: request });
  }

  /**
   * Lists all companies.
   */
  listCompanies(): Promise<CompanySummaryResponse[]> {
    return this.get<CompanySummaryResponse[]>(this.endpoint());
  }

  /**
   * Returns a company by id.
   * @param companyId company identifier
   */
  getCompany(companyId: number | string): Promise<CompanyDetailResponse> {
    return this.get<CompanyDetailResponse>(this.endpoint(`/${companyId}`));
  }

  /**
   * Creates a manager for a company.
   * @param companyId company identifier
   * @param request manager payload
   */
  createCompanyManager(companyId: number | string, request: CreateCompanyManagerRequest): Promise<UserSummary> {
    return this.post<UserSummary, CreateCompanyManagerRequest>(this.endpoint(`/${companyId}/managers`), {
      body: request,
    });
  }

  /**
   * Creates a company manager from platform scope.
   * @param request manager payload with company id
   */
  createPlatformCompanyManager(request: CreatePlatformCompanyManagerRequest): Promise<UserSummary> {
    return this.post<UserSummary, CreatePlatformCompanyManagerRequest>("/platform/managers", {
      body: request,
    });
  }
}
