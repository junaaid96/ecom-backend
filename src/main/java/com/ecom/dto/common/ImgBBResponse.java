package com.ecom.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImgBBResponse {

    private UploadData data;
    private boolean success;
    private int status;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UploadData {
        private String id;
        private String title;
        @JsonProperty("url_viewer")
        private String urlViewer;
        private String url;
        @JsonProperty("display_url")
        private String displayUrl;
        private int width;
        private int height;
        private int size;
        private long time;
        private String expiration;
        private ImageData image;
        private ThumbData thumb;
        @JsonProperty("medium")
        private MediumData mediumSize;
        private String deleteUrl;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageData {
        private String filename;
        private String name;
        private String mime;
        private String extension;
        private String url;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ThumbData {
        private String filename;
        private String name;
        private String mime;
        private String extension;
        private String url;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MediumData {
        private String filename;
        private String name;
        private String mime;
        private String extension;
        private String url;
    }
}

