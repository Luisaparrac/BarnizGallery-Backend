package com.barnizgallery.backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;

/**
 * Checks an uploaded image before it is sent to the storage: it must not be empty, must fit in
 * {@code app.storage.max-upload-size} and must really be a JPEG, PNG or WebP file. The type is read from the
 * first bytes of the file, not from the name or the {@code Content-Type} the client sends.
 */
@Component
public class ImageUploadValidator {

    private static final byte[] JPEG = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF };
    private static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n' };
    private static final byte[] RIFF = "RIFF".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] WEBP = "WEBP".getBytes(StandardCharsets.US_ASCII);
    private static final int HEADER_BYTES = 12;

    private final DataSize maxSize;

    public ImageUploadValidator(@Value("${app.storage.max-upload-size}") DataSize maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * @return the real MIME type of the image ({@code image/jpeg}, {@code image/png} or {@code image/webp})
     */
    public String validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw BusinessRuleException.unprocessable(ErrorCode.IMAGE_REQUIRED);
        }
        if (file.getSize() > maxSize.toBytes()) {
            throw BusinessRuleException.unprocessable(ErrorCode.IMAGE_TOO_LARGE, maxSize.toMegabytes());
        }
        return detectType(header(file));
    }

    static String detectType(byte[] header) {
        if (startsWith(header, JPEG, 0)) {
            return "image/jpeg";
        }
        if (startsWith(header, PNG, 0)) {
            return "image/png";
        }
        if (startsWith(header, RIFF, 0) && startsWith(header, WEBP, 8)) {
            return "image/webp";
        }
        throw BusinessRuleException.unprocessable(ErrorCode.IMAGE_TYPE_NOT_ALLOWED);
    }

    private static byte[] header(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            return in.readNBytes(HEADER_BYTES);
        } catch (IOException ex) {
            throw BusinessRuleException.unprocessable(ErrorCode.IMAGE_REQUIRED);
        }
    }

    private static boolean startsWith(byte[] data, byte[] prefix, int offset) {
        return data.length >= offset + prefix.length
                && Arrays.equals(data, offset, offset + prefix.length, prefix, 0, prefix.length);
    }
}
