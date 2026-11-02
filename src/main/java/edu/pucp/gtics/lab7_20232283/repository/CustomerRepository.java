package edu.pucp.gtics.lab7_20232283.repository;

import edu.pucp.gtics.lab7_20232283.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    Optional<Customer> findByDocument(String document);

    List<Customer> findAllByOrderByNameAsc();
}
