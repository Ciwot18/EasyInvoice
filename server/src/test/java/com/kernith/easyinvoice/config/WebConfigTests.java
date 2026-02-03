package com.kernith.easyinvoice.config;

import com.kernith.easyinvoice.helper.CurrentUserArgumentResolver;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebConfigTests {

    @Test
    void addArgumentResolversAddsCurrentUserResolver() {
        CurrentUserArgumentResolver resolver = new CurrentUserArgumentResolver();
        WebConfig config = new WebConfig(resolver);

        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();
        config.addArgumentResolvers(resolvers);

        assertEquals(1, resolvers.size());
        assertTrue(resolvers.contains(resolver));
    }
}
