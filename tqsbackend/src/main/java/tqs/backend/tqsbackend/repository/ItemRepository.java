package tqs.backend.tqsbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import tqs.backend.tqsbackend.entity.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {
    List<Item> findByNameContainingIgnoreCase(String name);

    List<Item> findByName(String name);

    List<Item> findByOwnerId(Long ownerId);
}
