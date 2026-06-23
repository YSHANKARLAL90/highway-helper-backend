package com.highwayhelper.repository;

import com.highwayhelper.entity.ServiceRequest;
import com.highwayhelper.entity.enums.ServiceRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    Page<ServiceRequest> findByUserId(Long userId, Pageable pageable);

    Page<ServiceRequest> findByMechanicId(Long mechanicId, Pageable pageable);

    Page<ServiceRequest> findByStatus(ServiceRequestStatus status, Pageable pageable);

    long countByStatus(ServiceRequestStatus status);

    @Query("""
            SELECT sr FROM ServiceRequest sr
            WHERE sr.mechanic.id = :mechanicId AND sr.status = :status
            """)
    long countByMechanicIdAndStatus(
            @Param("mechanicId") Long mechanicId,
            @Param("status") ServiceRequestStatus status);
}
