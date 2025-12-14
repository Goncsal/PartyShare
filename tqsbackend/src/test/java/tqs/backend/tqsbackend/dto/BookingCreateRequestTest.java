package tqs.backend.tqsbackend.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class BookingCreateRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setItemId(1L);
        request.setRenterId(2L);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(5));
        request.setProposedPrice(50.0);

        assertThat(request.getItemId()).isEqualTo(1L);
        assertThat(request.getRenterId()).isEqualTo(2L);
        assertThat(request.getStartDate()).isAfter(LocalDate.now());
        assertThat(request.getEndDate()).isAfter(request.getStartDate());
        assertThat(request.getProposedPrice()).isEqualTo(50.0);
    }

    @Test
    void testValidation_NullItemId_Fails() {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setRenterId(2L);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(5));

        var violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("itemId"))).isTrue();
    }

    @Test
    void testValidation_NullRenterId_Fails() {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setItemId(1L);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(5));

        var violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("renterId"))).isTrue();
    }

    @Test
    void testValidation_PastEndDate_Fails() {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setItemId(1L);
        request.setRenterId(2L);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().minusDays(1));

        var violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("endDate"))).isTrue();
    }

    @Test
    void testValidation_ValidRequest_Passes() {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setItemId(1L);
        request.setRenterId(2L);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(5));

        var violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }
}
