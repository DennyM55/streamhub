package com.dennymathew.catalog.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogWriteAuthorizationFilterTest {
    @Test
    void allMutationMethodsRejectMissingAndWrongKeys() throws Exception {
        for (String method : new String[]{"POST", "PUT", "PATCH", "DELETE"}) {
            for (String key : new String[]{"", "wrong"}) {
                MockHttpServletRequest request = new MockHttpServletRequest(method, "/movies/1");
                if (!key.isEmpty()) request.addHeader("X-Catalog-Key", key);
                MockHttpServletResponse response = new MockHttpServletResponse();
                MockFilterChain chain = new MockFilterChain();

                new CatalogWriteAuthorizationFilter("test-secret").doFilter(request, response, chain);

                assertThat(response.getStatus()).isEqualTo(403);
                assertThat(chain.getRequest()).isNull();
            }
        }
    }

    @Test
    void anUnconfiguredKeyFailsClosedEvenForAnEmptyHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/movies");
        request.addHeader("X-Catalog-Key", "");
        MockHttpServletResponse response = new MockHttpServletResponse();
        new CatalogWriteAuthorizationFilter("").doFilter(request, response, new MockFilterChain());
        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void matchingKeyAllowsWrite() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/movies/1");
        request.addHeader("X-Catalog-Key", "test-secret");
        MockFilterChain chain = new MockFilterChain();
        new CatalogWriteAuthorizationFilter("test-secret").doFilter(request, new MockHttpServletResponse(), chain);
        assertThat(chain.getRequest()).isSameAs(request);
    }

    @Test
    void readsRemainPublicWhenWritesAreDisabled() throws Exception {
        for (String method : new String[]{"GET", "HEAD", "OPTIONS"}) {
            MockHttpServletRequest request = new MockHttpServletRequest(method, "/movies");
            MockFilterChain chain = new MockFilterChain();
            new CatalogWriteAuthorizationFilter("").doFilter(request, new MockHttpServletResponse(), chain);
            assertThat(chain.getRequest()).isSameAs(request);
        }
    }
}
