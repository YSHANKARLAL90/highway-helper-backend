package com.highwayhelper.mapper;

import com.highwayhelper.dto.request.MechanicRegistrationRequest;
import com.highwayhelper.dto.request.RegisterRequest;
import com.highwayhelper.dto.response.MechanicResponse;
import com.highwayhelper.dto.response.ServiceRequestResponse;
import com.highwayhelper.dto.response.UserResponse;
import com.highwayhelper.entity.Mechanic;
import com.highwayhelper.entity.ServiceRequest;
import com.highwayhelper.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(RegisterRequest request);

    UserResponse toResponse(User user);
}
