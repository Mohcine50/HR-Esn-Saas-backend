package com.shegami.hr_saas.modules.auth.mapper;

import com.shegami.hr_saas.modules.auth.dto.UserDto;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.hr.entity.Employee;
import com.shegami.hr_saas.modules.hr.mapper.EmployeeMapper;
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

    UserDto toDto(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    User partialUpdate(UserDto userDto, @MappingTarget User user);
}