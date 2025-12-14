package tqs.backend.tqsbackend.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class RentalFormPage {
    private final Page page;

    public RentalFormPage(Page page) {
        this.page = page;
    }

    public void fillStartDate(String date) {
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Start date"))
                .evaluate("(el, date) => el.value = date", date);
    }

    public void fillEndDate(String date) {
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("End Date"))
                .evaluate("(el, date) => el.value = date", date);
    }

    public void fillOffer(String amount) {
        page.getByRole(AriaRole.SPINBUTTON, new Page.GetByRoleOptions().setName("Your Offer (per day) €")).fill(amount);
    }

    public void submit() {
        page.locator("#submitBtn").click();
    }

    public boolean hasConfirmationMessage(String message) {
        return page.getByText(message).isVisible();
    }
}
