package com.highwayhelper.service;

import com.highwayhelper.dto.request.MechanicRegistrationRequest;
import com.highwayhelper.dto.response.MechanicResponse;
import com.highwayhelper.dto.response.PageResponse;
import com.highwayhelper.entity.Mechanic;
import com.highwayhelper.entity.User;
import com.highwayhelper.entity.enums.MechanicStatus;
import com.highwayhelper.entity.enums.Role;
import com.highwayhelper.exception.BadRequestException;
import com.highwayhelper.exception.ForbiddenException;
import com.highwayhelper.exception.ResourceNotFoundException;
import com.highwayhelper.mapper.MechanicMapper;
import com.highwayhelper.repository.MechanicRepository;
import com.highwayhelper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MechanicService {

    private final MechanicRepository mechanicRepository;
    private final UserRepository userRepository;
    private final MechanicMapper mechanicMapper;

    @Transactional
    public MechanicResponse registerMechanic(Long userId, MechanicRegistrationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getRole() != Role.USER && user.getRole() != Role.MECHANIC) {
            throw new ForbiddenException("Only users can register as mechanics");
        }

        if (mechanicRepository.findByUserId(userId).isPresent()) {
            throw new BadRequestException("Mechanic profile already exists for this user");
        }

        Mechanic mechanic = mechanicMapper.toEntity(request);
        mechanic.setUser(user);

        if (user.getRole() == Role.USER) {
            user.setRole(Role.MECHANIC);
        }

        Mechanic saved = mechanicRepository.save(mechanic);
        return mechanicMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public MechanicResponse getMechanicById(Long id) {
        Mechanic mechanic = findMechanicOrThrow(id);
        return mechanicMapper.toResponse(mechanic);
    }

    @Transactional(readOnly = true)
    public MechanicResponse getMechanicByUserId(Long userId) {
        Mechanic mechanic = mechanicRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Mechanic not found for user id: " + userId));
        return mechanicMapper.toResponse(mechanic);
    }

    @Transactional(readOnly = true)
    public PageResponse<MechanicResponse> listMechanics(String city, Pageable pageable) {
        Page<Mechanic> page = mechanicRepository.findAvailableMechanics(
                city, MechanicStatus.ACTIVE, pageable);
        return PageResponse.from(page.map(mechanicMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<MechanicResponse> listPendingVerifications(Pageable pageable) {
        Page<Mechanic> page = mechanicRepository.findByIsVerifiedFalse(pageable);
        return PageResponse.from(page.map(mechanicMapper::toResponse));
    }

    @Transactional
    public MechanicResponse verifyMechanic(Long mechanicId) {
        Mechanic mechanic = findMechanicOrThrow(mechanicId);

        if (Boolean.TRUE.equals(mechanic.getIsVerified())) {
            throw new BadRequestException("Mechanic is already verified");
        }

        mechanic.setIsVerified(true);
        mechanic.setStatus(MechanicStatus.ACTIVE);
        return mechanicMapper.toResponse(mechanicRepository.save(mechanic));
    }

    @Transactional
    public MechanicResponse updateMechanicStatus(Long mechanicId, MechanicStatus status) {
        Mechanic mechanic = findMechanicOrThrow(mechanicId);

        if (!Boolean.TRUE.equals(mechanic.getIsVerified()) && status == MechanicStatus.ACTIVE) {
            throw new BadRequestException("Cannot activate unverified mechanic");
        }

        mechanic.setStatus(status);
        return mechanicMapper.toResponse(mechanicRepository.save(mechanic));
    }

    private Mechanic findMechanicOrThrow(Long id) {
        return mechanicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mechanic not found with id: " + id));
    }
}
