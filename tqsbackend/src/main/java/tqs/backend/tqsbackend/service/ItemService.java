package tqs.backend.tqsbackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import tqs.backend.tqsbackend.entity.Category;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.repository.ItemRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public List<Item> searchItems(String keyword, Category category, Double minPrice, Double maxPrice, Double minRating,
            String location) {
        Specification<Item> spec = (root, query, cb) -> cb.conjunction();

        if (keyword != null && !keyword.isEmpty()) {
            String likePattern = "%" + keyword.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), likePattern),
                    cb.like(cb.lower(root.get("description")), likePattern)));
        }

        if (category != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), category));
        }

        if (minPrice != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }

        if (maxPrice != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }

        if (minRating != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("averageRating"), minRating));
        }

        if (location != null && !location.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("location"), location));
        }

        return itemRepository.findAll(spec);
    }

    public Item getItemById(Long id) {
        return itemRepository.findById(id).orElse(null);
    }

    public Item saveItem(Item item) {
        return itemRepository.save(item);
    }
}
