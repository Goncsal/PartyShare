package tqs.backend.tqsbackend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tqs.backend.tqsbackend.entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByReceiverIdOrderBySentAtDesc(Long receiverId);

    List<Message> findBySenderIdOrderBySentAtDesc(Long senderId);

    @Query("SELECT m FROM Message m WHERE (m.senderId = :user1 AND m.receiverId = :user2) " +
           "OR (m.senderId = :user2 AND m.receiverId = :user1) ORDER BY m.sentAt ASC")
    List<Message> findConversation(@Param("user1") Long userId1, @Param("user2") Long userId2);

    @Query("SELECT m FROM Message m WHERE m.senderId = :userId OR m.receiverId = :userId ORDER BY m.sentAt DESC")
    List<Message> findAllForUser(@Param("userId") Long userId);
}
