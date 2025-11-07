package com.ecom.service.impl;

import com.ecom.config.ImgBBProperties;
import com.ecom.dto.common.ImgBBResponse;
import com.ecom.exception.BadRequestException;
import com.ecom.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@Slf4j
@Primary
@Service
@RequiredArgsConstructor
public class ImgBBService implements FileStorageService {

    private final ImgBBProperties imgBBProperties;
    private final RestTemplate restTemplate;

    @Override
    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File must not be empty");
        }

        try {
            // Validate file size (ImgBB allows up to 32MB for free tier)
            if (file.getSize() > 32 * 1024 * 1024) {
                throw new BadRequestException("File size exceeds 32MB limit");
            }

            // Convert file to base64
            byte[] fileBytes = file.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(fileBytes);

            // Prepare request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("key", imgBBProperties.getApiKey());
            body.add("image", base64Image);

            // Optional: Add filename for better organization
            if (file.getOriginalFilename() != null && !file.getOriginalFilename().isEmpty()) {
                body.add("name", file.getOriginalFilename());
            }

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

            // Make API call
            ResponseEntity<ImgBBResponse> response = restTemplate.exchange(
                    imgBBProperties.getUploadUrl(),
                    HttpMethod.POST,
                    requestEntity,
                    ImgBBResponse.class
            );

            // Process response
            ImgBBResponse imgBBResponse = response.getBody();
            if (imgBBResponse == null || !imgBBResponse.isSuccess() || imgBBResponse.getData() == null) {
                log.error("ImgBB upload failed: {}", response.getBody());
                throw new IllegalStateException("Failed to upload image to ImgBB");
            }

            // Return the display URL (direct image link)
            String imageUrl = imgBBResponse.getData().getUrl();
            if (imageUrl == null || imageUrl.isEmpty()) {
                imageUrl = imgBBResponse.getData().getDisplayUrl();
            }

            log.info("Image uploaded successfully to ImgBB: {}", imageUrl);
            return imageUrl;

        } catch (IOException e) {
            log.error("Error reading file", e);
            throw new IllegalStateException("Failed to read file", e);
        } catch (RestClientException e) {
            log.error("Error uploading to ImgBB", e);
            throw new IllegalStateException("Failed to upload image to ImgBB: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String imageUrl) {
        // ImgBB free tier doesn't provide a delete API
        // The images are automatically deleted after expiration (if set)
        // For now, we'll just log the deletion request
        if (imageUrl != null && !imageUrl.isBlank()) {
            log.info("Delete requested for ImgBB image: {}. Note: ImgBB free tier doesn't support programmatic deletion.", imageUrl);
        }
    }
}

