package com.barnizgallery.backend.patterns.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class CloudinarySignerTest {

    /** The example of the Cloudinary "authentication signatures" documentation (secret "abcd"). */
    @Test
    void matchesTheDocumentedExample() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("timestamp", "1315060510");
        params.put("public_id", "sample_image");
        params.put("eager", "w_400,h_300,c_pad|w_260,h_200,c_crop");

        assertThat(new CloudinarySigner("abcd").sign(params)).isEqualTo("bfd09f95f331f558cbd1320e67aa8d488770583e");
    }

    @Test
    void ignoresUnsignedAndEmptyParameters() {
        CloudinarySigner signer = new CloudinarySigner("secret");
        Map<String, String> signed = Map.of("public_id", "x", "timestamp", "1");
        Map<String, String> withExtras = new LinkedHashMap<>(signed);
        withExtras.put("api_key", "123");
        withExtras.put("file", "data");
        withExtras.put("cloud_name", "demo");
        withExtras.put("resource_type", "image");
        withExtras.put("folder", "");

        assertThat(signer.sign(withExtras)).isEqualTo(signer.sign(signed));
        assertThat(new CloudinarySigner("other").sign(signed)).isNotEqualTo(signer.sign(signed));
    }
}
