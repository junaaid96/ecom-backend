package com.ecom;

import com.ecom.config.CorsProperties;
import com.ecom.config.FileStorageProperties;
import com.ecom.config.ImgBBProperties;
import com.ecom.config.JwtProperties;
import com.ecom.config.StripeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, FileStorageProperties.class, CorsProperties.class, ImgBBProperties.class, StripeProperties.class})
public class EcomBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcomBackendApplication.class, args);
    }
}

