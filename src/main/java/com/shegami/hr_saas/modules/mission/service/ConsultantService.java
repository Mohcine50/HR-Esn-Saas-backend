package com.shegami.hr_saas.modules.mission.service;

import com.shegami.hr_saas.modules.mission.dto.ClientDto;
import com.shegami.hr_saas.modules.mission.dto.ConsultantDto;
import com.shegami.hr_saas.modules.mission.entity.Client;
import com.shegami.hr_saas.modules.mission.entity.Consultant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ConsultantService {

    ConsultantDto getConsultantById(String id);
    ConsultantDto getConsultantByEmail(String Email);

    ConsultantDto saveConsultant(ConsultantDto consultantDto);
    ConsultantDto updateConsultant(ConsultantDto consultant);
    void deleteConsultant(String id);

    Page<ConsultantDto> getAllConsultant(Pageable pageable);


}
