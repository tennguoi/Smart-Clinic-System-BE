package com.example.demo.service;

import com.example.demo.dto.AssignRoomRequest;
import com.example.demo.dto.doctor.DoctorSimpleResponse;
import com.example.demo.dto.ExaminationRoomRequest;
import com.example.demo.dto.ExaminationRoomResponse;
import com.example.demo.entity.ExaminationRoom;
import com.example.demo.entity.Queue;
import com.example.demo.entity.User;
import com.example.demo.entity.UserRole;
import com.example.demo.repository.ExaminationRoomRepository;
import com.example.demo.repository.QueueRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExaminationRoomService {

    @Autowired
    private ExaminationRoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private QueueRepository queueRepository;

    public List<ExaminationRoomResponse> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ExaminationRoomResponse> getActiveRooms() {
        return roomRepository.findByIsActiveTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách phòng sẵn sàng để phân phòng
     * Chỉ trả về phòng: có bác sĩ, đang active, và status = Available
     */
    public List<ExaminationRoomResponse> getAvailableRoomsForAssignment() {
        return roomRepository.findByIsActiveTrue().stream()
                .filter(room -> room.getDoctor() != null)
                .filter(room -> room.getStatus() == ExaminationRoom.RoomStatus.Available)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy phòng active của một bác sĩ cụ thể (nếu có)
     * Dùng để đề xuất phòng mặc định khi phân phòng
     */
    public Optional<ExaminationRoomResponse> getRoomByDoctorId(UUID doctorId) {
        return roomRepository.findByDoctorIdAndIsActiveTrue(doctorId)
                .map(this::toResponse);
    }

    public List<ExaminationRoomResponse> getRoomsByStatus(String status) {
        ExaminationRoom.RoomStatus roomStatus = ExaminationRoom.RoomStatus.valueOf(status);
        return roomRepository.findByStatus(roomStatus).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ExaminationRoomResponse> searchRooms(String keyword) {
        return roomRepository.searchActiveRooms(keyword).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ExaminationRoomResponse getRoomById(UUID roomId) {
        ExaminationRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng khám với ID: " + roomId));
        return toResponse(room);
    }

    public ExaminationRoomResponse createRoom(ExaminationRoomRequest request) {
        if (roomRepository.findByRoomName(request.getRoomName()).isPresent()) {
            throw new RuntimeException("Tên phòng khám đã tồn tại: " + request.getRoomName());
        }

        ExaminationRoom room = ExaminationRoom.builder()
                .roomName(request.getRoomName())
                .currentQueueId(request.getCurrentQueueId())
                .status(ExaminationRoom.RoomStatus.valueOf(request.getStatus()))
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        if (request.getDoctorId() != null) {
            User doctor = userRepository.findById(request.getDoctorId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ với ID: " + request.getDoctorId()));
            
            List<ExaminationRoom> existingRooms = roomRepository.findActiveRoomsByDoctorId(doctor.getUserId());
            if (!existingRooms.isEmpty()) {
                for (ExaminationRoom oldRoom : existingRooms) {
                    oldRoom.setDoctor(null);
                    roomRepository.save(oldRoom);
                }
            }
            
            room.setDoctor(doctor);
        }

        room = roomRepository.save(room);
        return toResponse(room);
    }

    public ExaminationRoomResponse updateRoom(UUID roomId, ExaminationRoomRequest request) {
        ExaminationRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng khám với ID: " + roomId));

        if (!room.getRoomName().equals(request.getRoomName())) {
            if (roomRepository.findByRoomName(request.getRoomName()).isPresent()) {
                throw new RuntimeException("Tên phòng khám đã tồn tại: " + request.getRoomName());
            }
        }

        room.setRoomName(request.getRoomName());
        room.setCurrentQueueId(request.getCurrentQueueId());
        room.setStatus(ExaminationRoom.RoomStatus.valueOf(request.getStatus()));
        if (request.getIsActive() != null) {
            room.setIsActive(request.getIsActive());
        }

        if (request.getDoctorId() != null) {
            User doctor = userRepository.findById(request.getDoctorId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ với ID: " + request.getDoctorId()));
            
            List<ExaminationRoom> existingRooms = roomRepository.findActiveRoomsByDoctorIdExcludingRoom(
                    doctor.getUserId(), roomId);
            if (!existingRooms.isEmpty()) {
                for (ExaminationRoom oldRoom : existingRooms) {
                    oldRoom.setDoctor(null);
                    roomRepository.save(oldRoom);
                }
            }
            
            room.setDoctor(doctor);
        } else {
            room.setDoctor(null);
        }

        room = roomRepository.save(room);
        return toResponse(room);
    }

    public void deleteRoom(UUID roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new RuntimeException("Không tìm thấy phòng khám với ID: " + roomId);
        }
        roomRepository.deleteById(roomId);
    }

    public List<DoctorSimpleResponse> getAllDoctors() {
        List<UserRole> doctorUserRoles = userRoleRepository.findByRoleName("bac_si");
        List<User> allDoctors = doctorUserRoles.stream()
                .map(UserRole::getUser)
                .filter(user -> user.getIsVerified() != null && user.getIsVerified())
                .collect(Collectors.toList());
        
        List<ExaminationRoom> activeRoomsWithDoctors = roomRepository.findByIsActiveTrue().stream()
                .filter(room -> room.getDoctor() != null)
                .collect(Collectors.toList());
        
        java.util.Set<UUID> doctorsWithRooms = activeRoomsWithDoctors.stream()
                .map(room -> room.getDoctor().getUserId())
                .collect(Collectors.toSet());
        
        return allDoctors.stream()
                .filter(doctor -> !doctorsWithRooms.contains(doctor.getUserId()))
                .map(user -> DoctorSimpleResponse.builder()
                        .doctorId(user.getUserId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .build())
                .collect(Collectors.toList());
    }

    public List<DoctorSimpleResponse> getAvailableDoctorsForUpdate(UUID currentRoomId) {
        List<DoctorSimpleResponse> availableDoctors = getAllDoctors();
        
        if (currentRoomId != null) {
            ExaminationRoom currentRoom = roomRepository.findById(currentRoomId).orElse(null);
            if (currentRoom != null && currentRoom.getDoctor() != null) {
                User currentDoctor = currentRoom.getDoctor();
                boolean alreadyInList = availableDoctors.stream()
                        .anyMatch(d -> d.getDoctorId().equals(currentDoctor.getUserId()));
                
                if (!alreadyInList) {
                    DoctorSimpleResponse currentDoctorResponse = DoctorSimpleResponse.builder()
                            .doctorId(currentDoctor.getUserId())
                            .fullName(currentDoctor.getFullName())
                            .email(currentDoctor.getEmail())
                            .phone(currentDoctor.getPhone())
                            .build();
                    availableDoctors.add(currentDoctorResponse);
                }
            }
        }
        
        return availableDoctors;
    }

    public List<ExaminationRoomResponse> getRoomsByDoctor(UUID doctorId) {
        return roomRepository.findByDoctorUserId(doctorId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Map<String, Long> getRoomCountByDoctor() {
        List<ExaminationRoom> allRooms = roomRepository.findAll();
        return allRooms.stream()
                .filter(room -> room.getDoctor() != null && room.getIsActive() != null && room.getIsActive())
                .collect(Collectors.groupingBy(
                        room -> room.getDoctor().getFullName(),
                        Collectors.counting()
                ));
    }

    @Transactional
    public ExaminationRoomResponse assignPatientToRoom(AssignRoomRequest request) {
        ExaminationRoom room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng khám với ID: " + request.getRoomId()));

        if (!room.getIsActive()) {
            throw new RuntimeException("Phòng khám đang không hoạt động");
        }

        if (room.getDoctor() == null) {
            throw new RuntimeException("Phòng khám chưa được gán bác sĩ, vui lòng gán bác sĩ trước khi phân bệnh nhân");
        }

        if (room.getStatus() == ExaminationRoom.RoomStatus.Occupied) {
            throw new RuntimeException("Phòng khám đang bận, vui lòng chọn phòng khác");
        }

        Queue queue = queueRepository.findById(request.getQueueId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bệnh nhân trong hàng đợi với ID: " + request.getQueueId()));

        if (queue.getStatus() != Queue.QueueStatus.Waiting) {
            throw new RuntimeException("Bệnh nhân không ở trạng thái chờ khám (status: " + queue.getStatus() + ")");
        }

        queue.setAssignedRoom(room);
        queue.setStatus(Queue.QueueStatus.InProgress);
        queue.setStartTime(LocalDateTime.now());
        queueRepository.save(queue);

        room.setCurrentQueueId(queue.getQueueId());
        room.setStatus(ExaminationRoom.RoomStatus.Occupied);
        ExaminationRoom savedRoom = roomRepository.save(room);

        return toResponse(savedRoom);
    }

    private ExaminationRoomResponse toResponse(ExaminationRoom room) {
        return ExaminationRoomResponse.builder()
                .roomId(room.getRoomId())
                .roomName(room.getRoomName())
                .doctorId(room.getDoctor() != null ? room.getDoctor().getUserId() : null)
                .doctorName(room.getDoctor() != null ? room.getDoctor().getFullName() : null)
                .currentQueueId(room.getCurrentQueueId())
                .status(room.getStatus().name())
                .isActive(room.getIsActive())
                .build();
    }
}

