package com.highwayhelper.repository;

import com.highwayhelper.entity.Mechanic;
import com.highwayhelper.entity.enums.MechanicStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MechanicRepository extends JpaRepository<Mechanic, Long> {

    Optional<Mechanic> findByUserId(Long userId);

    Page<Mechanic> findByStatus(MechanicStatus status, Pageable pageable);

    Page<Mechanic> findByIsVerifiedFalse(Pageable pageable);

    Page<Mechanic> findByCityIgnoreCaseAndStatusAndIsVerifiedTrue(
            String city, MechanicStatus status, Pageable pageable);

    long countByIsVerifiedTrue();

    long countByIsVerifiedFalse();

    @Query("""
            SELECT m FROM Mechanic m
            WHERE m.status = :status AND m.isVerified = true
            AND (:city IS NULL OR LOWER(m.city) = LOWER(:city))
            """)
    Page<Mechanic> findAvailableMechanics(
            @Param("city") String city,
            @Param("status") MechanicStatus status,
            Pageable pageable);
}
