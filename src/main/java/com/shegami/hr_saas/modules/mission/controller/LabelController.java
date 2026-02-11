package com.shegami.hr_saas.modules.mission.controller;


import com.shegami.hr_saas.modules.mission.dto.LabelDto;
import com.shegami.hr_saas.modules.mission.service.LabelsService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("api/labels")
@Slf4j
public class LabelController {


    private final LabelsService labelsService;
        @GetMapping
        public ResponseEntity<Page<LabelDto>> getAllLabels(
                @PageableDefault(size = 10) Pageable pageable
        ) {
            return ResponseEntity.ok(labelsService.getAllLabels(pageable));
        }

        @PostMapping
        public ResponseEntity<LabelDto> createLabel(@Valid @RequestBody LabelDto labelDto) {
            log.info("Creating new label: {}", labelDto.getLabelName());
            LabelDto created = labelsService.saveLabel(labelDto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        }

        @PutMapping("/{id}")
        public ResponseEntity<LabelDto> updateLabel(
                @PathVariable String id,
                @Valid @RequestBody LabelDto labelDto
        ) {
            log.info("Updating label ID: {}", id);
            return ResponseEntity.ok(labelsService.updateLabel(labelDto));
        }


        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteLabel(@PathVariable String id) {
            log.warn("Deleting label ID: {}", id);
            labelsService.deleteLabel(id);
            return ResponseEntity.noContent().build();
        }

        @GetMapping("/search")
        public ResponseEntity<List<LabelDto>> searchLabels(@RequestParam("q") String query) {
            return ResponseEntity.ok(labelsService.searchLabels(query));
        }
    }



