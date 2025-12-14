package tqs.backend.tqsbackend.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class CreateItemPage {
    private final Page page;

    public CreateItemPage(Page page) {
        this.page = page;
    }

    public void fillForm(String name, String description, String price, String category, String location,
            String imageUrl) {
        page.locator("input[name=\"name\"]").fill(name);
        page.locator("textarea[name=\"description\"]").fill(description);

        // Handling price input (number type)
        page.getByRole(AriaRole.SPINBUTTON).fill(price);

        // Handling category selection (combobox)
        // Select by label (category name) which is passed as 'category' argument
        page.getByRole(AriaRole.COMBOBOX)
                .selectOption(new com.microsoft.playwright.options.SelectOption().setLabel(category));

        page.locator("input[name=\"location\"]").fill(location);

        // Handling image URL input
        // The script used a very specific name for the getByRole, which might be the
        // placeholder or label.
        // Let's try to find a more robust selector if possible, or use the one from the
        // script if it's based on label.
        // Assuming there is a label or placeholder for image URL.
        // Based on script: page.getByRole(AriaRole.TEXTBOX, new
        // Page.GetByRoleOptions().setName("https://example.com/image.jpg"))
        // This looks like it's targeting by placeholder.
        page.getByPlaceholder("https://example.com/image.jpg").fill(imageUrl);
    }

    public void submit() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Create Item")).click();
    }
}
