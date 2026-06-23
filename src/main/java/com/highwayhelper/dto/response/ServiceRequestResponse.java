package com.highwayhelper.dto.response;

import com.highwayhelper.entity.enums.ServiceRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequestResponse {

    private Long id;
    private Long userId;
    private String userFullName;
    private Long mechanicId;
    private String mechanicShopName;
    private String vehicleType;
    private String vehicleNumber;
    private String issueType;
    private String description;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private ServiceRequestStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;
}
