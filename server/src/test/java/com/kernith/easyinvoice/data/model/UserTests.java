package com.kernith.easyinvoice.data.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTests {

    @Test
    void userDefaultsAndSettersWork() {
        Company company = new Company();
        User user = new User(company);

        assertEquals(company, user.getCompany());
        assertTrue(user.isEnabled());
        assertNull(user.getId());

        user.setEmail("user@acme.test");
        user.setPasswordHash("hash");
        user.setRole(UserRole.BACK_OFFICE);
        user.setEnabled(false);

        assertEquals("user@acme.test", user.getEmail());
        assertEquals("hash", user.getPasswordHash());
        assertEquals(UserRole.BACK_OFFICE, user.getRole());
        assertFalse(user.isEnabled());
        assertNull(user.getCreatedAt());
    }

    @Test
    void userRoleEnumHasExpectedValues() {
        assertNotNull(UserRole.valueOf("PLATFORM_ADMIN"));
        assertNotNull(UserRole.valueOf("COMPANY_MANAGER"));
        assertNotNull(UserRole.valueOf("BACK_OFFICE"));
    }
}
