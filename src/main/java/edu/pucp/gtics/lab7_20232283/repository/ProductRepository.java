package edu.pucp.gtics.lab7_20232283.repository;

import edu.pucp.gtics.lab7_20232283.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    Optional<Product> findByNameIgnoreCase(String name);

    List<Product> findAllByOrderByNameAsc();
}
