package com.ecom.service;

import com.ecom.dto.common.PagedResponse;
import com.ecom.dto.product.ProductRequest;
import com.ecom.dto.product.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    ProductResponse create(ProductRequest request, MultipartFile image);

    ProductResponse update(Long id, ProductRequest request, MultipartFile image);

    void delete(Long id);

    ProductResponse get(Long id);

    ProductResponse getBySlug(String slug);

    PagedResponse<ProductResponse> list(String search, String category, Double minPrice, Double maxPrice, String sort, int page, int size);

    PagedResponse<ProductResponse> listByCategorySlug(String categorySlug, int page, int size);

    List<ProductResponse> getFeatured();

    List<ProductResponse> getRelatedProducts(String categorySlug, Long excludeId, int limit);
}

