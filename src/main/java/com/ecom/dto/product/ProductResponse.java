package com.ecom.dto.product;

import com.ecom.dto.category.CategoryResponse;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Value
@Builder
public class ProductResponse {
    Long id;
    String name;
    String slug;
    String description;
    BigDecimal price;
    Integer stock;
    String imageUrl;
    CategoryResponse category;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}

