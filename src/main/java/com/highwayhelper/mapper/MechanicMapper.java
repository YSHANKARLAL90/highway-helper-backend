package com.highwayhelper.mapper;

import com.highwayhelper.dto.request.MechanicRegistrationRequest;
import com.highwayhelper.dto.response.MechanicResponse;
import com.highwayhelper.entity.Mechanic;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MechanicMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "isVerified", constant = "false")
    @Mapping(target = "status", expression = "java(com.highwayhelper.entity.enums.MechanicStatus.INACTIVE)")
    @Mapping(target = "createdAt", ignore = true)
    Mechanic toEntity(MechanicRegistrationRequest request);

    @Mapping(source = "user.id", target = "userId")
    MechanicResponse toResponse(Mechanic mechanic);
}
