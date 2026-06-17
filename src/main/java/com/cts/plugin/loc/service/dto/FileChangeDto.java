package com.cts.plugin.loc.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FileChangeDto — per-file LOC change detail within a single LOC event.
 *
 * When GitHub Copilot's "Keep All" is clicked it can modify multiple files.
 * The plugin bundles all file changes into one HTTP request; this DTO holds
 * the details for each individual file.
 *
 * Example JSON array entry:
 * <pre>
 * {
 *   "filePath"     : "C:/project/src/UserService.java",
 *   "fileName"     : "UserService.java",
 *   "className"    : "UserService",
 *   "linesAdded"   : 25,
 *   "linesModified": 4,
 *   "linesDeleted" : 2
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileChangeDto {

    /** Full absolute path of the changed file. */
    private String filePath;

    /** File name including extension (e.g. {@code UserService.java}). */
    private String fileName;

    /**
     * Java/Kotlin class name — file name without extension
     * (e.g. {@code UserService}).
     */
    private String className;

    /** Number of lines added by the AI in this file. */
    private int linesAdded;

    /** Number of lines modified (replaced) by the AI in this file. */
    private int linesModified;

    /** Number of lines deleted by the AI in this file. */
    private int linesDeleted;
}

