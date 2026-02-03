package com.kernith.easyinvoice.data.repository;

import com.kernith.easyinvoice.data.model.Customer;
import com.kernith.easyinvoice.data.model.CustomerStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
public class CustomerRepositoryTests {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void testFindByCompanyIdAndStatusOrderByDisplayNameAsc() {
        assertThat(customerRepository.findByCompanyIdAndStatusOrderByDisplayNameAsc(2L, CustomerStatus.ACTIVE))
                .extracting(Customer::getDisplayName)
                .containsExactly("Alfa Uno", "Beta Due");

        assertThat(customerRepository.findByCompanyIdAndStatusOrderByDisplayNameAsc(2L, CustomerStatus.ARCHIVED))
                .extracting(Customer::getDisplayName)
                .containsExactly("Gamma Tre");
    }

    @Test
    void testFindByCompanyIdAndStatusWithPaging() {
        Page<Customer> page0 = customerRepository.findByCompanyIdAndStatus(
                2L,
                CustomerStatus.ACTIVE,
                PageRequest.of(0, 1, Sort.by("displayName"))
        );
        Page<Customer> page1 = customerRepository.findByCompanyIdAndStatus(
                2L,
                CustomerStatus.ACTIVE,
                PageRequest.of(1, 1, Sort.by("displayName"))
        );

        assertThat(page0.getTotalElements()).isEqualTo(2);
        assertThat(page0.getContent()).extracting(Customer::getDisplayName).containsExactly("Alfa Uno");
        assertThat(page1.getContent()).extracting(Customer::getDisplayName).containsExactly("Beta Due");

        Page<Customer> betaPage = customerRepository.findByCompanyIdAndStatus(
                3L,
                CustomerStatus.ACTIVE,
                PageRequest.of(0, 10, Sort.by("displayName"))
        );
        assertThat(betaPage.getTotalElements()).isEqualTo(1);
    }

    @Test
    void testFindByIdAndCompanyIdAndStatus() {
        assertThat(customerRepository.findByIdAndCompanyIdAndStatus(100L, 2L, CustomerStatus.ACTIVE))
                .isPresent()
                .get()
                .extracting(Customer::getDisplayName)
                .isEqualTo("Alfa Uno");

        assertThat(customerRepository.findByIdAndCompanyIdAndStatus(102L, 2L, CustomerStatus.ACTIVE))
                .isNotPresent();
    }

    @Test
    void testFindByCompanyIdAndVatNumberAndStatus() {
        assertThat(customerRepository.findByCompanyIdAndVatNumberAndStatus(2L, "ITVAT002", CustomerStatus.ACTIVE))
                .isPresent()
                .get()
                .extracting(Customer::getDisplayName)
                .isEqualTo("Beta Due");

        assertThat(customerRepository.findByCompanyIdAndVatNumberAndStatus(2L, "ITVAT003", CustomerStatus.ACTIVE))
                .isNotPresent();
    }

    @Test
    void testSearchByCompanyIdAndStatus() {
        Page<Customer> allActive = customerRepository.searchByCompanyIdAndStatus(
                2L,
                CustomerStatus.ACTIVE,
                null,
                PageRequest.of(0, 10, Sort.by("displayName"))
        );
        assertThat(allActive.getTotalElements()).isEqualTo(4);

        Page<Customer> filtered = customerRepository.searchByCompanyIdAndStatus(
                2L,
                CustomerStatus.ACTIVE,
                "alfa",
                PageRequest.of(0, 10, Sort.by("displayName"))
        );
        assertThat(filtered.getTotalElements()).isEqualTo(1);
        assertThat(filtered.getContent()).extracting(Customer::getDisplayName).containsExactly("Alfa Uno");
    }
}