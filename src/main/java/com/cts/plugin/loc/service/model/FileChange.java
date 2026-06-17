package com.cts.plugin.loc.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * FileChange — embedded MongoDB sub-document stored inside {@link LocEvent}.
 *
 * Each entry in the {@code fileChanges} array describes the LOC metrics for
 * one file that was changed during a single "Keep All" / "Keep" Copilot action.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileChange {

    /** Full absolute path of the changed file. */
    @Field("filePath")
    private String filePath;

    /** File name including extension (e.g. {@code UserService.java}). */
    @Field("fileName")
    private String fileName;

    /**
     * Java/Kotlin class name — file name without extension
     * (e.g. {@code UserService}).
     */
    @Field("className")
    private String className;

    /** Number of lines added by the AI in this file. */
    @Field("linesAdded")
    private int linesAdded;

    /** Number of lines modified (replaced) by the AI in this file. */
    @Field("linesModified")
    private int linesModified;

    /** Number of lines deleted by the AI in this file. */
    @Field("linesDeleted")
    private int linesDeleted;
}

