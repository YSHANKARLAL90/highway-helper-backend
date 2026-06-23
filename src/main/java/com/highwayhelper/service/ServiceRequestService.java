package com.highwayhelper.service;

import com.highwayhelper.dto.request.AssignMechanicRequest;
import com.highwayhelper.dto.request.CreateServiceRequestRequest;
import com.highwayhelper.dto.request.UpdateServiceRequestStatusRequest;
import com.highwayhelper.dto.response.PageResponse;
import com.highwayhelper.dto.response.ServiceRequestResponse;
import com.highwayhelper.entity.Mechanic;
import com.highwayhelper.entity.ServiceRequest;
import com.highwayhelper.entity.User;
import com.highwayhelper.entity.enums.MechanicStatus;
import com.highwayhelper.entity.enums.Role;
import com.highwayhelper.entity.enums.ServiceRequestStatus;
import com.highwayhelper.exception.BadRequestException;
import com.highwayhelper.exception.ForbiddenException;
import com.highwayhelper.exception.ResourceNotFoundException;
import com.highwayhelper.mapper.ServiceRequestMapper;
import com.highwayhelper.repository.MechanicRepository;
import com.highwayhelper.repository.ServiceRequestRepository;
import com.highwayhelper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ServiceRequestService {

    private static final Set<ServiceRequestStatus> TERMINAL_STATUSES =
            EnumSet.of(ServiceRequestStatus.COMPLETED, ServiceRequestStatus.CANCELLED);

    private final ServiceRequestRepository serviceRequestRepository;
    private final UserRepository userRepository;
    private final MechanicRepository mechanicRepository;
    private final ServiceRequestMapper serviceRequestMapper;

    @Transactional
    public ServiceRequestResponse createRequest(Long userId, CreateServiceRequestRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getRole() != Role.USER) {
            throw new ForbiddenException("Only vehicle owners can create service requests");
        }

        ServiceRequest serviceRequest = ServiceRequest.builder()
                .user(user)
                .vehicleType(request.getVehicleType())
                .vehicleNumber(request.getVehicleNumber())
                .issueType(request.getIssueType())
                .description(request.getDescription())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .status(ServiceRequestStatus.PENDING)
                .build();

        return serviceRequestMapper.toResponse(serviceRequestRepository.save(serviceRequest));
    }

    @Transactional
    public ServiceRequestResponse assignMechanic(Long requestId, AssignMechanicRequest assignRequest, Role callerRole) {
        ServiceRequest serviceRequest = findRequestOrThrow(requestId);

        if (serviceRequest.getStatus() != ServiceRequestStatus.PENDING) {
            throw new BadRequestException("Mechanic can only be assigned to pending requests");
        }

        if (callerRole != Role.ADMIN && callerRole != Role.USER) {
            throw new ForbiddenException("Not authorized to assign mechanic");
        }

        Mechanic mechanic = mechanicRepository.findById(assignRequest.getMechanicId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mechanic not found with id: " + assignRequest.getMechanicId()));

        if (!Boolean.TRUE.equals(mechanic.getIsVerified()) || mechanic.getStatus() != MechanicStatus.ACTIVE) {
            throw new BadRequestException("Selected mechanic is not available");
        }

        serviceRequest.setMechanic(mechanic);
        serviceRequest.setStatus(ServiceRequestStatus.ACCEPTED);

        return serviceRequestMapper.toResponse(serviceRequestRepository.save(serviceRequest));
    }

    @Transactional
    public ServiceRequestResponse updateStatus(
            Long requestId, UpdateServiceRequestStatusRequest statusRequest, Long callerId, Role callerRole) {

        ServiceRequest serviceRequest = findRequestOrThrow(requestId);
        ServiceRequestStatus newStatus = statusRequest.getStatus();

        validateStatusTransition(serviceRequest, newStatus, callerId, callerRole);

        serviceRequest.setStatus(newStatus);
        if (newStatus == ServiceRequestStatus.COMPLETED) {
            serviceRequest.setCompletedAt(LocalDateTime.now());
        }

        return serviceRequestMapper.toResponse(serviceRequestRepository.save(serviceRequest));
    }

    @Transactional(readOnly = true)
    public ServiceRequestResponse getRequestById(Long id) {
        return serviceRequestMapper.toResponse(findRequestOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<ServiceRequestResponse> listUserRequests(Long userId, Pageable pageable) {
        Page<ServiceRequest> page = serviceRequestRepository.findByUserId(userId, pageable);
        return PageResponse.from(page.map(serviceRequestMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<ServiceRequestResponse> listMechanicRequests(Long mechanicId, Pageable pageable) {
        Page<ServiceRequest> page = serviceRequestRepository.findByMechanicId(mechanicId, pageable);
        return PageResponse.from(page.map(serviceRequestMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<ServiceRequestResponse> listAllRequests(ServiceRequestStatus status, Pageable pageable) {
        Page<ServiceRequest> page = status != null
                ? serviceRequestRepository.findByStatus(status, pageable)
                : serviceRequestRepository.findAll(pageable);
        return PageResponse.from(page.map(serviceRequestMapper::toResponse));
    }

    private void validateStatusTransition(
            ServiceRequest request, ServiceRequestStatus newStatus, Long callerId, Role callerRole) {

        if (TERMINAL_STATUSES.contains(request.getStatus())) {
            throw new BadRequestException("Cannot update a completed or cancelled request");
        }

        switch (newStatus) {
            case ACCEPTED -> {
                if (request.getStatus() != ServiceRequestStatus.PENDING) {
                    throw new BadRequestException("Only pending requests can be accepted");
                }
                if (callerRole != Role.MECHANIC && callerRole != Role.ADMIN) {
                    throw new ForbiddenException("Only mechanics or admins can accept requests");
                }
            }
            case ON_THE_WAY -> {
                if (request.getStatus() != ServiceRequestStatus.ACCEPTED) {
                    throw new BadRequestException("Request must be accepted before marking on the way");
                }
                validateMechanicOwnership(request, callerId, callerRole);
            }
            case COMPLETED -> {
                if (request.getStatus() != ServiceRequestStatus.ON_THE_WAY
                        && request.getStatus() != ServiceRequestStatus.ACCEPTED) {
                    throw new BadRequestException("Request must be accepted or on the way before completion");
                }
                validateMechanicOwnership(request, callerId, callerRole);
            }
            case CANCELLED -> {
                if (callerRole == Role.USER && !request.getUser().getId().equals(callerId)) {
                    throw new ForbiddenException("You can only cancel your own requests");
                }
                if (callerRole == Role.MECHANIC) {
                    validateMechanicOwnership(request, callerId, callerRole);
                }
            }
            case PENDING -> throw new BadRequestException("Cannot revert request to pending status");
        }
    }

    private void validateMechanicOwnership(ServiceRequest request, Long callerId, Role callerRole) {
        if (callerRole == Role.ADMIN) {
            return;
        }
        if (callerRole != Role.MECHANIC || request.getMechanic() == null
                || !request.getMechanic().getUser().getId().equals(callerId)) {
            throw new ForbiddenException("Not authorized to update this request");
        }
    }

    private ServiceRequest findRequestOrThrow(Long id) {
        return serviceRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service request not found with id: " + id));
    }
}
