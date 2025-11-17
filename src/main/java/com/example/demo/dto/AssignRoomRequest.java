package com.example.demo.dto;

import java.util.UUID;

public record AssignRoomRequest(
        UUID queueId,
        UUID roomId
) {}

