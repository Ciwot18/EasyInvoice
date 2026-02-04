package com.kernith.easyinvoice.helper;

import com.kernith.easyinvoice.config.AuthPrincipal;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CurrentUserArgumentResolverTests {

    private final CurrentUserArgumentResolver resolver = new CurrentUserArgumentResolver();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void supportsParameterDetectsCurrentUserAuthPrincipal() throws Exception {
        Method method = TestController.class.getDeclaredMethod("handle", AuthPrincipal.class, String.class);
        MethodParameter currentUserParam = new MethodParameter(method, 0);
        MethodParameter otherParam = new MethodParameter(method, 1);

        assertTrue(resolver.supportsParameter(currentUserParam));
        assertFalse(resolver.supportsParameter(otherParam));
    }

    @Test
    void resolveArgumentReturnsPrincipalWhenAuthenticated() throws Exception {
        AuthPrincipal principal = new AuthPrincipal(1L, 2L, "COMPANY_MANAGER", List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.authorities())
        );

        Method method = TestController.class.getDeclaredMethod("handle", AuthPrincipal.class, String.class);
        MethodParameter currentUserParam = new MethodParameter(method, 0);

        Object resolved = resolver.resolveArgument(currentUserParam, null, null, null);
        assertEquals(principal, resolved);
    }

    @Test
    void resolveArgumentReturnsNullWhenNoAuth() throws Exception {
        Method method = TestController.class.getDeclaredMethod("handle", AuthPrincipal.class, String.class);
        MethodParameter currentUserParam = new MethodParameter(method, 0);

        Object resolved = resolver.resolveArgument(currentUserParam, null, null, null);
        assertNull(resolved);
    }

    @Test
    void resolveArgumentReturnsNullWhenPrincipalNotAuthPrincipal() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", null, List.of())
        );

        Method method = TestController.class.getDeclaredMethod("handle", AuthPrincipal.class, String.class);
        MethodParameter currentUserParam = new MethodParameter(method, 0);

        Object resolved = resolver.resolveArgument(currentUserParam, null, null, null);
        assertNull(resolved);
    }

    private static class TestController {
        void handle(@CurrentUser AuthPrincipal principal, String other) {
        }
    }
}
