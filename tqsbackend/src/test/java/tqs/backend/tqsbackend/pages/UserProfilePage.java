package tqs.backend.tqsbackend.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class UserProfilePage {
    private final Page page;

    public UserProfilePage(Page page) {
        this.page = page;
    }

    public void deactivateUser() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Deactivate User")).click();
    }

    public boolean isUserInactive() {
        return page.getByText("Inactive").isVisible();
    }
}
