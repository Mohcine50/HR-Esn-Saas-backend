package com.shegami.hr_saas.modules.hr.service;

import com.shegami.hr_saas.modules.hr.dto.AcceptInvitationDto;
import com.shegami.hr_saas.modules.hr.dto.InvitationDto;
import com.shegami.hr_saas.modules.hr.dto.InvitationRequestDto;
import com.shegami.hr_saas.modules.hr.dto.InvitationValidationResponse;
import com.shegami.hr_saas.modules.hr.entity.Invitation;

import java.util.List;

public interface InvitationService {
    boolean revokeInvitation(String invitationId);
    InvitationDto getInvitation(String invitationId);
    List<Invitation> getAllInvitations();
    InvitationDto createInvitation(InvitationRequestDto invitationDto);
    boolean updateInvitation(String invitationId, InvitationDto invitationDto);
    void deleteInvitation(String invitationId);
    boolean acceptInvitation(String token, AcceptInvitationDto acceptInvitationDto);
    boolean rejectInvitation(String invitationId);
    InvitationValidationResponse validateInvitation(String token);

}
