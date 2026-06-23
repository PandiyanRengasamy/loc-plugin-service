package com.cts.plugin.loc.service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * LocEventRequest — inbound DTO received from the IntelliJ plugin.
 * Matches the JSON payload sent by EventDispatcher.
 *
 * The {@link #fileChanges} list contains per-file LOC details when Copilot
 * modified multiple files in a single "Keep All" action. The top-level
 * linesAdded/Modified/Deleted hold the aggregated totals.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocEventRequest {

    @NotBlank(message = "developerId is required")
    private String developerId;

    private String developerName;

    @NotBlank(message = "projectId is required")
    private String projectId;

    private String sprintId;

    @NotBlank(message = "filePath is required")
    private String filePath;

    @NotBlank(message = "fileName is required")
    private String fileName;

    /** Java class name (without .java extension) that was added/modified/deleted. */
    private String className;

    private String ideType;

    @NotBlank(message = "genAiTool is required")
    private String genAiTool;

    private String developmentMode;

    @PositiveOrZero
    private int linesAdded;

    @PositiveOrZero
    private int linesModified;

    @PositiveOrZero
    private int linesDeleted;

    @NotNull
    private Boolean genAiGenerated;

    private Double genAiConfidenceScore;

    @NotBlank(message = "eventTimestamp is required")
    private String eventTimestamp;

    @NotBlank(message = "sessionId is required")
    private String sessionId;

    /** LLM model name used for code generation (e.g. "gpt-4o", "claude-3.5-sonnet", "gemini-pro"). */
    private String llmModel;

    /** Agent name that generated the code (e.g. "Copilot", "Claude Code", "Gemini"). */
    private String agentName;

    /**
     * Location where the AI-generated changes were accepted.
     * Values: CODE_EDITOR | COPILOT_CHAT | CLAUDE_CHAT | INLINE_SUGGESTION | UNKNOWN
     */
    private String acceptedLocation;

    /**
     * Per-file change details. When Copilot modifies multiple files in a single
     * "Keep All" action, each file's LOC breakdown is stored here.
     * May be null or empty for single-file or legacy events.
     */
    private List<FileChangeDto> fileChanges;

    /** Total number of files updated in this event. */
    @PositiveOrZero
    private int totalFilesUpdated;

    /** Total number of files added in this event. */
    @PositiveOrZero
    private int totalFilesAdded;

    /** Total number of files deleted in this event. */
    @PositiveOrZero
    private int totalFilesDeleted;

    /** Number of input (prompt) tokens consumed by the LLM for this event. */
    @PositiveOrZero
    private Integer inputTokens;

    /** Number of output (completion) tokens produced by the LLM for this event. */
    @PositiveOrZero
    private Integer outputTokens;

    /**
     * Percentage of lines in this event that were written by a human (not GenAI).
     * Optional, may be null if not set by the user/plugin.
     */
    @DecimalMin(value = "0.0", inclusive = true, message = "humanLocPercent must be between 0 and 100")
    private Double humanLocPercent;
}
