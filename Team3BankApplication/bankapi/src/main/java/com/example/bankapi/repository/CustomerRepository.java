package com.example.bankapi.repository;

import com.example.bankapi.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Find a customer by their unique customer number.
     */
    Optional<Customer> findByCustomerNumber(String customerNumber);

    /**
     * Find a customer by their unique email address.
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Find all customers with a full name containing the given search text (case-insensitive).
     */
    List<Customer> findByFullNameContainingIgnoreCase(String fullName);

    /**
     * Custom query to find customers created on or after a specific date.
     */
    @Query("SELECT c FROM Customer c WHERE c.createdDate >= :createdDate ORDER BY c.createdDate DESC")
    List<Customer> findCustomersCreatedAfter(@Param("createdDate") java.time.LocalDate createdDate);

    /**
     * Find all customers ordered by their creation date in descending order.
     */
    List<Customer> findAllByOrderByCreatedDateDesc();

}

