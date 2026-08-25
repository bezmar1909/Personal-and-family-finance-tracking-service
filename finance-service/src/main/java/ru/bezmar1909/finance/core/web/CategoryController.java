package ru.bezmar1909.finance.core.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.bezmar1909.finance.core.service.CategoryService;
import ru.bezmar1909.finance.core.web.dto.CategoryResponse;
import ru.bezmar1909.finance.core.web.dto.CreateCategoryRequest;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CreateCategoryRequest request, Authentication authentication) {
        return CategoryResponse.from(categoryService.create(
                request.name(),
                request.type(),
                request.groupId(),
                CurrentUser.id(authentication)
        ));
    }

    @GetMapping
    public List<CategoryResponse> list(@RequestParam(required = false) Long groupId, Authentication authentication) {
        return categoryService.list(groupId, CurrentUser.id(authentication)).stream()
                .map(CategoryResponse::from)
                .toList();
    }
}
