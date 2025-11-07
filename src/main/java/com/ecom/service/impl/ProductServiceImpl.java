package com.ecom.service.impl;

import com.ecom.dto.common.PagedResponse;
import com.ecom.dto.product.ProductRequest;
import com.ecom.dto.product.ProductResponse;
import com.ecom.entity.Category;
import com.ecom.entity.Product;
import com.ecom.exception.BadRequestException;
import com.ecom.exception.ResourceNotFoundException;
import com.ecom.mapper.ProductMapper;
import com.ecom.repository.CategoryRepository;
import com.ecom.repository.ProductRepository;
import com.ecom.service.FileStorageService;
import com.ecom.service.ProductService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request, MultipartFile image) {
        validateSlugUniqueness(null, request.getSlug());
        Category category = getCategory(request.getCategoryId());
        Product product = ProductMapper.fromRequest(request, category);
        if (image != null && !image.isEmpty()) {
            product.setImageUrl(fileStorageService.storeFile(image));
        }
        Product saved = productRepository.save(product);
        return ProductMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request, MultipartFile image) {
        Product product = getProduct(id);
        validateSlugUniqueness(id, request.getSlug());
        Category category = getCategory(request.getCategoryId());

        if (image != null && !image.isEmpty()) {
            if (product.getImageUrl() != null) {
                fileStorageService.deleteFile(product.getImageUrl());
            }
            String storedPath = fileStorageService.storeFile(image);
            product.setImageUrl(storedPath);
        }

        ProductMapper.update(product, request, category);
        Product saved = productRepository.save(product);
        return ProductMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = getProduct(id);
        if (product.getImageUrl() != null) {
            fileStorageService.deleteFile(product.getImageUrl());
        }
        productRepository.delete(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return ProductMapper.toResponse(getProduct(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + slug));
        return ProductMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> list(String search, String category, Double minPrice, Double maxPrice, String sort, int page, int size) {
        // Parse sort parameter (format: "field,direction" e.g., "price,asc" or "createdAt,desc")
        Sort sortObj = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        
        // Build specification for filtering
        Specification<Product> spec = buildSpecification(search, category, minPrice, maxPrice);
        
        // Use specification to query products
        Page<Product> result = productRepository.findAll(spec, pageable);

        return PagedResponse.<ProductResponse>builder()
                .content(result.getContent().stream().map(ProductMapper::toResponse).toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();
    }
    
    private Specification<Product> buildSpecification(String search, String category, Double minPrice, Double maxPrice) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Category filter (case-insensitive slug matching)
            if (category != null && !category.isBlank()) {
                Join<Product, Category> categoryJoin = root.join("category", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(
                    criteriaBuilder.lower(categoryJoin.get("slug")), 
                    category.toLowerCase().trim()
                ));
            }
            
            // Price range filters
            if (minPrice != null && minPrice > 0) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("price"), 
                    BigDecimal.valueOf(minPrice)
                ));
            }
            if (maxPrice != null && maxPrice > 0) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("price"), 
                    BigDecimal.valueOf(maxPrice)
                ));
            }
            
            // Search filter (search by name)
            if (search != null && !search.isBlank()) {
                String searchPattern = "%" + search.toLowerCase().trim() + "%";
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")), 
                    searchPattern
                ));
            }
            
            // If no predicates, return all products (no filtering)
            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        
        String[] parts = sort.split(",");
        if (parts.length != 2) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        
        String field = parts[0].trim();
        String direction = parts[1].trim().toUpperCase();
        
        // Validate field names
        if (!isValidSortField(field)) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        
        Sort.Direction sortDirection = direction.equals("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(sortDirection, field);
    }
    
    private boolean isValidSortField(String field) {
        // Only allow sorting by these fields for security
        return field.equals("name") || 
               field.equals("price") || 
               field.equals("createdAt") || 
               field.equals("updatedAt");
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> listByCategorySlug(String categorySlug, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        Page<Product> result = productRepository.findByCategorySlug(categorySlug, pageable);

        return PagedResponse.<ProductResponse>builder()
                .content(result.getContent().stream().map(ProductMapper::toResponse).toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getFeatured() {
        // Return featured products: in-stock products, limited to 12, ordered by name
        Pageable pageable = PageRequest.of(0, 12, Sort.by(Sort.Direction.ASC, "name"));
        Page<Product> products = productRepository.findAll(pageable);
        return products.getContent().stream()
                .filter(product -> product.getStock() > 0)
                .limit(12)
                .map(ProductMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getRelatedProducts(String categorySlug, Long excludeId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Product> products = productRepository.findRelatedProductsByCategorySlug(categorySlug, excludeId, pageable);
        return products.stream()
                .map(ProductMapper::toResponse)
                .toList();
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
    }

    private void validateSlugUniqueness(Long id, String slug) {
        Optional<Product> existing = productRepository.findBySlug(slug);
        if (existing.isPresent() && (id == null || !existing.get().getId().equals(id))) {
            throw new BadRequestException("Product slug already exists");
        }
    }
}

