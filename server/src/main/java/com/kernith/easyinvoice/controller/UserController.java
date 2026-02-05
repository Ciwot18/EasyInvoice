package com.kernith.easyinvoice.controller;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.dto.user.CreateBackofficeUserRequest;
import com.kernith.easyinvoice.data.dto.user.ProfileResponse;
import com.kernith.easyinvoice.data.dto.user.UserSummary;
import com.kernith.easyinvoice.data.model.User;
import com.kernith.easyinvoice.helper.CurrentUser;
import com.kernith.easyinvoice.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * User endpoints for company managers and back office users.
 */
@RestController
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	/**
	 * Creates a back office user for the current company.
	 *
	 * @param request user creation payload
	 * @param principal authenticated principal
	 * @return created user summary or {@code 404 Not Found} if current user missing
	 * @throws org.springframework.web.server.ResponseStatusException if validation or authorization fails
	 */
	@PostMapping("/manager/backoffice-users")
	public ResponseEntity<UserSummary> createBackofficeUser(
			@Valid @RequestBody CreateBackofficeUserRequest request,
            @CurrentUser AuthPrincipal principal
	) {
		User newUser = userService.createBackofficeUser(request, principal);
        if (newUser != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(UserSummary.from(newUser));
        } else {
            return ResponseEntity.notFound().build();
        }
	}

	/**
	 * Lists users for the current company.
	 *
	 * @param principal authenticated principal
	 * @return list of user summaries or {@code 204 No Content} if empty
	 * @throws org.springframework.web.server.ResponseStatusException if authorization fails
	 */
	@GetMapping("/manager/users")
	public ResponseEntity<List<UserSummary>> listCompanyUsers(@CurrentUser AuthPrincipal principal) {
		List<User> users = userService.listCompanyUsers(principal);
		if (users.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(users.stream().map(UserSummary::from).toList());
	}

	/**
	 * Disables a user in the current company.
	 *
	 * @param userId target user identifier
	 * @param principal authenticated principal
	 * @return {@code 204 No Content} on success or {@code 404 Not Found} if missing
	 * @throws org.springframework.web.server.ResponseStatusException if authorization fails
	 */
	@PatchMapping("/manager/users/{userId}/disable")
	public ResponseEntity<Void> disableUser(@PathVariable("userId") Long userId, @CurrentUser AuthPrincipal principal) {
		if (userService.disableUser(userId, principal).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.noContent().build();
	}

	// Back Office

	/**
	 * Returns the profile of the current back office user.
	 *
	 * @param principal authenticated principal
	 * @return profile response or {@code 401 Unauthorized} if missing
	 * @throws org.springframework.web.server.ResponseStatusException if authorization fails
	 */
	@GetMapping("/backoffice/profile")
	public ResponseEntity<ProfileResponse> getBackofficeProfile(@CurrentUser AuthPrincipal principal) {
        Optional<User> optionalUser = userService.getBackofficeProfile(principal);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else {
            return ResponseEntity.ok(ProfileResponse.from(optionalUser.get()));
        }
	}
}
