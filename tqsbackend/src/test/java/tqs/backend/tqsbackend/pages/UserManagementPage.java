package tqs.backend.tqsbackend.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class UserManagementPage {
    private final Page page;

    public UserManagementPage(Page page) {
        this.page = page;
    }

    public void searchForUser(String keyword) {
        page.getByPlaceholder("Search by name or email").fill(keyword);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search")).click();
    }

    public void viewProfile(String userName) {
        // Find the row with the user name and click "View Profile"
        page.getByRole(AriaRole.ROW, new Page.GetByRoleOptions().setName(userName))
                .getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("View Profile"))
                .click();
    }
}
