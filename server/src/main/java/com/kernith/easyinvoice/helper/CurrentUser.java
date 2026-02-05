package com.kernith.easyinvoice.helper;

import java.lang.annotation.*;

/**
 * Marks a controller method parameter that should receive the authenticated {@code AuthPrincipal}.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {}
