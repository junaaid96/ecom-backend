package com.ecom.service;

import com.ecom.dto.category.CategoryRequest;
import com.ecom.dto.category.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse create(CategoryRequest request);

    CategoryResponse update(Long id, CategoryRequest request);

    void delete(Long id);

    CategoryResponse get(Long id);

    CategoryResponse getBySlug(String slug);

    List<CategoryResponse> list();
}

