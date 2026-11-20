package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

class ImageUploadValidatorTest {

    private static final byte[] JPEG = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0x10 };
    private static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 0x0D };
    private static final byte[] WEBP = { 'R', 'I', 'F', 'F', 1, 2, 3, 4, 'W', 'E', 'B', 'P', 'V', 'P' };

    private final ImageUploadValidator validator = new ImageUploadValidator(DataSize.ofKilobytes(1));

    @Test
    void detectsTheRealTypeFromTheFirstBytes() {
        assertThat(validator.validate(file("a.jpg", "image/jpeg", JPEG))).isEqualTo("image/jpeg");
        assertThat(validator.validate(file("a.png", "image/png", PNG))).isEqualTo("image/png");
        assertThat(validator.validate(file("a.webp", "image/webp", WEBP))).isEqualTo("image/webp");
    }

    @Test
    void rejectsFilesThatOnlyPretendToBeImages() {
        byte[] script = "<script>alert(1)</script>".getBytes();

        assertThatThrownBy(() -> validator.validate(file("evil.jpg", "image/jpeg", script)))
                .hasMessage("image_type_not_allowed");
        assertThatThrownBy(() -> validator.validate(file("anim.gif", "image/gif", "GIF89a....".getBytes())))
                .hasMessage("image_type_not_allowed");
    }

    @Test
    void rejectsEmptyAndTooLargeFiles() {
        assertThatThrownBy(() -> validator.validate(null)).hasMessage("image_required");
        assertThatThrownBy(() -> validator.validate(file("a.jpg", "image/jpeg", new byte[0])))
                .hasMessage("image_required");

        byte[] big = new byte[2048];
        System.arraycopy(JPEG, 0, big, 0, JPEG.length);
        assertThatThrownBy(() -> validator.validate(file("big.jpg", "image/jpeg", big)))
                .hasMessageStartingWith("image_too_large");
    }

    private static MockMultipartFile file(String name, String type, byte[] content) {
        return new MockMultipartFile("file", name, type, content);
    }
}
