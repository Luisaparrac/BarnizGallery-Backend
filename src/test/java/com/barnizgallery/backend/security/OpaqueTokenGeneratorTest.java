package com.barnizgallery.backend.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class OpaqueTokenGeneratorTest {

    private final OpaqueTokenGenerator generator = new OpaqueTokenGenerator();

    @Test
    void tokensAreRandomAndUrlSafe() {
        Set<String> tokens = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            tokens.add(generator.generate());
        }

        assertThat(tokens).hasSize(100).allSatisfy(token -> assertThat(token).matches("[A-Za-z0-9_-]{43}"));
    }

    @Test
    void hashIsSha256HexAndNeverTheToken() {
        String token = generator.generate();

        assertThat(generator.hash(token)).matches("[0-9a-f]{64}").isNotEqualTo(token)
                .isEqualTo(generator.hash(token));
        assertThat(generator.hash("abc"))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }
}
