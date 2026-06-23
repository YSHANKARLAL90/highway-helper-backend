package com.highwayhelper.dto.request;

import com.highwayhelper.entity.enums.ServiceRequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateServiceRequestStatusRequest {

    @NotNull(message = "Status is required")
    private ServiceRequestStatus status;
}
