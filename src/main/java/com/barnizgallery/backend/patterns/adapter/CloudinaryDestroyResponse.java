package com.barnizgallery.backend.patterns.adapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Answer of a Cloudinary destroy call: {@code ok}, or {@code not found} when the image was already gone.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CloudinaryDestroyResponse(String result) {
}
