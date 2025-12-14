package tqs.backend.tqsbackend.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class DetailPage {
    private final Page page;

    public DetailPage(Page page) {
        this.page = page;
    }

    public void clickRentNow() {
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Rent Now")).click();
    }
}
