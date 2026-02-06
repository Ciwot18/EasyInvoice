package com.kernith.easyinvoice.data.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * User entity used for authentication and authorization.
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_users_company_email", columnNames = {"company_id", "email"})
        },
        indexes = {
                @Index(name = "ix_users_company_id", columnList = "company_id"),
                @Index(name = "ix_users_email", columnList = "email")
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user always belongs to a company (including PLATFORM scope via __PLATFORM__ company row).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    /**
     * Used as username for authentication.
     */
    @Column(name = "email", nullable = false, length = 190)
    private String email;

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    /**
     * BCrypt hash
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 32)
    private UserRole role;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    /**
     * Populated by DB default CURRENT_TIMESTAMP.
     * Marked as non-updatable to avoid JPA trying to write it.
     */
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    // --- constructors

    public User() {}
    public User(Company company) {
        this.company = company;
    }

    // --- getters/setters

    public Long getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
