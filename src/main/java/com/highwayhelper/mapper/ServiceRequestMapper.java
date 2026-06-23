package com.highwayhelper.mapper;

import com.highwayhelper.dto.response.ServiceRequestResponse;
import com.highwayhelper.entity.ServiceRequest;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ServiceRequestMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "userFullName")
    @Mapping(source = "mechanic.id", target = "mechanicId")
    @Mapping(source = "mechanic.shopName", target = "mechanicShopName")
    ServiceRequestResponse toResponse(ServiceRequest serviceRequest);
}
