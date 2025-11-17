package com.example.demo.service;

import com.example.demo.dto.DoctorSimpleResponse;
import com.example.demo.dto.ExaminationRoomRequest;
import com.example.demo.dto.ExaminationRoomResponse;
import com.example.demo.entity.ExaminationRoom;
import com.example.demo.entity.User;
import com.example.demo.entity.UserRole;
import com.example.demo.repository.ExaminationRoomRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
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
        // Kiểm tra tên phòng đã tồn tại chưa
        if (roomRepository.findByRoomName(request.getRoomName()).isPresent()) {
            throw new RuntimeException("Tên phòng khám đã tồn tại: " + request.getRoomName());
        }

        ExaminationRoom room = ExaminationRoom.builder()
                .roomName(request.getRoomName())
                .currentQueueId(request.getCurrentQueueId())
                .status(ExaminationRoom.RoomStatus.valueOf(request.getStatus()))
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        // Gán bác sĩ nếu có
        if (request.getDoctorId() != null) {
            User doctor = userRepository.findById(request.getDoctorId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ với ID: " + request.getDoctorId()));
            
            // Kiểm tra bác sĩ đã có phòng active chưa (ràng buộc: 1 bác sĩ chỉ ở 1 phòng)
            List<ExaminationRoom> existingRooms = roomRepository.findActiveRoomsByDoctorId(doctor.getUserId());
            if (!existingRooms.isEmpty()) {
                // Tự động gỡ bác sĩ khỏi phòng cũ
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

        // Kiểm tra tên phòng trùng (nếu đổi tên)
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

        // Cập nhật bác sĩ
        if (request.getDoctorId() != null) {
            User doctor = userRepository.findById(request.getDoctorId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ với ID: " + request.getDoctorId()));
            
            // Kiểm tra bác sĩ đã có phòng active khác chưa (trừ phòng hiện tại)
            // Ràng buộc: 1 bác sĩ chỉ ở 1 phòng
            List<ExaminationRoom> existingRooms = roomRepository.findActiveRoomsByDoctorIdExcludingRoom(
                    doctor.getUserId(), roomId);
            if (!existingRooms.isEmpty()) {
                // Tự động gỡ bác sĩ khỏi phòng cũ
                for (ExaminationRoom oldRoom : existingRooms) {
                    oldRoom.setDoctor(null);
                    roomRepository.save(oldRoom);
                }
            }
            
            room.setDoctor(doctor);
        } else {
            // Gỡ bác sĩ khỏi phòng
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

    /**
     * Lấy danh sách bác sĩ (role = "bac_si") chưa có phòng active để lễ tân chọn khi tạo phòng mới
     * Ràng buộc: 1 bác sĩ chỉ ở 1 phòng, nên bác sĩ đã có phòng active sẽ không hiển thị
     */
    public List<DoctorSimpleResponse> getAllDoctors() {
        // Lấy tất cả bác sĩ đã verified
        List<UserRole> doctorUserRoles = userRoleRepository.findByRoleName("bac_si");
        List<User> allDoctors = doctorUserRoles.stream()
                .map(UserRole::getUser)
                .filter(user -> user.getIsVerified() != null && user.getIsVerified())
                .collect(Collectors.toList());
        
        // Lấy danh sách ID của bác sĩ đã có phòng active (tối ưu: chỉ query phòng active)
        List<ExaminationRoom> activeRoomsWithDoctors = roomRepository.findByIsActiveTrue().stream()
                .filter(room -> room.getDoctor() != null)
                .collect(Collectors.toList());
        
        // Tạo set các doctorId đã có phòng active
        java.util.Set<UUID> doctorsWithRooms = activeRoomsWithDoctors.stream()
                .map(room -> room.getDoctor().getUserId())
                .collect(Collectors.toSet());
        
        // Filter: chỉ lấy bác sĩ chưa có phòng active
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

    /**
     * Lấy danh sách bác sĩ để chọn khi cập nhật phòng
     * Bao gồm: bác sĩ chưa có phòng active + bác sĩ hiện tại của phòng đang được cập nhật
     * @param currentRoomId ID của phòng đang được cập nhật (null nếu là tạo mới)
     */
    public List<DoctorSimpleResponse> getAvailableDoctorsForUpdate(UUID currentRoomId) {
        // Lấy danh sách bác sĩ chưa có phòng active
        List<DoctorSimpleResponse> availableDoctors = getAllDoctors();
        
        // Nếu đang cập nhật phòng, thêm bác sĩ hiện tại của phòng đó vào danh sách
        if (currentRoomId != null) {
            ExaminationRoom currentRoom = roomRepository.findById(currentRoomId).orElse(null);
            if (currentRoom != null && currentRoom.getDoctor() != null) {
                User currentDoctor = currentRoom.getDoctor();
                // Kiểm tra xem bác sĩ hiện tại đã có trong danh sách chưa
                boolean alreadyInList = availableDoctors.stream()
                        .anyMatch(d -> d.getDoctorId().equals(currentDoctor.getUserId()));
                
                // Nếu chưa có, thêm vào
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

    /**
     * Lấy danh sách phòng theo bác sĩ (bao gồm cả phòng inactive để xem lịch sử)
     */
    public List<ExaminationRoomResponse> getRoomsByDoctor(UUID doctorId) {
        return roomRepository.findByDoctorUserId(doctorId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Thống kê: Mỗi bác sĩ đang phụ trách bao nhiêu phòng active
     * Lưu ý: Với ràng buộc 1 bác sĩ chỉ ở 1 phòng, mỗi bác sĩ sẽ có tối đa 1 phòng active
     */
    public Map<String, Long> getRoomCountByDoctor() {
        List<ExaminationRoom> allRooms = roomRepository.findAll();
        return allRooms.stream()
                .filter(room -> room.getDoctor() != null && room.getIsActive() != null && room.getIsActive())
                .collect(Collectors.groupingBy(
                        room -> room.getDoctor().getFullName(),
                        Collectors.counting()
                ));
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

