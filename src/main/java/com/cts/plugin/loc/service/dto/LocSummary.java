package com.cts.plugin.loc.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LocSummary — aggregated LOC stats returned by the summary endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocSummary {

    private String developerId;
    private String projectId;
    private String genAiTool;
    private String fromTimestamp;
    private String toTimestamp;

    private long   totalEvents;
    private long   genAiEvents;
    private long   manualEvents;

    private long   totalLinesAdded;
    private long   totalLinesModified;
    private long   totalLinesDeleted;

    private long   genAiLinesAdded;
    private long   manualLinesAdded;

    /** genAiLinesAdded / totalLinesAdded * 100  (0 if totalLinesAdded == 0) */
    private double genAiAdoptionPct;

    /** manual contribution percentage (100 - genAiAdoptionPct) */
    private double manualContributionPct;

    private double avgConfidenceScore;
}

