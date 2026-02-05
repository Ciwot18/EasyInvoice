package com.kernith.easyinvoice.config;

import com.kernith.easyinvoice.helper.CurrentUserArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * MVC configuration that registers custom method argument resolvers.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final CurrentUserArgumentResolver resolver;

    /**
     * Creates the configuration with the {@link CurrentUserArgumentResolver}.
     *
     * @param resolver resolver used to inject the current user into controller methods
     */
    public WebConfig(CurrentUserArgumentResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * Adds the {@link CurrentUserArgumentResolver} to Spring MVC.
     *
     * @param resolvers list of resolvers to extend
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(resolver);
    }
}
