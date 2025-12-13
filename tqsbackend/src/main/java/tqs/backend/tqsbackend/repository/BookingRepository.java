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

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.status IN ('REQUESTED', 'ACCEPTED')")
    List<Booking> findActiveBookingsByItemId(@Param("itemId") Long itemId);

        boolean existsByRenterIdAndItem_IdAndStatusAndEndDateBefore(Long renterId, Long itemId, BookingStatus status,
                        LocalDate date);

        // Check if renter has completed booking with owner (for owner rating
        // validation)
        @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b WHERE b.renterId = :renterId AND b.item.ownerId = :ownerId AND b.status = :status AND b.endDate < :date")
        boolean existsByRenterIdAndItem_OwnerIdAndStatusAndEndDateBefore(@Param("renterId") Long renterId,
                        @Param("ownerId") Long ownerId, @Param("status") BookingStatus status,
                        @Param("date") LocalDate date);

        // Get upcoming rentals for owner dashboard (endDate >= today)
        @Query("SELECT b FROM Booking b WHERE b.item.ownerId = :ownerId AND b.status IN :statuses AND b.endDate >= :date ORDER BY b.startDate ASC")
        List<Booking> findByItem_OwnerIdAndStatusInAndEndDateGreaterThanEqualOrderByStartDateAsc(
                        @Param("ownerId") Long ownerId, @Param("statuses") Collection<BookingStatus> statuses,
                        @Param("date") LocalDate date);

        // Get past rentals for owner dashboard (endDate < today)
        @Query("SELECT b FROM Booking b WHERE b.item.ownerId = :ownerId AND b.status IN :statuses AND b.endDate < :date ORDER BY b.startDate DESC")
        List<Booking> findByItem_OwnerIdAndStatusInAndEndDateLessThanOrderByStartDateDesc(
                        @Param("ownerId") Long ownerId, @Param("statuses") Collection<BookingStatus> statuses,
                        @Param("date") LocalDate date);

    List<Booking> findByItem_OwnerIdAndStatus(Long ownerId, BookingStatus status);
    List<Booking> findByStatus(BookingStatus status);
}
