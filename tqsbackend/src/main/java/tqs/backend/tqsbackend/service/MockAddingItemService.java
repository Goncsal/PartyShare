package tqs.backend.tqsbackend.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tqs.backend.tqsbackend.entity.Category;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.repository.CategoryRepository;
import tqs.backend.tqsbackend.repository.ItemRepository;
import tqs.backend.tqsbackend.repository.UserRepository;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class MockAddingItemService {

        private final ItemRepository itemRepository;
        private final CategoryRepository categoryRepository;
        private final UserRepository userRepository;

        @Value("${MOCK_USER_PASSWORD:testPassword123}")
        private String mockUserPassword;

        @PostConstruct
        public void init() {
                if (userRepository.count() == 0 && itemRepository.count() == 0) {
                        // Create owner users
                        User owner1 = new User("Maria Silva", "maria@ua.pt",
                                        BCrypt.hashpw(mockUserPassword, BCrypt.gensalt()),
                                        UserRoles.OWNER);
                        User owner2 = new User("João Santos", "joao@ua.pt",
                                        BCrypt.hashpw(mockUserPassword, BCrypt.gensalt()),
                                        UserRoles.OWNER);
                        User owner3 = new User("Ana Costa", "ana@ua.pt",
                                        BCrypt.hashpw(mockUserPassword, BCrypt.gensalt()),
                                        UserRoles.OWNER);

                        userRepository.saveAll(Arrays.asList(owner1, owner2, owner3));

                        // Create categories
                        Category electronics = new Category("Electronics");
                        Category lighting = new Category("Lighting");
                        Category furniture = new Category("Furniture");
                        categoryRepository.saveAll(Arrays.asList(electronics, lighting, furniture));

                        // Create items (reduced to 5 items)
                        Item partyLamps = new Item("Party Lights", "Beautiful Colorful Lights", 55.0, lighting, 0.0,
                                        "Lisbon");
                        partyLamps.setOwnerId(owner1.getId());
                        itemRepository.save(partyLamps);

                        Item mealTable = new Item("Party Meal Table", "Foldable dining table", 150.0, furniture, 0.0,
                                        "Porto");
                        mealTable.setOwnerId(owner1.getId());
                        itemRepository.save(mealTable);

                        Item audioInterface = new Item("Audio Interface", "High performance laptop", 1200.0,
                                        electronics, 0.0,
                                        "Lisbon");
                        audioInterface.setOwnerId(owner2.getId());
                        itemRepository.save(audioInterface);

                        Item sunHat = new Item("Big Sun Hat", "Sun Hat for Parties", 80.0, furniture, 0.0, "Lisbon");
                        sunHat.setOwnerId(owner2.getId());
                        itemRepository.save(sunHat);

                        Item speaker = new Item("Bluetooth Speaker", "Portable speaker", 40.0, electronics, 0.0,
                                        "Porto");
                        speaker.setOwnerId(owner3.getId());
                        itemRepository.save(speaker);

                        System.out.println("Mock data initialized: 3 owners and 5 items created!");
                }
        }
}
