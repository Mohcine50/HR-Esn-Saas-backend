package com.shegami.hr_saas.modules.mission.mapper;

import com.shegami.hr_saas.modules.auth.mapper.TenantMapper;
import com.shegami.hr_saas.modules.hr.dto.ClientDto;
import com.shegami.hr_saas.modules.mission.entity.Client;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {TenantMapper.class})
public interface ClientMapper {
    Client toEntity(ClientDto clientDto);

    ClientDto toDto(Client client);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Client partialUpdate(ClientDto clientDto, @MappingTarget Client client);
}