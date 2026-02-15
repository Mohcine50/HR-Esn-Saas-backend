package com.shegami.hr_saas.modules.hr.service;

import com.shegami.hr_saas.modules.hr.dto.InvitationDto;
import com.shegami.hr_saas.modules.hr.entity.Invitation;

import java.util.List;

public interface InvitationService {
    boolean revokeInvitation(String invitationId);
    InvitationDto getInvitation(String invitationId);
    List<Invitation> getAllInvitations();
    InvitationDto createInvitation(InvitationDto invitationDto);
    boolean updateInvitation(String invitationId, InvitationDto invitationDto);
    boolean deleteInvitation(String invitationId);
    boolean acceptInvitation(String token);
    boolean rejectInvitation(String invitationId);
    boolean validateInvitation(String token);

}
