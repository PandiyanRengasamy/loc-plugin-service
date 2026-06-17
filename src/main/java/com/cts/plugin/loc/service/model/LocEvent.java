package com.cts.plugin.loc.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;

/**
 * LocEvent — MongoDB document that stores a single LOC tracking event
 * sent by the IntelliJ plugin.
 *
 * Collection : genai_loc_events
 * Indexes    :
 *   - developerId + eventTimestamp  (queries by developer over time)
 *   - projectId  + eventTimestamp   (queries by project over time)
 *   - genAiTool                     (filter by tool)
 *   - sessionId                     (replay / dedup by session)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "genai_loc_events")
@CompoundIndexes({
    @CompoundIndex(name = "idx_dev_ts",  def = "{'developerId': 1, 'eventTimestamp': -1}"),
    @CompoundIndex(name = "idx_proj_ts", def = "{'projectId': 1,  'eventTimestamp': -1}"),
    @CompoundIndex(name = "idx_tool_ts", def = "{'genAiTool': 1,  'eventTimestamp': -1}")
})
public class LocEvent {

    @Id
    private String id;

    /** Developer identifier — email or OS login. */
    @Indexed
    @Field("developerId")
    private String developerId;

    @Field("developerName")
    private String developerName;

    /** IntelliJ project name or configured override. */
    @Indexed
    @Field("projectId")
    private String projectId;

    /** Optional sprint / iteration identifier. */
    @Field("sprintId")
    private String sprintId;

    /** Full file path inside the project. */
    @Field("filePath")
    private String filePath;

    /** Just the file name (for display). */
    @Field("fileName")
    private String fileName;

    /** Java class name (without extension) that was added/modified/deleted. */
    @Field("className")
    private String className;

    /** Always "INTELLIJ" for this plugin. */
    @Field("ideType")
    private String ideType;

    /**
     * GenAI tool that generated the code.
     * Values: COPILOT | CLAUDE | CHATGPT | GEMINI | CODEWHISPERER | TABNINE | CODEIUM | NONE | OTHER
     */
    @Indexed
    @Field("genAiTool")
    private String genAiTool;

    /** GREENFIELD (new file) or BROWNFIELD (existing file). */
    @Field("developmentMode")
    private String developmentMode;

    @Field("linesAdded")
    private int linesAdded;

    @Field("linesModified")
    private int linesModified;

    @Field("linesDeleted")
    private int linesDeleted;

    /** True if the change was classified as AI-generated. */
    @Field("genAiGenerated")
    private boolean genAiGenerated;

    /** Detection confidence score 0.0–1.0 (null when genAiGenerated=false). */
    @Field("genAiConfidenceScore")
    private Double genAiConfidenceScore;

    /** ISO-8601 timestamp from the plugin (e.g. "2026-04-09T10:30:00"). */
    @Indexed
    @Field("eventTimestamp")
    private String eventTimestamp;

    /** Plugin session UUID — used for deduplication and replay. */
    @Indexed
    @Field("sessionId")
    private String sessionId;

    /** LLM model name used for code generation (e.g. "gpt-4o", "claude-3.5-sonnet"). */
    @Field("llmModel")
    private String llmModel;

    /** Agent name that generated the code (e.g. "Copilot", "Claude Code"). */
    @Field("agentName")
    private String agentName;

    /**
     * Location where the AI-generated changes were accepted.
     * Values: CODE_EDITOR | COPILOT_CHAT | CLAUDE_CHAT | INLINE_SUGGESTION | UNKNOWN
     */
    @Indexed
    @Field("acceptedLocation")
    private String acceptedLocation;

    /**
     * Per-file LOC change details. When Copilot's "Keep All" modifies multiple files,
     * each file's breakdown is stored here. The top-level linesAdded/Modified/Deleted
     * hold the aggregated totals across all files in this array.
     */
    @Field("fileChanges")
    private List<FileChange> fileChanges;

    /** Total number of files updated in this event. */
    @Field("totalFilesUpdated")
    private int totalFilesUpdated;

    /** Total number of files added in this event. */
    @Field("totalFilesAdded")
    private int totalFilesAdded;

    /** Total number of files deleted in this event. */
    @Field("totalFilesDeleted")
    private int totalFilesDeleted;

    /** Server-side record creation time (auto-set by MongoDB auditing). */
    @CreatedDate
    @Field("createdAt")
    private Instant createdAt;

    /** Last update time (auto-set by MongoDB auditing). */
    @LastModifiedDate
    @Field("updatedAt")
    private Instant updatedAt;

    /**
     * Percentage of lines in this event that were written by a human (not GenAI).
     * Optional, may be null if not set by the user/plugin.
     */
    @Field("humanLocPercent")
    //set default to 0.0 to avoid nulls, since this is used in calculations. The plugin can explicitly set to null if they want to indicate "unknown".
    private Double humanLocPercent = 0.0;
}
