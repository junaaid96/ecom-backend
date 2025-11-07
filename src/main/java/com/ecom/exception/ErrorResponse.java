package com.ecom.exception;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.List;

@Value
@Builder
public class ErrorResponse {
    OffsetDateTime timestamp;
    int status;
    String error;
    String message;
    String path;
    List<String> details;
}

