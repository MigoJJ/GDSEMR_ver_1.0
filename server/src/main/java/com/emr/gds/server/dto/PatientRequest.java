package com.emr.gds.server.dto;

import java.time.LocalDate;
import java.util.List;

public record PatientRequest(
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String phone,
        List<VisitRequest> visits
) {
}
