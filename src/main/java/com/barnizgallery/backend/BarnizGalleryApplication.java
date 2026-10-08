package com.barnizgallery.backend;

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
        SpringApplication.run(BarnizGalleryApplication.class, args);
    }
}
