package edu.pucp.gtics.lab7_20232283.repository;

import edu.pucp.gtics.lab7_20232283.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {

    List<Invoice> findAllByOrderByDateDescIdDesc();
}
