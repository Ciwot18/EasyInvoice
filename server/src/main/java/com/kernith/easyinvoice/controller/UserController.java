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

@RestController
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

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

	@GetMapping("/manager/users")
	public ResponseEntity<List<UserSummary>> listCompanyUsers(@CurrentUser AuthPrincipal principal) {
		List<User> users = userService.listCompanyUsers(principal);
		if (users.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(users.stream().map(UserSummary::from).toList());
	}

	@PatchMapping("/manager/users/{userId}/disable")
	public ResponseEntity<Void> disableUser(@PathVariable("userId") Long userId, @CurrentUser AuthPrincipal principal) {
		if (userService.disableUser(userId, principal).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.noContent().build();
	}

	// Back Office

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
