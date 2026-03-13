package com.shegami.hr_saas.modules.mission.mapper;

import com.shegami.hr_saas.modules.auth.mapper.TenantMapper;
import com.shegami.hr_saas.modules.auth.mapper.UserMapper;
import com.shegami.hr_saas.modules.mission.dto.ConsultantDto;
import com.shegami.hr_saas.modules.mission.dto.ProjectSummaryDto;
import com.shegami.hr_saas.modules.mission.entity.Consultant;
import com.shegami.hr_saas.modules.mission.entity.Project;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {TenantMapper.class, UserMapper.class})
public interface ConsultantMapper {
    Consultant toEntity(ConsultantDto consultantDto);

    @Mapping(target = "projects", qualifiedByName = "toProjectSummary")
    ConsultantDto toDto(Consultant consultant);

    @Named("toProjectSummary")
    default List<ProjectSummaryDto> toProjectSummary(Set<Project> projects) {
        if (projects == null) return List.of();
        return projects.stream()
                .map(p -> new ProjectSummaryDto(
                        p.getProjectId(),
                        p.getName(),
                        p.getDescription(),
                        p.getPriority(),
                        p.getProjectStatus(),
                        p.getTags(),
                        p.getCreatedAt(),
                        p.getUpdatedAt()
                ))
                .toList();
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Consultant partialUpdate(ConsultantDto consultantDto, @MappingTarget Consultant consultant);
}