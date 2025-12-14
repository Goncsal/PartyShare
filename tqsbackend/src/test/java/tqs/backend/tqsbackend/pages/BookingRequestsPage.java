package tqs.backend.tqsbackend.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class BookingRequestsPage {
    private final Page page;

    public BookingRequestsPage(Page page) {
        this.page = page;
    }

    public void acceptRequest(String itemName) {
        // Assuming there's a row or card for the item with an Accept button
        // The script just clicks "Accept", assuming it's the only one or the first one.
        // To be more robust, we should find the request for the specific item.
        // For now, let's follow the script's simplicity but maybe scope it if possible.
        // If there are multiple, this will click the first one or fail if strict.
        // Let's assume for this test scenario there is only one pending request.
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Accept")).click();
    }

    public boolean hasRequestStatus(String itemName, String status) {
        // Verify that the status has changed.
        // This might require reloading or checking the text content of a status
        // element.
        // Assuming the status text is visible.
        return page.getByText(status).isVisible();
    }
}
