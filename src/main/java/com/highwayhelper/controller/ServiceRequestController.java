package com.highwayhelper.controller;

import com.highwayhelper.dto.request.AssignMechanicRequest;
import com.highwayhelper.dto.request.CreateServiceRequestRequest;
import com.highwayhelper.dto.request.UpdateServiceRequestStatusRequest;
import com.highwayhelper.dto.response.PageResponse;
import com.highwayhelper.dto.response.ServiceRequestResponse;
import com.highwayhelper.entity.enums.Role;
import com.highwayhelper.exception.ResourceNotFoundException;
import com.highwayhelper.repository.MechanicRepository;
import com.highwayhelper.security.SecurityUtils;
import com.highwayhelper.security.UserPrincipal;
import com.highwayhelper.service.ServiceRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/service-requests")
@RequiredArgsConstructor
@Tag(name = "Service Requests", description = "Roadside assistance request management")
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;
    private final MechanicRepository mechanicRepository;
    private final SecurityUtils securityUtils;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create a new service request")
    public ResponseEntity<ServiceRequestResponse> createRequest(
            @Valid @RequestBody CreateServiceRequestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(serviceRequestService.createRequest(securityUtils.getCurrentUserId(), request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get service request by ID")
    public ResponseEntity<ServiceRequestResponse> getRequest(@PathVariable Long id) {
        return ResponseEntity.ok(serviceRequestService.getRequestById(id));
    }

    @GetMapping("/my-requests")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "List current user's service requests")
    public ResponseEntity<PageResponse<ServiceRequestResponse>> listMyRequests(
            @PageableDefault(size = 20, sort = "requestedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(
                serviceRequestService.listUserRequests(securityUtils.getCurrentUserId(), pageable));
    }

    @GetMapping("/assigned")
    @PreAuthorize("hasRole('MECHANIC')")
    @Operation(summary = "List service requests assigned to current mechanic")
    public ResponseEntity<PageResponse<ServiceRequestResponse>> listAssignedRequests(
            @PageableDefault(size = 20, sort = "requestedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long mechanicId = mechanicRepository.findByUserId(securityUtils.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Mechanic profile not found"))
                .getId();
        return ResponseEntity.ok(serviceRequestService.listMechanicRequests(mechanicId, pageable));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Assign a mechanic to a pending service request")
    public ResponseEntity<ServiceRequestResponse> assignMechanic(
            @PathVariable Long id,
            @Valid @RequestBody AssignMechanicRequest request) {
        Role callerRole = getCallerRole();
        return ResponseEntity.ok(serviceRequestService.assignMechanic(id, request, callerRole));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('USER', 'MECHANIC', 'ADMIN')")
    @Operation(summary = "Update service request status")
    public ResponseEntity<ServiceRequestResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateServiceRequestStatusRequest request) {
        UserPrincipal principal = securityUtils.getCurrentUser();
        String roleAuthority = principal.getAuthorities().iterator().next().getAuthority();
        Role callerRole = Role.valueOf(roleAuthority.replace("ROLE_", ""));
        return ResponseEntity.ok(serviceRequestService.updateStatus(
                id, request, principal.getId(), callerRole));
    }

    private Role getCallerRole() {
        String roleAuthority = securityUtils.getCurrentUser().getAuthorities().iterator().next().getAuthority();
        return Role.valueOf(roleAuthority.replace("ROLE_", ""));
    }
}
