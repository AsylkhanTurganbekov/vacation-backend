package com.company.vacation.mapper;

import com.company.vacation.dto.user.UserResponse;
import com.company.vacation.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}
