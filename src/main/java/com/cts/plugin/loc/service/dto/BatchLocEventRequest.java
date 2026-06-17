package com.cts.plugin.loc.service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * BatchLocEventRequest — wraps a list of events for the batch POST endpoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchLocEventRequest {

    @NotEmpty(message = "events list must not be empty")
    @Valid
    private List<LocEventRequest> events;
}

