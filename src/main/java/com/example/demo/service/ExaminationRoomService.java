package com.example.demo.service;

import com.example.demo.dto.ExaminationRoomResponse;
import com.example.demo.entity.ExaminationRoom;
import com.example.demo.repository.ExaminationRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExaminationRoomService {

    private final ExaminationRoomRepository examinationRoomRepository;

    public List<ExaminationRoomResponse> getAllRooms() {
        return examinationRoomRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ExaminationRoomResponse> getAvailableRooms() {
        return examinationRoomRepository.findAvailableRooms()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ExaminationRoomResponse getRoomById(UUID roomId) {
        return examinationRoomRepository.findById(roomId)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
    }

    private ExaminationRoomResponse toResponse(ExaminationRoom room) {
        return new ExaminationRoomResponse(
                room.getRoomId(),
                room.getRoomNumber(),
                room.getRoomName(),
                room.getStatus(),
                room.getCurrentQueue() != null ? room.getCurrentQueue().getQueueId() : null,
                room.getCurrentQueue() != null ? room.getCurrentQueue().getQueueNumber() : null,
                room.getAssignedDoctor() != null ? room.getAssignedDoctor().getUserId() : null,
                room.getAssignedDoctor() != null ? room.getAssignedDoctor().getFullName() : null,
                room.getCreatedAt()
        );
    }
}

