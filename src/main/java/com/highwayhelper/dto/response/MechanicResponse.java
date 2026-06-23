package com.highwayhelper.dto.response;

import com.highwayhelper.entity.enums.MechanicStatus;
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
public class MechanicResponse {

    private Long id;
    private Long userId;
    private String shopName;
    private String ownerName;
    private String phoneNumber;
    private String alternatePhoneNumber;
    private String address;
    private String city;
    private String state;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String servicesOffered;
    private String operatingHours;
    private Integer yearsOfExperience;
    private Boolean isVerified;
    private MechanicStatus status;
    private LocalDateTime createdAt;
}
