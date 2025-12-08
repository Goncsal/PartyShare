package tqs.backend.tqsbackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tqs.backend.tqsbackend.entity.Category;
import tqs.backend.tqsbackend.repository.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }
}
