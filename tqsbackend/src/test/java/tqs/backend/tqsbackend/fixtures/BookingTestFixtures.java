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
                BookingStatus.ACCEPTED,
                PaymentStatus.PAID);
        booking.setId(id);
        booking.setPaymentReference("PAY-" + id);
        return booking;
    }

    public static LocalDate futureDate(int daysAhead) {
        return LocalDate.now().plusDays(daysAhead);
    }

    public static BookingCreateRequest sampleRequest(long itemId, long renterId, LocalDate start, LocalDate end) {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setItemId(itemId);
        request.setRenterId(renterId);
        request.setStartDate(start);
        request.setEndDate(end);
        return request;
    }

    public static BookingCreateRequest sampleRequest(long itemId, long renterId) {
        return sampleRequest(itemId, renterId, futureDate(3), futureDate(6));
    }

    public static String bookingRequestJson(long itemId, long renterId, LocalDate start, LocalDate end) {
        return """
                {
                  \"itemId\": %d,
                  \"renterId\": %d,
                  \"startDate\": \"%s\",
                  \"endDate\": \"%s\"
                }
                """.formatted(itemId, renterId, start, end);
    }
}
