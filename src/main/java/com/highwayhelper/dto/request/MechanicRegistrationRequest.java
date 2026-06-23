package com.highwayhelper.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MechanicRegistrationRequest {

    @NotBlank(message = "Shop name is required")
    @Size(max = 200)
    private String shopName;

    @NotBlank(message = "Owner name is required")
    @Size(max = 150)
    private String ownerName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid alternate phone number format")
    private String alternatePhoneNumber;

    @NotBlank(message = "Address is required")
    @Size(max = 500)
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100)
    private String state;

    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    private BigDecimal longitude;

    @NotBlank(message = "Services offered is required")
    @Size(max = 1000)
    private String servicesOffered;

    @Size(max = 500)
    private String operatingHours;

    @Min(value = 0, message = "Years of experience cannot be negative")
    @Max(value = 60, message = "Years of experience seems unrealistic")
    private Integer yearsOfExperience;
}
