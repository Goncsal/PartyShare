package tqs.backend.tqsbackend.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tqs.backend.tqsbackend.entity.Category;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.repository.CategoryRepository;
import tqs.backend.tqsbackend.repository.ItemRepository;

import java.util.Arrays;

@Service
public class MockAddingItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PostConstruct
    public void init() {
        if (itemRepository.count() == 0) {
            Category electronics = new Category("Electronics");
            Category lighting = new Category("Lighting");
            Category furniture = new Category("Furniture");

            categoryRepository.saveAll(Arrays.asList(electronics, lighting, furniture));

            itemRepository.save(new Item("Vintage Lamp", "A beautiful vintage lamp", 55.0, lighting, 4.5, "Lisbon"));
            itemRepository.save(new Item("Modern Desk", "Sleek modern desk", 150.0, furniture, 4.0, "Porto"));
            itemRepository
                    .save(new Item("Gaming Laptop", "High performance laptop", 1200.0, electronics, 4.8, "Lisbon"));
            itemRepository.save(new Item("Chandelier", "Crystal chandelier", 250.0, lighting, 4.9, "Porto"));
            itemRepository.save(new Item("Office Chair", "Ergonomic chair", 80.0, furniture, 3.5, "Lisbon"));
            itemRepository.save(new Item("Bluetooth Speaker", "Portable speaker", 40.0, electronics, 4.2, "Porto"));
            itemRepository.save(new Item("Floor Lamp", "Minimalist floor lamp", 60.0, lighting, 3.8, "Lisbon"));
            itemRepository.save(new Item("Bookshelf", "Wooden bookshelf", 90.0, furniture, 4.1, "Porto"));
            itemRepository.save(new Item("Smart Watch", "Fitness tracker", 120.0, electronics, 4.6, "Lisbon"));
            itemRepository.save(new Item("Table Lamp", "Small table lamp", 25.0, lighting, 3.0, "Porto"));
        }
    }
}
