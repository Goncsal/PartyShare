package tqs.backend.tqsbackend.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class SearchPage {
    private final Page page;

    public SearchPage(Page page) {
        this.page = page;
    }

    public void navigate(String url) {
        page.navigate(url);
    }

    public void clickFirstItemDetails() {
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Details")).first().click();
    }
}
