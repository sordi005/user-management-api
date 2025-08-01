package com.sordi.userManagement.model.dto.mapper;

import com.sordi.userManagement.model.User;
import com.sordi.userManagement.model.dto.request.CreateUserRequest;
import com.sordi.userManagement.model.dto.request.UpdateUserRequest;
import com.sordi.userManagement.model.dto.response.UserResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {

    @Mappings({
            @Mapping(target = "fullName", expression ="java(buildFullName(user.getFirstName(), user.getLastName()))"),
    })
    UserResponse toResponse(User user);

    @Mappings({
            @Mapping(target = "id", ignore = true), // Este campo no debe mappearse
            @Mapping(target = "createdAt", ignore = true), // Este campo se maneja automáticamente
            @Mapping(target = "updatedAt", ignore = true)
    })
    User toEntity(CreateUserRequest request);

    @Mappings({
            @Mapping(target = "id", ignore = true), // Este campo no debe mappearse
            @Mapping(target = "dni", ignore = true), // DNI no se puede actualizar
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "username", ignore = true),
            @Mapping(target = "password", ignore = true)
    })
    void updateEntity(@MappingTarget User user , UpdateUserRequest request);

    List<UserResponse> toResponseList(List<User>users) ;

    default String buildFullName(String firstName, String lastName){
        if(firstName == null && lastName == null) return "";
        if(firstName == null) return lastName;
        if(lastName == null) return firstName;
        return firstName + " " + lastName;
    }

}
