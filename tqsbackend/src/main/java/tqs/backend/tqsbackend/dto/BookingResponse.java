package tqs.backend.tqsbackend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tqs.backend.tqsbackend.entity.BookingStatus;
import tqs.backend.tqsbackend.entity.PaymentStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private Long id;
    private Long itemId;
    private Long renterId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private PaymentStatus paymentStatus;
    private String paymentReference;
}
