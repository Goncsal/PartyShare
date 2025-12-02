package tqs.backend.tqsbackend.entity;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class EntityTest {

    @Test
    public void testCategoryEntity() {
        Category category = new Category("Test Category");
        category.setId(1L);

        assertThat(category.getId()).isEqualTo(1L);
        assertThat(category.getName()).isEqualTo("Test Category");

        category.setName("Updated Category");
        assertThat(category.getName()).isEqualTo("Updated Category");
    }

    @Test
    public void testItemEntity() {
        Category category = new Category("Cat");
        Item item = new Item("Item Name", "Desc", 10.0, category, 4.5, "Loc");
        item.setId(1L);

        assertThat(item.getId()).isEqualTo(1L);
        assertThat(item.getName()).isEqualTo("Item Name");
        assertThat(item.getDescription()).isEqualTo("Desc");
        assertThat(item.getPrice()).isEqualTo(10.0);
        assertThat(item.getCategory()).isEqualTo(category);
        assertThat(item.getAverageRating()).isEqualTo(4.5);
        assertThat(item.getLocation()).isEqualTo("Loc");

        item.setName("New Name");
        item.setDescription("New Desc");
        item.setPrice(20.0);
        item.setAverageRating(5.0);
        item.setLocation("New Loc");

        assertThat(item.getName()).isEqualTo("New Name");
        assertThat(item.getDescription()).isEqualTo("New Desc");
        assertThat(item.getPrice()).isEqualTo(20.0);
        assertThat(item.getAverageRating()).isEqualTo(5.0);
        assertThat(item.getLocation()).isEqualTo("New Loc");
    }

    @Test
    public void testFavoriteEntity() {
        Item item = new Item();
        Favorite favorite = new Favorite(1L, item);
        favorite.setId(10L);

        assertThat(favorite.getId()).isEqualTo(10L);
        assertThat(favorite.getUserId()).isEqualTo(1L);
        assertThat(favorite.getItem()).isEqualTo(item);

        favorite.setUserId(2L);
        Item newItem = new Item();
        favorite.setItem(newItem);

        assertThat(favorite.getUserId()).isEqualTo(2L);
        assertThat(favorite.getItem()).isEqualTo(newItem);
    }

    @Test
    public void testNoArgsConstructor() {
        Category category = new Category();
        assertThat(category).isNotNull();

        Item item = new Item();
        assertThat(item).isNotNull();

        Favorite favorite = new Favorite();
        assertThat(favorite).isNotNull();
    }
}
