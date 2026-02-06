package com.kernith.easyinvoice.service;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.dto.dashboard.AdminDashboardSummaryResponse;
import com.kernith.easyinvoice.data.dto.dashboard.CustomerInvoiceSummaryResponse;
import com.kernith.easyinvoice.data.dto.dashboard.InvoiceStatusAggregateResponse;
import com.kernith.easyinvoice.data.dto.dashboard.ManagerDashboardSummaryResponse;
import com.kernith.easyinvoice.data.dto.dashboard.QuoteStatusAggregateResponse;
import com.kernith.easyinvoice.data.model.InvoiceStatus;
import com.kernith.easyinvoice.data.model.QuoteStatus;
import com.kernith.easyinvoice.data.model.UserRole;
import com.kernith.easyinvoice.data.repository.CompanyRepository;
import com.kernith.easyinvoice.data.repository.CustomerRepository;
import com.kernith.easyinvoice.data.repository.InvoiceRepository;
import com.kernith.easyinvoice.data.repository.InvoiceStatusAggregate;
import com.kernith.easyinvoice.data.repository.QuoteRepository;
import com.kernith.easyinvoice.data.repository.QuoteStatusAggregate;
import com.kernith.easyinvoice.data.repository.UserRepository;
import com.kernith.easyinvoice.helper.Utils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Dashboard aggregations for managers and platform admins.
 */
@Service
public class DashboardService {

    private final QuoteRepository quoteRepository;
    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public DashboardService(
            QuoteRepository quoteRepository,
            InvoiceRepository invoiceRepository,
            CustomerRepository customerRepository,
            CompanyRepository companyRepository,
            UserRepository userRepository
    ) {
        this.quoteRepository = quoteRepository;
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    /**
     * Builds the manager dashboard summary for the current company.
     *
     * @param principal authenticated principal
     * @return summary response
     */
    public ManagerDashboardSummaryResponse getManagerSummary(AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER));
        Long companyId = Utils.getRequiredCompanyId(principal);

        List<QuoteStatusAggregateResponse> quoteAggregates =
                normalizeQuoteAggregates(quoteRepository.aggregateByStatus(companyId));
        List<InvoiceStatusAggregateResponse> invoiceAggregates =
                normalizeInvoiceAggregates(invoiceRepository.aggregateByStatus(companyId));

        BigDecimal paidTotal = getInvoiceTotal(invoiceAggregates, InvoiceStatus.PAID);
        BigDecimal issuedTotal = getInvoiceTotal(invoiceAggregates, InvoiceStatus.ISSUED);
        BigDecimal outstandingTotal = issuedTotal
                .add(getInvoiceTotal(invoiceAggregates, InvoiceStatus.OVERDUE));

        return new ManagerDashboardSummaryResponse(
                quoteAggregates,
                invoiceAggregates,
                paidTotal,
                issuedTotal,
                outstandingTotal
        );
    }

    /**
     * Builds an invoice summary for a specific customer.
     *
     * @param customerId customer identifier
     * @param principal authenticated principal
     * @return optional summary response
     */
    public Optional<CustomerInvoiceSummaryResponse> getCustomerInvoiceSummary(
            Long customerId,
            AuthPrincipal principal
    ) {
        Utils.requireRoles(principal, List.of(UserRole.COMPANY_MANAGER, UserRole.BACK_OFFICE));
        Long companyId = Utils.getRequiredCompanyId(principal);

        if (customerRepository.findByIdAndCompanyId(customerId, companyId).isEmpty()) {
            return Optional.empty();
        }

        List<InvoiceStatusAggregateResponse> invoiceAggregates =
                normalizeInvoiceAggregates(invoiceRepository.aggregateByStatusForCustomer(companyId, customerId));

        BigDecimal paidTotal = getInvoiceTotal(invoiceAggregates, InvoiceStatus.PAID);
        BigDecimal issuedTotal = getInvoiceTotal(invoiceAggregates, InvoiceStatus.ISSUED);
        BigDecimal outstandingTotal = issuedTotal
                .add(getInvoiceTotal(invoiceAggregates, InvoiceStatus.OVERDUE));

        return Optional.of(new CustomerInvoiceSummaryResponse(
                customerId,
                invoiceAggregates,
                paidTotal,
                issuedTotal,
                outstandingTotal
        ));
    }

    /**
     * Builds the platform admin dashboard summary.
     *
     * @param principal authenticated principal
     * @return summary response
     */
    public AdminDashboardSummaryResponse getAdminSummary(AuthPrincipal principal) {
        Utils.requireRoles(principal, List.of(UserRole.PLATFORM_ADMIN));

        long companies = companyRepository.count();
        long users = userRepository.count();
        long enabledUsers = userRepository.countByEnabledTrue();
        long disabledUsers = userRepository.countByEnabledFalse();

        return new AdminDashboardSummaryResponse(companies, users, enabledUsers, disabledUsers);
    }

    private List<QuoteStatusAggregateResponse> normalizeQuoteAggregates(List<QuoteStatusAggregate> rows) {
        Map<QuoteStatus, QuoteStatusAggregateResponse> map = new EnumMap<>(QuoteStatus.class);
        for (QuoteStatus status : QuoteStatus.values()) {
            map.put(status, new QuoteStatusAggregateResponse(status, 0L, BigDecimal.ZERO));
        }
        for (QuoteStatusAggregate row : rows) {
            QuoteStatus status = row.getStatus();
            long count = row.getCount() == null ? 0L : row.getCount();
            BigDecimal total = row.getTotalAmount() == null ? BigDecimal.ZERO : row.getTotalAmount();
            map.put(status, new QuoteStatusAggregateResponse(status, count, total));
        }
        return new ArrayList<>(map.values());
    }

    private List<InvoiceStatusAggregateResponse> normalizeInvoiceAggregates(List<InvoiceStatusAggregate> rows) {
        Map<InvoiceStatus, InvoiceStatusAggregateResponse> map = new EnumMap<>(InvoiceStatus.class);
        for (InvoiceStatus status : InvoiceStatus.values()) {
            map.put(status, new InvoiceStatusAggregateResponse(status, 0L, BigDecimal.ZERO));
        }
        for (InvoiceStatusAggregate row : rows) {
            InvoiceStatus status = row.getStatus();
            long count = row.getCount() == null ? 0L : row.getCount();
            BigDecimal total = row.getTotalAmount() == null ? BigDecimal.ZERO : row.getTotalAmount();
            map.put(status, new InvoiceStatusAggregateResponse(status, count, total));
        }
        return new ArrayList<>(map.values());
    }

    private BigDecimal getInvoiceTotal(
            List<InvoiceStatusAggregateResponse> rows,
            InvoiceStatus status
    ) {
        for (InvoiceStatusAggregateResponse row : rows) {
            if (row.status() == status) {
                return row.totalAmount() == null ? BigDecimal.ZERO : row.totalAmount();
            }
        }
        return BigDecimal.ZERO;
    }
}
