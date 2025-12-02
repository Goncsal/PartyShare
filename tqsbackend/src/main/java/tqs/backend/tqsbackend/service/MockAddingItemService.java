package tqs.backend.tqsbackend.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tqs.backend.tqsbackend.entity.Category;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.repository.CategoryRepository;
import tqs.backend.tqsbackend.repository.ItemRepository;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class MockAddingItemService {

    private final ItemRepository itemRepository;

    private final CategoryRepository categoryRepository;

    @PostConstruct
    public void init() {
        if (itemRepository.count() == 0) {
            Category electronics = new Category("Electronics");
            Category lighting = new Category("Lighting");
            Category furniture = new Category("Furniture");

            categoryRepository.saveAll(Arrays.asList(electronics, lighting, furniture));

            Item vintageLamp = new Item("Vintage Lamp", "A beautiful vintage lamp", 55.0, lighting, 4.5, "Lisbon");
            vintageLamp.setOwnerId(100L);
            itemRepository.save(vintageLamp);

            Item modernDesk = new Item("Modern Desk", "Sleek modern desk", 150.0, furniture, 4.0, "Porto");
            modernDesk.setOwnerId(101L);
            itemRepository.save(modernDesk);

            Item gamingLaptop = new Item("Gaming Laptop", "High performance laptop", 1200.0, electronics, 4.8,
                    "Lisbon");
            gamingLaptop.setOwnerId(102L);
            itemRepository.save(gamingLaptop);

            Item chandelier = new Item("Chandelier", "Crystal chandelier", 250.0, lighting, 4.9, "Porto");
            chandelier.setOwnerId(103L);
            itemRepository.save(chandelier);

            Item officeChair = new Item("Office Chair", "Ergonomic chair", 80.0, furniture, 3.5, "Lisbon");
            officeChair.setOwnerId(104L);
            itemRepository.save(officeChair);

            Item speaker = new Item("Bluetooth Speaker", "Portable speaker", 40.0, electronics, 4.2, "Porto");
            speaker.setOwnerId(105L);
            itemRepository.save(speaker);

            Item floorLamp = new Item("Floor Lamp", "Minimalist floor lamp", 60.0, lighting, 3.8, "Lisbon");
            floorLamp.setOwnerId(106L);
            itemRepository.save(floorLamp);

            Item bookshelf = new Item("Bookshelf", "Wooden bookshelf", 90.0, furniture, 4.1, "Porto");
            bookshelf.setOwnerId(107L);
            itemRepository.save(bookshelf);

            Item smartWatch = new Item("Smart Watch", "Fitness tracker", 120.0, electronics, 4.6, "Lisbon");
            smartWatch.setOwnerId(108L);
            itemRepository.save(smartWatch);

            Item tableLamp = new Item("Table Lamp", "Small table lamp", 25.0, lighting, 3.0, "Porto");
            tableLamp.setOwnerId(109L);
            itemRepository.save(tableLamp);
        }
    }
}
