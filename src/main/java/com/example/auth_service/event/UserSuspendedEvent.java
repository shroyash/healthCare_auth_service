package com.example.auth_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSuspendedEvent {

    private UUID userId;
    private String username;
    private String email;
    private String role;
    private String reason;
    private LocalDateTime suspendedAt;
    private boolean suspended;
}
