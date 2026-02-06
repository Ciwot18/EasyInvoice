import { ApiClient } from "@/src/core/api";
import { AuthService } from "./auth-service";
import { CompanyService } from "./company-service";
import { CustomerService } from "./customer-service";
import { InvoiceItemService } from "./invoice-item-service";
import { InvoiceService } from "./invoice-service";
import { QuoteItemService } from "./quote-item-service";
import { QuoteService } from "./quote-service";
import { UserService } from "./user-service";

/** Options for configuring service registry. */
export interface ServiceRegistryOptions {
  /** Override base path for quote item endpoints. */
  quoteItemsBasePath?: string;
}

/** Convenience registry to access all API services from one place. */
export class ServiceRegistry {
  /** Auth service. */
  readonly auth: AuthService;
  /** Users service. */
  readonly users: UserService;
  /** Companies service. */
  readonly companies: CompanyService;
  /** Customers service. */
  readonly customers: CustomerService;
  /** Invoices service. */
  readonly invoices: InvoiceService;
  /** Invoice items service. */
  readonly invoiceItems: InvoiceItemService;
  /** Quotes service. */
  readonly quotes: QuoteService;
  /** Quote items service. */
  readonly quoteItems: QuoteItemService;

  /**
   * @param client API client instance
   * @param options optional registry configuration
   */
  constructor(client: ApiClient, options: ServiceRegistryOptions = {}) {
    this.auth = new AuthService(client);
    this.users = new UserService(client);
    this.companies = new CompanyService(client);
    this.customers = new CustomerService(client);
    this.invoices = new InvoiceService(client);
    this.invoiceItems = new InvoiceItemService(client);
    this.quotes = new QuoteService(client);
    this.quoteItems = new QuoteItemService(client, options.quoteItemsBasePath ?? "/api/quotes");
  }
}
