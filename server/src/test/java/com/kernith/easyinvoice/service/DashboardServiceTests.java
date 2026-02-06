package com.kernith.easyinvoice.service;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.dto.dashboard.ManagerDashboardSummaryResponse;
import com.kernith.easyinvoice.data.dto.dashboard.CustomerInvoiceSummaryResponse;
import com.kernith.easyinvoice.data.model.InvoiceStatus;
import com.kernith.easyinvoice.data.model.QuoteStatus;
import com.kernith.easyinvoice.data.repository.CompanyRepository;
import com.kernith.easyinvoice.data.repository.CustomerRepository;
import com.kernith.easyinvoice.data.repository.InvoiceRepository;
import com.kernith.easyinvoice.data.repository.InvoiceStatusAggregate;
import com.kernith.easyinvoice.data.repository.QuoteRepository;
import com.kernith.easyinvoice.data.repository.QuoteStatusAggregate;
import com.kernith.easyinvoice.data.repository.UserRepository;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTests {

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData metaData;

    @Test
    void getManagerSummaryComputesTotals() {
        DashboardService service = new DashboardService(
                quoteRepository,
                invoiceRepository,
                customerRepository,
                companyRepository,
                userRepository,
                dataSource
        );

        when(quoteRepository.aggregateByStatus(2L)).thenReturn(List.of(
                new QuoteAgg(QuoteStatus.DRAFT, 2L, new BigDecimal("300.00"))
        ));
        when(invoiceRepository.aggregateByStatus(2L)).thenReturn(List.of(
                new InvoiceAgg(InvoiceStatus.ISSUED, 1L, new BigDecimal("100.00")),
                new InvoiceAgg(InvoiceStatus.OVERDUE, 1L, new BigDecimal("40.00")),
                new InvoiceAgg(InvoiceStatus.PAID, 1L, new BigDecimal("60.00"))
        ));

        AuthPrincipal principal = new AuthPrincipal(10L, 2L, "COMPANY_MANAGER", List.of());
        ManagerDashboardSummaryResponse response = service.getManagerSummary(principal);

        assertThat(response.paidTotal()).isEqualByComparingTo(new BigDecimal("60.00"));
        assertThat(response.issuedTotal()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(response.outstandingTotal()).isEqualByComparingTo(new BigDecimal("140.00"));
        assertThat(response.invoices()).hasSize(InvoiceStatus.values().length);
        assertThat(response.quotes()).hasSize(QuoteStatus.values().length);
    }

    @Test
    void getCustomerInvoiceSummaryReturnsEmptyWhenMissingCustomer() {
        DashboardService service = new DashboardService(
                quoteRepository,
                invoiceRepository,
                customerRepository,
                companyRepository,
                userRepository,
                dataSource
        );
        when(customerRepository.findByIdAndCompanyId(100L, 2L)).thenReturn(Optional.empty());

        AuthPrincipal principal = new AuthPrincipal(10L, 2L, "COMPANY_MANAGER", List.of());
        Optional<CustomerInvoiceSummaryResponse> response = service.getCustomerInvoiceSummary(100L, principal);

        assertThat(response).isEmpty();
    }

    @Test
    void getAdminSummaryReportsInMemoryH2() throws Exception {
        DashboardService service = new DashboardService(
                quoteRepository,
                invoiceRepository,
                customerRepository,
                companyRepository,
                userRepository,
                dataSource
        );
        when(companyRepository.count()).thenReturn(3L);
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByEnabledTrue()).thenReturn(8L);
        when(userRepository.countByEnabledFalse()).thenReturn(2L);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getURL()).thenReturn("jdbc:h2:mem:easyinvoice;DB_CLOSE_DELAY=-1");

        AuthPrincipal principal = new AuthPrincipal(1L, 1L, "PLATFORM_ADMIN", List.of());
        var response = service.getAdminSummary(principal);

        assertThat(response.dbPath()).isEqualTo("in-memory");
        assertThat(response.dbFileBytes()).isNull();
    }

    private static final class InvoiceAgg implements InvoiceStatusAggregate {
        private final InvoiceStatus status;
        private final Long count;
        private final BigDecimal totalAmount;

        private InvoiceAgg(InvoiceStatus status, Long count, BigDecimal totalAmount) {
            this.status = status;
            this.count = count;
            this.totalAmount = totalAmount;
        }

        @Override
        public InvoiceStatus getStatus() {
            return status;
        }

        @Override
        public Long getCount() {
            return count;
        }

        @Override
        public BigDecimal getTotalAmount() {
            return totalAmount;
        }
    }

    private static final class QuoteAgg implements QuoteStatusAggregate {
        private final QuoteStatus status;
        private final Long count;
        private final BigDecimal totalAmount;

        private QuoteAgg(QuoteStatus status, Long count, BigDecimal totalAmount) {
            this.status = status;
            this.count = count;
            this.totalAmount = totalAmount;
        }

        @Override
        public QuoteStatus getStatus() {
            return status;
        }

        @Override
        public Long getCount() {
            return count;
        }

        @Override
        public BigDecimal getTotalAmount() {
            return totalAmount;
        }
    }
}
