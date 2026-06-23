package com.highwayhelper.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignMechanicRequest {

    @NotNull(message = "Mechanic ID is required")
    private Long mechanicId;
}
