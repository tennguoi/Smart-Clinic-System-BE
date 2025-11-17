package com.example.demo.controller;

import com.example.demo.dto.AssignRoomRequest;
import com.example.demo.dto.QueueRequest;
import com.example.demo.dto.QueueResponse;
import com.example.demo.service.QueueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/queues")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TIEP_TAN', 'ADMIN', 'BAC_SI')")
public class QueueController {

    private final QueueService queueService;

    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('TIEP_TAN', 'ADMIN')")
    public ResponseEntity<QueueResponse> checkIn(@Valid @RequestBody QueueRequest request) {
        try {
            QueueResponse response = queueService.checkIn(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/assign-room")
    @PreAuthorize("hasAnyRole('TIEP_TAN', 'ADMIN')")
    public ResponseEntity<QueueResponse> assignRoom(@Valid @RequestBody AssignRoomRequest request) {
        try {
            QueueResponse response = queueService.assignRoom(request.queueId(), request.roomId());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{queueId}/complete")
    @PreAuthorize("hasAnyRole('BAC_SI', 'ADMIN')")
    public ResponseEntity<QueueResponse> completeExamination(
            @PathVariable UUID queueId,
            @RequestParam(required = false) UUID medicalRecordId) {
        try {
            QueueResponse response = queueService.completeExamination(queueId, medicalRecordId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/waiting")
    public ResponseEntity<List<QueueResponse>> getWaitingQueues() {
        List<QueueResponse> queues = queueService.getWaitingQueues();
        return ResponseEntity.ok(queues);
    }

    @GetMapping("/in-progress")
    public ResponseEntity<List<QueueResponse>> getInProgressQueues() {
        List<QueueResponse> queues = queueService.getInProgressQueues();
        return ResponseEntity.ok(queues);
    }

    @GetMapping("/{queueId}")
    public ResponseEntity<QueueResponse> getQueueById(@PathVariable UUID queueId) {
        try {
            QueueResponse response = queueService.getQueueById(queueId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

