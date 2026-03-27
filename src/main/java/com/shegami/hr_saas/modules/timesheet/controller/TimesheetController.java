package com.shegami.hr_saas.modules.timesheet.controller;

import com.shegami.hr_saas.modules.timesheet.dto.SaveEntriesRequest;
import com.shegami.hr_saas.modules.timesheet.dto.CreateTimesheetRequest;
import com.shegami.hr_saas.modules.timesheet.dto.TimesheetResponse;
import com.shegami.hr_saas.modules.timesheet.service.TimesheetService;
import com.shegami.hr_saas.modules.timesheet.dto.ReviewTimesheetRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/timesheets")
@RequiredArgsConstructor
public class TimesheetController {
    private final TimesheetService timesheetService;

    /**
     * Create a new timesheet (DRAFT) for a given
     **/
    @PostMapping
    public ResponseEntity<TimesheetResponse> create(@Valid @RequestBody CreateTimesheetRequest req) {

        return ResponseEntity.ok(timesheetService.createTimesheet(req));
    }

    /**
     * Save (batch replace) all entries for a timesheet
     */
    @PutMapping("/{timesheetId}/entries")
    public ResponseEntity<TimesheetResponse> saveEntries(
            @PathVariable String timesheetId,
            @Valid @RequestBody SaveEntriesRequest req) {

        return ResponseEntity.ok(timesheetService.saveEntries(timesheetId, req));
    }

    /**
     * Consultant submits for manager review
     */
    @PostMapping("/{timesheetId}/submit")
    public ResponseEntity<TimesheetResponse> submit(
            @PathVariable String timesheetId) {

        return ResponseEntity.ok(timesheetService.submitTimesheet(timesheetId));
    }

    /**
     * Manager approves or rejects a submitted timesheet
     */
    @PostMapping("/{timesheetId}/review")
    public ResponseEntity<TimesheetResponse> review(
            @PathVariable String timesheetId,
            @Valid @RequestBody ReviewTimesheetRequest req) {
        return ResponseEntity.ok(timesheetService.reviewTimesheet(timesheetId, req));
    }

    /**
     * Manager gets their pending validation queue
     */
    @GetMapping("/pending")
    public ResponseEntity<List<TimesheetResponse>> getPending() {

        return ResponseEntity.ok(timesheetService.getPendingTimesheets());
    }

    /**
     * Get a single timesheet with all its entries
     */
    @GetMapping("/{timesheetId}")
    public ResponseEntity<TimesheetResponse> getOne(@PathVariable String timesheetId) {

        return ResponseEntity.ok(timesheetService.getTimesheet(timesheetId));
    }

    /**
     * Consultant's full history
     */
    @GetMapping("/consultant/{consultantId}")
    public ResponseEntity<List<TimesheetResponse>> getHistory(@PathVariable String consultantId) {

        return ResponseEntity.ok(timesheetService.getConsultantHistory(consultantId));
    }

    @GetMapping("/client/{clientId}/approved")
    public ResponseEntity<List<TimesheetResponse>> getApprovedByClient(@PathVariable String clientId) {
        return ResponseEntity.ok(timesheetService.getApprovedTimesheetsByClient(clientId));
    }

    /**
     * Admin — paginated list of all timesheets for the tenant
     */
    @GetMapping("/all")
    public ResponseEntity<Page<TimesheetResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(timesheetService.getAllTimesheets(pageable));
    }
}
