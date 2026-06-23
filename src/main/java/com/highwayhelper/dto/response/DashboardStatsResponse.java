package com.highwayhelper.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    private long totalUsers;
    private long totalMechanics;
    private long verifiedMechanics;
    private long pendingVerifications;
    private long totalServiceRequests;
    private long pendingRequests;
    private long activeRequests;
    private long completedRequests;
    private long cancelledRequests;
}
