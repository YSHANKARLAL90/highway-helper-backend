package com.highwayhelper.controller;

import com.highwayhelper.dto.response.DashboardStatsResponse;
import com.highwayhelper.dto.response.MechanicResponse;
import com.highwayhelper.dto.response.PageResponse;
import com.highwayhelper.dto.response.ServiceRequestResponse;
import com.highwayhelper.entity.enums.ServiceRequestStatus;
import com.highwayhelper.service.DashboardService;
import com.highwayhelper.service.MechanicService;
import com.highwayhelper.service.ServiceRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin-only management endpoints")
public class AdminController {

    private final MechanicService mechanicService;
    private final ServiceRequestService serviceRequestService;
    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get platform dashboard statistics")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(dashboardService.getAdminDashboardStats());
    }

    @GetMapping("/mechanics/pending")
    @Operation(summary = "List mechanics pending verification")
    public ResponseEntity<PageResponse<MechanicResponse>> listPendingMechanics(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(mechanicService.listPendingVerifications(pageable));
    }

    @PatchMapping("/mechanics/{id}/verify")
    @Operation(summary = "Verify a mechanic profile")
    public ResponseEntity<MechanicResponse> verifyMechanic(@PathVariable Long id) {
        return ResponseEntity.ok(mechanicService.verifyMechanic(id));
    }

    @GetMapping("/service-requests")
    @Operation(summary = "List all service requests with optional status filter")
    public ResponseEntity<PageResponse<ServiceRequestResponse>> listAllServiceRequests(
            @RequestParam(required = false) ServiceRequestStatus status,
            @PageableDefault(size = 20, sort = "requestedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(serviceRequestService.listAllRequests(status, pageable));
    }
}
