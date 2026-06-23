package com.highwayhelper.service;

import com.highwayhelper.dto.response.DashboardStatsResponse;
import com.highwayhelper.entity.enums.Role;
import com.highwayhelper.entity.enums.ServiceRequestStatus;
import com.highwayhelper.repository.MechanicRepository;
import com.highwayhelper.repository.ServiceRequestRepository;
import com.highwayhelper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final MechanicRepository mechanicRepository;
    private final ServiceRequestRepository serviceRequestRepository;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getAdminDashboardStats() {
        long pending = serviceRequestRepository.countByStatus(ServiceRequestStatus.PENDING);
        long accepted = serviceRequestRepository.countByStatus(ServiceRequestStatus.ACCEPTED);
        long onTheWay = serviceRequestRepository.countByStatus(ServiceRequestStatus.ON_THE_WAY);

        return DashboardStatsResponse.builder()
                .totalUsers(userRepository.countByRole(Role.USER))
                .totalMechanics(userRepository.countByRole(Role.MECHANIC))
                .verifiedMechanics(mechanicRepository.countByIsVerifiedTrue())
                .pendingVerifications(mechanicRepository.countByIsVerifiedFalse())
                .totalServiceRequests(serviceRequestRepository.count())
                .pendingRequests(pending)
                .activeRequests(accepted + onTheWay)
                .completedRequests(serviceRequestRepository.countByStatus(ServiceRequestStatus.COMPLETED))
                .cancelledRequests(serviceRequestRepository.countByStatus(ServiceRequestStatus.CANCELLED))
                .build();
    }
}
