package com.kernith.easyinvoice.data.repository;

import com.kernith.easyinvoice.data.model.Company;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
public class CompanyRepositoryTests {

    @Autowired
    private CompanyRepository companyRepository;

    @Test
    void testFindAllByOrderByNameAsc() {
        List<Company> companies = companyRepository.findAllByOrderByNameAsc();
        List<String> names = companies.stream().map(Company::getName).toList();

        List<String> sorted = new ArrayList<>(names);
        sorted.sort(String::compareTo);

        assertThat(names).contains("Alpha SRL", "Beta SPA");
        assertThat(names).isEqualTo(sorted);
    }
}
