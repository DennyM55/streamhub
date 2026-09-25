package com.dennymathew.streamhub.security;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
class JwtServiceTest {
    @Test void issuedTokenIdentifiesUserAndRejectsWrongSigningKey() {
        var issuer=new JwtService("a-local-test-secret-with-at-least-32-characters");
        var token=issuer.generateToken("denny@example.com");
        assertThat(issuer.extractEmail(token)).isEqualTo("denny@example.com");
        var other=new JwtService("another-test-secret-with-at-least-32-characters");
        assertThatThrownBy(() -> other.extractEmail(token)).isInstanceOf(io.jsonwebtoken.JwtException.class);
    }
    @Test void rejectsShortKeysAtStartup() {
        assertThatThrownBy(() -> new JwtService("short")).isInstanceOf(io.jsonwebtoken.security.WeakKeyException.class);
    }
}
