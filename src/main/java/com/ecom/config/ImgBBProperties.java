package com.ecom.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "application.imgbb")
public class ImgBBProperties {

    @NotBlank
    private String apiKey;

    private String uploadUrl = "https://api.imgbb.com/1/upload";
}

