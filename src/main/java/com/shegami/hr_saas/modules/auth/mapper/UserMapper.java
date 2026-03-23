package com.shegami.hr_saas.modules.auth.mapper;

import com.shegami.hr_saas.modules.auth.dto.UserDto;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.hr.entity.Employee;
import com.shegami.hr_saas.modules.hr.mapper.EmployeeMapper;
import com.shegami.hr_saas.modules.upload.entity.UploadFile;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {TenantMapper.class, UserRoleMapper.class, EmployeeMapper.class, UserSettingsMapper.class})
public interface UserMapper {
    User toEntity(UserDto userDto);

    @AfterMapping
    default void linkEmployee(@MappingTarget User user) {
        Employee employee = user.getEmployee();
        if (employee != null) {
            employee.setUser(user);
        }
    }


    @Mapping(target = "profileUrl", expression = "java(resolveProfileUrl(user))")
    UserDto toDto(User user);

    default String resolveProfileUrl(User user) {
        if (user.getImageUrl() == null) return null;
        UploadFile img = user.getImageUrl();
        if (img.isPublic() && img.getPublicUrl() != null) {
            return img.getPublicUrl();
        }
        return null;
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    User partialUpdate(UserDto userDto, @MappingTarget User user);
}