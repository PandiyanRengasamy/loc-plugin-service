package com.cts.plugin.loc.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * LocEventResponse — outbound DTO returned by the REST API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocEventResponse {

    private String  id;
    private String  developerId;
    private String  developerName;
    private String  projectId;
    private String  sprintId;
    private String  filePath;
    private String  fileName;
    private String  className;
    private String  ideType;
    private String  genAiTool;
    private String  developmentMode;
    private int     linesAdded;
    private int     linesModified;
    private int     linesDeleted;
    private boolean genAiGenerated;
    private Double  genAiConfidenceScore;
    private String  eventTimestamp;
    private String  sessionId;
    /** LLM model name (e.g. "gpt-4o", "claude-3.5-sonnet"). */
    private String  llmModel;
    /** Agent name (e.g. "Copilot", "Claude Code"). */
    private String  agentName;
    /** Where changes were accepted: CODE_EDITOR | COPILOT_CHAT | CLAUDE_CHAT | INLINE_SUGGESTION | UNKNOWN */
    private String  acceptedLocation;
    /** Per-file LOC breakdown — one entry per file changed in this "Keep All" action. */
    private List<FileChangeDto> fileChanges;
    /** Total number of files updated in this event. */
    private int totalFilesUpdated;
    /** Total number of files added in this event. */
    private int totalFilesAdded;
    /** Total number of files deleted in this event. */
    private int totalFilesDeleted;
    /** Number of input (prompt) tokens consumed by the LLM for this event. */
    private Integer inputTokens;
    /** Number of output (completion) tokens produced by the LLM for this event. */
    private Integer outputTokens;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Percentage of lines in this event that were written by a human (not GenAI).
     * Optional, may be null if not set by the user/plugin.
     */
    private Double humanLocPercent;
}
