package tqs.backend.tqsbackend.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class AdminDashboardPage {
    private final Page page;

    public AdminDashboardPage(Page page) {
        this.page = page;
    }

    public void navigateToUserManagement() {
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("User Management")).click();
    }
}
