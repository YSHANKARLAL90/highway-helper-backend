package com.highwayhelper.service;

import com.highwayhelper.dto.request.CreateServiceRequestRequest;
import com.highwayhelper.dto.response.ServiceRequestResponse;
import com.highwayhelper.entity.User;
import com.highwayhelper.entity.enums.Role;
import com.highwayhelper.entity.enums.ServiceRequestStatus;
import com.highwayhelper.exception.ForbiddenException;
import com.highwayhelper.mapper.ServiceRequestMapper;
import com.highwayhelper.repository.MechanicRepository;
import com.highwayhelper.repository.ServiceRequestRepository;
import com.highwayhelper.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceRequestServiceTest {

    @Mock
    private ServiceRequestRepository serviceRequestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MechanicRepository mechanicRepository;
    @Mock
    private ServiceRequestMapper serviceRequestMapper;

    @InjectMocks
    private ServiceRequestService serviceRequestService;

    @Test
    void createRequest_shouldSucceedForUserRole() {
        Long userId = 1L;
        User user = User.builder().id(userId).role(Role.USER).build();

        CreateServiceRequestRequest request = CreateServiceRequestRequest.builder()
                .vehicleType("Car")
                .vehicleNumber("MH12AB1234")
                .issueType("Flat Tire")
                .description("Front left tire punctured")
                .latitude(new BigDecimal("19.0760"))
                .longitude(new BigDecimal("72.8777"))
                .build();

        ServiceRequestResponse response = ServiceRequestResponse.builder()
                .id(10L)
                .status(ServiceRequestStatus.PENDING)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(serviceRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(serviceRequestMapper.toResponse(any())).thenReturn(response);

        ServiceRequestResponse result = serviceRequestService.createRequest(userId, request);

        assertThat(result.getStatus()).isEqualTo(ServiceRequestStatus.PENDING);
        verify(serviceRequestRepository).save(any());
    }

    @Test
    void createRequest_shouldRejectNonUserRole() {
        Long userId = 2L;
        User mechanicUser = User.builder().id(userId).role(Role.MECHANIC).build();

        CreateServiceRequestRequest request = CreateServiceRequestRequest.builder()
                .vehicleType("Car")
                .vehicleNumber("MH12AB1234")
                .issueType("Flat Tire")
                .description("Test")
                .latitude(new BigDecimal("19.0760"))
                .longitude(new BigDecimal("72.8777"))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(mechanicUser));

        assertThatThrownBy(() -> serviceRequestService.createRequest(userId, request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Only vehicle owners");
    }
}
