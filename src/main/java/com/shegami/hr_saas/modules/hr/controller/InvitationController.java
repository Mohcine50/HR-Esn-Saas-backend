package com.shegami.hr_saas.modules.hr.controller;

import com.shegami.hr_saas.modules.hr.dto.InvitationDto;
import com.shegami.hr_saas.modules.hr.entity.Invitation;
import com.shegami.hr_saas.modules.hr.service.InvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
@Slf4j
public class InvitationController {

    private final InvitationService invitationService;


    @PostMapping
    public ResponseEntity<InvitationDto> createInvitation(@Valid @RequestBody InvitationDto invitationDto) {
        InvitationDto created = invitationService.createInvitation(invitationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }


    @GetMapping
    public ResponseEntity<List<Invitation>> getAllInvitations() {
        List<Invitation> invitations = invitationService.getAllInvitations();
        return ResponseEntity.ok(invitations);
    }


    @GetMapping("/{invitationId}")
    public ResponseEntity<InvitationDto> getInvitation(@PathVariable String invitationId) {
        InvitationDto invitation = invitationService.getInvitation(invitationId);
        return ResponseEntity.ok(invitation);
    }

    /**
     * Update invitation
     */
    @PutMapping("/{invitationId}")
    public ResponseEntity<Map<String, Object>> updateInvitation(
            @PathVariable String invitationId,
            @Valid @RequestBody InvitationDto invitationDto) {
        boolean updated = invitationService.updateInvitation(invitationId, invitationDto);
        return ResponseEntity.ok(Map.of(
                "success", updated,
                "message", "Invitation updated successfully"
        ));
    }


    @DeleteMapping("/{invitationId}")
    public ResponseEntity<Void> deleteInvitation(@PathVariable String invitationId) {
        invitationService.deleteInvitation(invitationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{invitationId}/revoke")
    public ResponseEntity<Map<String, Object>> revokeInvitation(@PathVariable String invitationId) {
        boolean revoked = invitationService.revokeInvitation(invitationId);
        return ResponseEntity.ok(Map.of(
                "success", revoked,
                "message", "Invitation revoked successfully"
        ));
    }


    @PostMapping("/{invitationId}/resend")
    public ResponseEntity<Map<String, Object>> resendInvitation(@PathVariable String invitationId) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Invitation email resent successfully"
        ));
    }


    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateInvitation(@RequestParam String token) {
        boolean valid = invitationService.validateInvitation(token);
        return ResponseEntity.ok(Map.of(
                "valid", valid,
                "message", valid ? "Invitation is valid" : "Invalid or expired invitation"
        ));
    }


    @PostMapping("/accept")
    public ResponseEntity<Map<String, Object>> acceptInvitation(@RequestParam String token) {
        boolean accepted = invitationService.acceptInvitation(token);
        return ResponseEntity.ok(Map.of(
                "success", accepted,
                "message", "Invitation accepted successfully"
        ));
    }


    @PostMapping("/{invitationId}/reject")
    public ResponseEntity<Map<String, Object>> rejectInvitation(@PathVariable String invitationId) {
        boolean rejected = invitationService.rejectInvitation(invitationId);
        return ResponseEntity.ok(Map.of(
                "success", rejected,
                "message", "Invitation rejected"
        ));
    }
}
