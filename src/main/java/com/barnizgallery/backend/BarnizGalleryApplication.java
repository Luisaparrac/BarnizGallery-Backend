package com.barnizgallery.backend;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point of the Barniz Gallery backend.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class BarnizGalleryApplication {

    public static void main(String[] args) {
        // The database stores timestamps in UTC; use UTC everywhere so that "now"
        // means the same on a developer machine (UTC-5) and on Render.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(BarnizGalleryApplication.class, args);
    }
}
