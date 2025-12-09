package tqs.backend.tqsbackend.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tqs.backend.tqsbackend.entity.Booking;
import tqs.backend.tqsbackend.entity.BookingStatus;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByItemIdAndStatusInAndStartDateLessThanAndEndDateGreaterThan(Long itemId,
            Collection<BookingStatus> statuses, LocalDate endDate, LocalDate startDate);

    List<Booking> findByRenterId(Long renterId);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.status IN ('PENDING', 'CONFIRMED')")
    List<Booking> findActiveBookingsByItemId(@Param("itemId") Long itemId);
}
