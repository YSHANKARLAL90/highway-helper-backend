package com.highwayhelper.controller;

import com.highwayhelper.dto.request.MechanicRegistrationRequest;
import com.highwayhelper.dto.response.MechanicResponse;
import com.highwayhelper.dto.response.PageResponse;
import com.highwayhelper.entity.enums.MechanicStatus;
import com.highwayhelper.security.SecurityUtils;
import com.highwayhelper.service.MechanicService;
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
@RequestMapping("/mechanics")
@RequiredArgsConstructor
@Tag(name = "Mechanics", description = "Mechanic registration and listing endpoints")
public class MechanicController {

    private final MechanicService mechanicService;
    private final SecurityUtils securityUtils;

    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('USER', 'MECHANIC')")
    @Operation(summary = "Register as a mechanic (authenticated user)")
    public ResponseEntity<MechanicResponse> registerMechanic(
            @Valid @RequestBody MechanicRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mechanicService.registerMechanic(securityUtils.getCurrentUserId(), request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get mechanic profile by ID")
    public ResponseEntity<MechanicResponse> getMechanic(@PathVariable Long id) {
        return ResponseEntity.ok(mechanicService.getMechanicById(id));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('MECHANIC')")
    @Operation(summary = "Get current mechanic's profile")
    public ResponseEntity<MechanicResponse> getMyProfile() {
        return ResponseEntity.ok(mechanicService.getMechanicByUserId(securityUtils.getCurrentUserId()));
    }

    @GetMapping
    @Operation(summary = "List verified active mechanics (optional city filter)")
    public ResponseEntity<PageResponse<MechanicResponse>> listMechanics(
            @RequestParam(required = false) String city,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(mechanicService.listMechanics(city, pageable));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update mechanic active/inactive status (Admin)")
    public ResponseEntity<MechanicResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam MechanicStatus status) {
        return ResponseEntity.ok(mechanicService.updateMechanicStatus(id, status));
    }
}
