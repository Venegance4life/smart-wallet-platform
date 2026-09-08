package com.smartwallet.service;

import com.smartwallet.dto.CategoryDto.*;
import com.smartwallet.entity.Category;
import com.smartwallet.entity.User;
import com.smartwallet.exception.BadRequestException;
import com.smartwallet.exception.ResourceNotFoundException;
import com.smartwallet.repository.CategoryRepository;
import com.smartwallet.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final CurrentUserProvider currentUserProvider;

    public List<CategoryResponse> getMyCategories() {
        User user = currentUserProvider.getCurrentUser();
        return categoryRepository.findByUserIsNullOrUserId(user.getId())
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        User user = currentUserProvider.getCurrentUser();
        if (request.name() == null || request.name().isBlank()) {
            throw new BadRequestException("Category name is required");
        }
        Category category = Category.builder()
                .name(request.name().trim())
                .type(request.type())
                .icon(request.icon() != null && !request.icon().isBlank() ? request.icon() : "tag")
                .user(user)
                .build();
        return toDto(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        User user = currentUserProvider.getCurrentUser();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (category.getUser() == null) {
            throw new BadRequestException("System categories cannot be deleted");
        }
        if (!category.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Category not found");
        }
        if (transactionRepository.existsByCategoryId(id)) {
            throw new BadRequestException("Cannot delete category that is used by existing transactions. Reassign or delete those transactions first.");
        }
        categoryRepository.delete(category);
    }

    private CategoryResponse toDto(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getType(), c.getIcon(), c.getUser() == null);
    }
}
