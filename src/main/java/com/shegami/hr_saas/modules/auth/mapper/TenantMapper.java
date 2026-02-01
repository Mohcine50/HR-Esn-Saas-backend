package com.shegami.hr_saas.modules.auth.mapper;


import com.shegami.hr_saas.modules.auth.dto.TenantDto;
import com.shegami.hr_saas.modules.auth.entity.Tenant;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TenantMapper {

    TenantDto toDto(Tenant tenant);

    Tenant fromDto(TenantDto TenantDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Tenant partialUpdate(TenantDto tenantDto, @MappingTarget Tenant tenant);
}
