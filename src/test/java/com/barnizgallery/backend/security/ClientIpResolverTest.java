package com.barnizgallery.backend.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class ClientIpResolverTest {

    @Test
    void withoutTrustedProxiesTheHeaderIsIgnored() {
        assertThat(new ClientIpResolver(0).resolve(request("10.0.0.5", "1.2.3.4"))).isEqualTo("10.0.0.5");
    }

    @Test
    void behindOneProxyTheLastEntryIsTheClient() {
        ClientIpResolver resolver = new ClientIpResolver(1);

        assertThat(resolver.resolve(request("10.0.0.5", "181.50.20.7"))).isEqualTo("181.50.20.7");
    }

    @Test
    void aForgedEntryWrittenByTheClientIsNotTrusted() {
        ClientIpResolver resolver = new ClientIpResolver(1);

        // The client sent "X-Forwarded-For: 9.9.9.9"; the proxy appended the real address.
        assertThat(resolver.resolve(request("10.0.0.5", "9.9.9.9, 181.50.20.7"))).isEqualTo("181.50.20.7");
    }

    @Test
    void twoTrustedProxiesSkipTheInnerOne() {
        assertThat(new ClientIpResolver(2).resolve(request("10.0.0.5", "9.9.9.9, 181.50.20.7, 172.16.0.2")))
                .isEqualTo("181.50.20.7");
    }

    @Test
    void missingOrShortHeaderFallsBackToTheTcpAddress() {
        ClientIpResolver resolver = new ClientIpResolver(2);

        assertThat(resolver.resolve(request("10.0.0.5", null))).isEqualTo("10.0.0.5");
        assertThat(resolver.resolve(request("10.0.0.5", "181.50.20.7"))).isEqualTo("10.0.0.5");
    }

    private static MockHttpServletRequest request(String remoteAddr, String forwardedFor) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(remoteAddr);
        if (forwardedFor != null) {
            request.addHeader(ClientIpResolver.FORWARDED_FOR, forwardedFor);
        }
        return request;
    }
}
