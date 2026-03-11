package com.chipcook.api.config.multitenancy;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TenantSubdomainResolverTest {

    private final TenantSubdomainResolver resolver = new TenantSubdomainResolver();

    @Test
    void deveResolverTenantDeSubdominioLocalhost() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Host", "koicarp.localhost:8080");

        assertEquals("koicarp", resolver.resolveTenant(request));
    }

    @Test
    void deveResolverTenantDoOriginQuandoHostForLocalhost() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Host", "localhost:8080");
        request.addHeader("Origin", "http://koicarp.localhost:3000");

        assertEquals("koicarp", resolver.resolveTenant(request));
    }

    @Test
    void deveUsarTenantPadraoQuandoNaoConseguirResolver() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Host", "localhost:8080");

        assertEquals("chipcook", resolver.resolveTenant(request));
    }

    @Test
    void deveUsarHeaderQuandoNaoHouverTenantNoHostNemNoOrigin() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Host", "localhost:8080");
        request.addHeader("X-Tenant-ID", "koicarp");

        assertEquals("koicarp", resolver.resolveTenant(request));
    }
}
