package tqs.backend.tqsbackend.entity;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class ItemTest {

    @Test
    public void testDefaultImage() {
        Item item = new Item();
        assertThat(item.getImageUrl()).isEqualTo("https://placehold.co/600x400?text=No+Image");
    }

    @Test
    public void testSetImageUrl_Valid() {
        Item item = new Item();
        String url = "http://example.com/image.jpg";
        item.setImageUrl(url);
        assertThat(item.getImageUrl()).isEqualTo(url);
    }

    @Test
    public void testSetImageUrl_Null_KeepsDefault() {
        Item item = new Item();
        item.setImageUrl(null);
        assertThat(item.getImageUrl()).isEqualTo("https://placehold.co/600x400?text=No+Image");
    }

    @Test
    public void testSetImageUrl_Empty_KeepsDefault() {
        Item item = new Item();
        item.setImageUrl("");
        assertThat(item.getImageUrl()).isEqualTo("https://placehold.co/600x400?text=No+Image");
    }
    
    @Test
    public void testConstructors() {
        Category cat = new Category("Test");
        Item item = new Item("Name", "Desc", 10.0, cat, 5.0, "Loc");
        assertThat(item.getName()).isEqualTo("Name");
        assertThat(item.getOwnerId()).isNull();
        
        Item item2 = new Item("Name", "Desc", 10.0, cat, 5.0, "Loc", 1L);
        assertThat(item2.getOwnerId()).isEqualTo(1L);
    }
}
