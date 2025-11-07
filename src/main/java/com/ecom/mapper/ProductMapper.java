package com.ecom.mapper;

import com.ecom.dto.category.CategoryResponse;
import com.ecom.dto.product.ProductRequest;
import com.ecom.dto.product.ProductResponse;
import com.ecom.entity.Category;
import com.ecom.entity.Product;

public final class ProductMapper {

    private ProductMapper() {
    }

    public static ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }
        Category category = product.getCategory();
        CategoryResponse categoryResponse = category == null ? null : CategoryMapper.toResponse(category);

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .category(categoryResponse)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public static Product fromRequest(ProductRequest request, Category category) {
        return Product.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .category(category)
                .build();
    }

    public static void update(Product product, ProductRequest request, Category category) {
        product.setName(request.getName());
        product.setSlug(request.getSlug());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(category);
    }
}

