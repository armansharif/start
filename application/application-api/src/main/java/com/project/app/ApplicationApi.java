package com.project.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Single executable entry point of the platform.
 *
 * <p>{@code scanBasePackages} is the root package on purpose: every common module and
 * every business module contributes beans through its own configuration classes, and no
 * module may define another {@code @SpringBootApplication}.</p>
 */
@SpringBootApplication(scanBasePackages = "com.project")
public class ApplicationApi {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationApi.class, args);
    }
}
