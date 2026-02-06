package com.kernith.easyinvoice.data.repository;

import com.kernith.easyinvoice.data.model.User;
import com.kernith.easyinvoice.data.model.UserRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmailIgnoreCase(String email);

	Optional<User> findByCompanyIdAndEmailIgnoreCase(Long companyId, String email);

	List<User> findByCompanyIdOrderByRoleAscEmailAsc(Long companyId);

	List<User> findByCompanyIdAndRoleOrderByEmailAsc(Long companyId, UserRole role);

	List<User> findAllByOrderByCompanyIdAscRoleAscEmailAsc();

	Optional<User> findByIdAndCompanyId(Long id, Long companyId);

	long countByEnabledTrue();

	long countByEnabledFalse();
}
