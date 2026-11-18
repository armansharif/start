package com.project.user.mapper;

import com.project.user.dto.response.UserResponse;
import com.project.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapping from the entity to the response DTO.
 *
 * <p>Display oriented fields (Persian dates, roles) are filled by the service, because they
 * need the formatter and a second query; keeping them out of the mapper avoids injecting
 * services into mapping code.</p>
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserMapper {

    @Mapping(target = "birthDatePersian", ignore = true)
    @Mapping(target = "createdDatePersian", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "status", expression = "java(user.getStatus().name())")
    UserResponse toResponse(User user);
}
