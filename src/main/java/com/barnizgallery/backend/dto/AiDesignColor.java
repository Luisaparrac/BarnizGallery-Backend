package com.barnizgallery.backend.dto;

/** A color sent to {@code POST /v1/designs}: its Spanish name and its {@code #RRGGBB} value. */
public record AiDesignColor(String name, String hex) {
}
