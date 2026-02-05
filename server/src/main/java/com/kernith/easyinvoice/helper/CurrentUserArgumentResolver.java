package com.kernith.easyinvoice.helper;

import com.kernith.easyinvoice.config.AuthPrincipal;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolves {@link CurrentUser} parameters to the authenticated {@link AuthPrincipal}.
 */
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * Supports parameters annotated with {@link CurrentUser} and assignable to {@link AuthPrincipal}.
     *
     * @param parameter method parameter to check
     * @return {@code true} if supported
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && AuthPrincipal.class.isAssignableFrom(parameter.getParameterType());
    }

    /**
     * Returns the authenticated principal or {@code null} if not available.
     *
     * @param parameter method parameter
     * @param mavContainer model/view container
     * @param webRequest current request
     * @param binderFactory binder factory
     * @return resolved principal or {@code null}
     */
    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthPrincipal principal)) {
            return null;
        }
        return principal;
    }
}
