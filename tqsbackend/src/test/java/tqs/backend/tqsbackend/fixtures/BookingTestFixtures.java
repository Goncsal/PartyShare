package tqs.backend.tqsbackend.fixtures;

import java.math.BigDecimal;
import java.time.LocalDate;
import tqs.backend.tqsbackend.dto.BookingCreateRequest;
import tqs.backend.tqsbackend.entity.Booking;
import tqs.backend.tqsbackend.entity.BookingStatus;
import tqs.backend.tqsbackend.entity.Category;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.entity.PaymentStatus;

public final class BookingTestFixtures {

    private BookingTestFixtures() {
    }

    public static Item sampleItem(long id) {
        Item item = new Item();
        item.setId(id);
        item.setName("Lamp");
        item.setCategory(new Category("Lighting"));
        item.setPrice(40.0);
        item.setOwnerId(20L);
        return item;
    }

    public static Booking sampleBooking(long id, Item item) {
        Booking booking = new Booking(item,
                70L,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                BigDecimal.valueOf(item.getPrice()),
                BigDecimal.valueOf(item.getPrice() * 2),
                BookingStatus.CONFIRMED,
                PaymentStatus.PAID);
        booking.setId(id);
        booking.setPaymentReference("PAY-" + id);
        return booking;
    }

    public static BookingCreateRequest sampleRequest(long itemId, long renterId, LocalDate start, LocalDate end) {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setItemId(itemId);
        request.setRenterId(renterId);
        request.setStartDate(start);
        request.setEndDate(end);
        return request;
    }
}
