package com.cts.plugin.loc.service.repository;

import com.cts.plugin.loc.service.model.LocEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * LocEventRepository — Spring Data MongoDB DAO for LocEvent documents.
 *
 * Provides:
 *   - Standard CRUD (inherited from MongoRepository)
 *   - Finder methods by developer, project, tool, session
 *   - Paginated queries for large result sets
 */
@Repository
public interface LocEventRepository extends MongoRepository<LocEvent, String> {

    // ── By Developer ─────────────────────────────────────────────────────────

    Page<LocEvent> findByDeveloperId(String developerId, Pageable pageable);

    Page<LocEvent> findByDeveloperIdAndEventTimestampBetween(
            String developerId, String from, String to, Pageable pageable);

    // ── By Project ────────────────────────────────────────────────────────────

    Page<LocEvent> findByProjectId(String projectId, Pageable pageable);

    Page<LocEvent> findByProjectIdAndEventTimestampBetween(
            String projectId, String from, String to, Pageable pageable);

    // ── By Developer + Project ────────────────────────────────────────────────

    Page<LocEvent> findByDeveloperIdAndProjectId(
            String developerId, String projectId, Pageable pageable);

    Page<LocEvent> findByDeveloperIdAndProjectIdAndEventTimestampBetween(
            String developerId, String projectId, String from, String to, Pageable pageable);

    // ── By GenAI Tool ─────────────────────────────────────────────────────────

    Page<LocEvent> findByGenAiTool(String genAiTool, Pageable pageable);

    Page<LocEvent> findByGenAiGenerated(boolean genAiGenerated, Pageable pageable);

    // ── By Session ────────────────────────────────────────────────────────────

    List<LocEvent> findBySessionId(String sessionId);

    Optional<LocEvent> findBySessionIdAndFileNameAndEventTimestamp(
            String sessionId, String fileName, String eventTimestamp);

    // ── By Sprint ─────────────────────────────────────────────────────────────

    Page<LocEvent> findByProjectIdAndSprintId(
            String projectId, String sprintId, Pageable pageable);

    // ── Aggregation helpers (used by LocSummaryService) ───────────────────────

    @Query("{ 'developerId': ?0, 'eventTimestamp': { $gte: ?1, $lte: ?2 } }")
    List<LocEvent> findByDeveloperIdInRange(String developerId, String from, String to);

    List<LocEvent> findByDeveloperId(String developerId);

    @Query("{ 'projectId': ?0, 'eventTimestamp': { $gte: ?1, $lte: ?2 } }")
    List<LocEvent> findByProjectIdInRange(String projectId, String from, String to);

    @Query("{ 'projectId': ?0, 'sprintId': ?1 }")
    List<LocEvent> findByProjectIdAndSprintIdAll(String projectId, String sprintId);

    // ── Deduplication check ───────────────────────────────────────────────────

    boolean existsByDeveloperIdAndFileNameAndEventTimestampAndSessionId(
            String developerId, String fileName, String eventTimestamp, String sessionId);

    // ── Counts ────────────────────────────────────────────────────────────────

    long countByProjectId(String projectId);

    long countByDeveloperId(String developerId);

    long countByGenAiGenerated(boolean genAiGenerated);
}

