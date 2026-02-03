package com.kernith.easyinvoice.data.repository;

import com.kernith.easyinvoice.data.model.User;
import com.kernith.easyinvoice.data.model.UserRole;
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
public class UserRepositoryTests {
    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByEmailIgnoreCase() {
        assertThat(userRepository.findByEmailIgnoreCase("MANAGER@ALPHA.IT"))
                .isPresent()
                .get()
                .extracting(User::getEmail)
                .isEqualTo("manager@alpha.it");

        assertThat(userRepository.findByEmailIgnoreCase("missing@alpha.it"))
                .isNotPresent();
    }

    @Test
    void testFindByCompanyIdAndEmailIgnoreCase() {
        assertThat(userRepository.findByCompanyIdAndEmailIgnoreCase(2L, "BACKOFFICE@ALPHA.IT"))
                .isPresent()
                .get()
                .extracting(User::getEmail)
                .isEqualTo("backoffice@alpha.it");

        assertThat(userRepository.findByCompanyIdAndEmailIgnoreCase(3L, "backoffice@alpha.it"))
                .isNotPresent();
    }

    @Test
    void testFindByCompanyIdOrderByRoleAscEmailAsc() {
        List<User> users = userRepository.findByCompanyIdOrderByRoleAscEmailAsc(2L);

        assertThat(users).hasSize(3);
        assertThat(users)
                .extracting(User::getEmail)
                .containsExactly(
                        "backoffice@alpha.it",
                        "zeta@alpha.it",
                        "manager@alpha.it"
                );
    }

    @Test
    void testFindByCompanyIdAndRoleOrderByEmailAsc() {
        assertThat(userRepository.findByCompanyIdAndRoleOrderByEmailAsc(2L, UserRole.BACK_OFFICE))
                .extracting(User::getEmail)
                .containsExactly("backoffice@alpha.it", "zeta@alpha.it");

        assertThat(userRepository.findByCompanyIdAndRoleOrderByEmailAsc(3L, UserRole.BACK_OFFICE))
                .extracting(User::getEmail)
                .containsExactly("backoffice@beta.it");
    }

    @Test
    void testFindByIdAndCompanyId() {
        assertThat(userRepository.findByIdAndCompanyId(10L, 2L))
                .isPresent()
                .get()
                .extracting(User::getEmail)
                .isEqualTo("manager@alpha.it");

        assertThat(userRepository.findByIdAndCompanyId(10L, 3L))
                .isNotPresent();
    }
}