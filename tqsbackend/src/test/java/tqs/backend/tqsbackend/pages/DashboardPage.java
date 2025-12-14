package tqs.backend.tqsbackend.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class DashboardPage {
    private final Page page;

    public DashboardPage(Page page) {
        this.page = page;
    }

    public void navigate(String url) {
        page.navigate(url);
    }

    public void clickAddNewItem() {
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("+ Add New Item")).click();
    }

    public boolean hasItem(String itemName) {
        // Use heading role to be more specific and avoid strict mode violations
        return page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName(itemName)).isVisible();
    }

    public void clickMyBookingRequests() {
        // Use CSS selector for robustness against icons and whitespace
        page.locator("a[href='/bookings/requests']").click();
    }

    public boolean isBookingStatus(String itemName, String status) {
        // Check in upcoming rentals table
        // Find row containing item name
        com.microsoft.playwright.Locator row = page.locator("tr")
                .filter(new com.microsoft.playwright.Locator.FilterOptions().setHasText(itemName));
        if (row.count() == 0)
            return false;
        return row.getByText(status).isVisible();
    }
}
