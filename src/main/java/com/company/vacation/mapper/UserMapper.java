package com.company.vacation.mapper;

import com.company.vacation.dto.user.UserResponse;
import com.company.vacation.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "avatarUrl",
            expression = "java(user.getAvatarFileName() != null ? \"/api/v1/users/\" + user.getId() + \"/avatar\" : null)")
    UserResponse toResponse(User user);
}
