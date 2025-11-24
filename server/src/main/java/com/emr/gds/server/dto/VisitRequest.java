package com.emr.gds.server.dto;

import java.time.LocalDateTime;

public record VisitRequest(
        LocalDateTime occurredAt,
        String reason,
        String notes
) {
}
