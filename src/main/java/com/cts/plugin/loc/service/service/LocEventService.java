package com.cts.plugin.loc.service.service;

import com.cts.plugin.loc.service.dto.*;
import com.cts.plugin.loc.service.exception.LocEventDataNotFoundException;
import com.cts.plugin.loc.service.model.FileChange;
import com.cts.plugin.loc.service.model.LocEvent;
import com.cts.plugin.loc.service.repository.LocEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LocEventService — core business logic for CRUD operations on LOC events.
 *
 * Responsibilities:
 *   - Map DTO ↔ Document
 *   - Deduplication check before insert (same dev + file + timestamp + session)
 *   - Delegate persistence to LocEventRepository (MongoDB DAO)
 *   - Build LocSummary aggregations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocEventService {

    private final LocEventRepository repository;

    // ── CREATE ────────────────────────────────────────────────────────────────

    /**
     * Save a single LOC event. Skips duplicate events (idempotent).
     */
    public LocEventResponse create(LocEventRequest req) {
        if (isDuplicate(req)) {
            log.debug("create: duplicate skipped dev={} file={} ts={}",
                    req.getDeveloperId(), req.getFileName(), req.getEventTimestamp());
            // Return existing record
            return repository
                    .findBySessionIdAndFileNameAndEventTimestamp(
                            req.getSessionId(), req.getFileName(), req.getEventTimestamp())
                    .map(this::toResponse)
                    .orElse(saveAndReturn(req));
        }
        return saveAndReturn(req);
    }

    /**
     * Save a batch of LOC events. Each event is deduplicated individually.
     */
    public List<LocEventResponse> createBatch(List<LocEventRequest> requests) {
        log.info("createBatch: processing {} events", requests.size());
        return requests.stream()
                .map(this::create)
                .collect(Collectors.toList());
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    /** Get a single event by MongoDB document ID. */
    public LocEventResponse getById(String id) {
        LocEventResponse cached = repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new LocEventDataNotFoundException("LocEvent not found: " + id));
        if (cached != null) {
            log.debug("getById: cache hit for id={}", id);
            log.debug("getById: event details: dev={} file={} ts={}",
                    cached.getDeveloperId(), cached.getFileName(), cached.getEventTimestamp());
        } else {
            log.debug("getById: cache miss for id={}", id);
        }
        return cached;

    }

    /** List all events (not paginated). */
    public List<LocEventResponse> getAll() {
        List<LocEventResponse> locList = repository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
        log.info("LOC Event Service: getAll() : totalElements={}", locList.size());
        return locList;
    }

    /** List events by developer (paginated). */
    public Page<LocEventResponse> getByDeveloper(String developerId, Pageable pageable) {
        return repository.findByDeveloperId(developerId, pageable).map(this::toResponse);
    }

    /** List events by developer within a timestamp range (paginated). */
    public Page<LocEventResponse> getByDeveloperInRange(
            String developerId, String from, String to, Pageable pageable) {
        log.info("getByDeveloperInRange: dev={} from={} to={}", developerId, from, to);
        Page<LocEventResponse> locRes = repository.findByDeveloperIdAndEventTimestampBetween(developerId, from, to, pageable)
                .map(this::toResponse);
        log.debug("getByDeveloperInRange: found {} events for dev={} in range", locRes.getTotalElements(), developerId);
        return locRes;
    }

    /** List events by project (paginated). */
    public Page<LocEventResponse> getByProject(String projectId, Pageable pageable) {
        return repository.findByProjectId(projectId, pageable).map(this::toResponse);
    }

    /** List events by project within a timestamp range (paginated). */
    public Page<LocEventResponse> getByProjectInRange(
            String projectId, String from, String to, Pageable pageable) {
        return repository
                .findByProjectIdAndEventTimestampBetween(projectId, from, to, pageable)
                .map(this::toResponse);
    }

    /** List events by developer AND project (paginated). */
    public Page<LocEventResponse> getByDeveloperAndProject(
            String developerId, String projectId, Pageable pageable) {
        return repository
                .findByDeveloperIdAndProjectId(developerId, projectId, pageable)
                .map(this::toResponse);
    }

    /** List events by developer + project within a timestamp range (paginated). */
    public Page<LocEventResponse> getByDeveloperAndProjectInRange(
            String developerId, String projectId, String from, String to, Pageable pageable) {
        return repository.findByDeveloperIdAndProjectIdAndEventTimestampBetween(
                        developerId, projectId, from, to, pageable)
                .map(this::toResponse);
    }

    /** List events by GenAI tool (paginated). */
    public Page<LocEventResponse> getByTool(String genAiTool, Pageable pageable) {
        return repository.findByGenAiTool(genAiTool, pageable).map(this::toResponse);
    }

    /** List events by sprint. */
    public List<LocEventResponse> getBySprint(String projectId, String sprintId) {
        return repository.findByProjectIdAndSprintIdAll(projectId, sprintId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /** List events belonging to a session (for replay / dedup). */
    public List<LocEventResponse> getBySession(String sessionId) {
        return repository.findBySessionId(sessionId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── UPDATE ──────���─────────────────────────────────────────────────────────

    /**
     * Full update — replace all fields of an existing event.
     */
    public LocEventResponse update(String id, LocEventRequest req) {
        LocEvent existing = repository.findById(id)
                .orElseThrow(() -> new LocEventDataNotFoundException("LocEvent not found: " + id));

        LocEvent updated = toDocument(req);
        updated.setId(existing.getId());
        updated.setCreatedAt(existing.getCreatedAt());   // preserve original creation time

        LocEvent saved = repository.save(updated);
        log.info("update: id={} dev={} file={}", id, req.getDeveloperId(), req.getFileName());
        return toResponse(saved);
    }

    /**
     * Update only the humanLocPercent field for an event.
     */
    public LocEventResponse updateHumanLocPercent(String id, Double humanLocPercent) {
        LocEvent event = repository.findById(id)
                .orElseThrow(() -> new LocEventDataNotFoundException("LocEvent not found: " + id));
        event.setHumanLocPercent(humanLocPercent);
        LocEvent saved = repository.save(event);
        log.info("updateHumanLocPercent: id={} humanLocPercent={}", id, humanLocPercent);
        return toResponse(saved);
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    /** Delete a single event by ID. */
    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new LocEventDataNotFoundException("LocEvent not found: " + id);
        }
        repository.deleteById(id);
        log.info("delete: id={}", id);
    }

    /** Delete all events belonging to a session (e.g. replay cleanup). */
    public long deleteBySession(String sessionId) {
        List<LocEvent> events = repository.findBySessionId(sessionId);
        repository.deleteAll(events);
        log.info("deleteBySession: sessionId={} removed={}", sessionId, events.size());
        return events.size();
    }

    // ── SUMMARY / AGGREGATION ─────────────────────────────────────────────────

    /**
     * Compute LOC summary for a developer within a date range.
     */
    public LocSummary getSummaryByDeveloper(String developerId, String from, String to) {
        log.info("START: getSummaryByDeveloper: dev={} from={} to={}", developerId, from, to);
        List<LocEvent> events = repository.findByDeveloperIdInRange(developerId, from, to);
        LocSummary summary = buildSummary(events, developerId, null, from, to);
        log.info("END: getSummaryByDeveloper: dev={} totalEvents={} genAiEvents={} manualEvents={}",
                developerId, summary.getTotalEvents(), summary.getGenAiEvents(), summary.getManualEvents());
        return summary;
    }

    /**
     * Compute LOC summary for a project within a date range.
     */
    public LocSummary getSummaryByProject(String projectId, String from, String to) {
        List<LocEvent> events = repository.findByProjectIdInRange(projectId, from, to);
        return buildSummary(events, null, projectId, from, to);
    }

    /**
     * Compute LOC summary for a project sprint.
     */
    public LocSummary getSummaryBySprint(String projectId, String sprintId) {
        List<LocEvent> events = repository.findByProjectIdAndSprintIdAll(projectId, sprintId);
        return buildSummary(events, null, projectId, "sprint:" + sprintId, sprintId);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private boolean isDuplicate(LocEventRequest req) {
        return repository.existsByDeveloperIdAndFileNameAndEventTimestampAndSessionId(
                req.getDeveloperId(), req.getFileName(),
                req.getEventTimestamp(), req.getSessionId());
    }

    private LocEventResponse saveAndReturn(LocEventRequest req) {
        LocEvent saved = repository.save(toDocument(req));
        log.debug("saveAndReturn: id={} dev={} file={} tool={} +lines={}",
                saved.getId(), saved.getDeveloperId(), saved.getFileName(),
                saved.getGenAiTool(), saved.getLinesAdded());
        return toResponse(saved);
    }

    /** DTO → Document */
    private LocEvent toDocument(LocEventRequest req) {
        // Map fileChanges DTO list → model list
        List<FileChange> fileChanges = new ArrayList<>();
        if (req.getFileChanges() != null) {
            for (FileChangeDto fc : req.getFileChanges()) {
                fileChanges.add(FileChange.builder()
                        .filePath(fc.getFilePath())
                        .fileName(fc.getFileName())
                        .className(fc.getClassName())
                        .linesAdded(fc.getLinesAdded())
                        .linesModified(fc.getLinesModified())
                        .linesDeleted(fc.getLinesDeleted())
                        .build());
            }
        }
        return LocEvent.builder()
                .developerId(req.getDeveloperId())
                .developerName(req.getDeveloperName())
                .projectId(req.getProjectId())
                .sprintId(req.getSprintId())
                .filePath(req.getFilePath())
                .fileName(req.getFileName())
                .className(req.getClassName())
                .ideType(req.getIdeType() != null ? req.getIdeType() : "INTELLIJ")
                .genAiTool(req.getGenAiTool())
                .developmentMode(req.getDevelopmentMode())
                .linesAdded(req.getLinesAdded())
                .linesModified(req.getLinesModified())
                .linesDeleted(req.getLinesDeleted())
                .genAiGenerated(Boolean.TRUE.equals(req.getGenAiGenerated()))
                .genAiConfidenceScore(req.getGenAiConfidenceScore())
                .eventTimestamp(req.getEventTimestamp())
                .sessionId(req.getSessionId())
                .llmModel(req.getLlmModel())
                .agentName(req.getAgentName())
                .acceptedLocation(req.getAcceptedLocation())
                .fileChanges(fileChanges)
                .totalFilesUpdated(req.getTotalFilesUpdated())
                .totalFilesAdded(req.getTotalFilesAdded())
                .totalFilesDeleted(req.getTotalFilesDeleted())
                .build();
    }

    /** Document → Response DTO */
    private LocEventResponse toResponse(LocEvent event) {
        // Map fileChanges model list → DTO list
        List<FileChangeDto> fileChangeDtos = new ArrayList<>();
        if (event.getFileChanges() != null) {
            for (FileChange fc : event.getFileChanges()) {
                fileChangeDtos.add(FileChangeDto.builder()
                        .filePath(fc.getFilePath())
                        .fileName(fc.getFileName())
                        .className(fc.getClassName())
                        .linesAdded(fc.getLinesAdded())
                        .linesModified(fc.getLinesModified())
                        .linesDeleted(fc.getLinesDeleted())
                        .build());
            }
        }
        return LocEventResponse.builder()
                .id(event.getId())
                .developerId(event.getDeveloperId())
                .developerName(event.getDeveloperName())
                .projectId(event.getProjectId())
                .sprintId(event.getSprintId())
                .filePath(event.getFilePath())
                .fileName(event.getFileName())
                .className(event.getClassName())
                .ideType(event.getIdeType())
                .genAiTool(event.getGenAiTool())
                .developmentMode(event.getDevelopmentMode())
                .linesAdded(event.getLinesAdded())
                .linesModified(event.getLinesModified())
                .linesDeleted(event.getLinesDeleted())
                .genAiGenerated(event.isGenAiGenerated())
                .genAiConfidenceScore(event.getGenAiConfidenceScore())
                .eventTimestamp(event.getEventTimestamp())
                .sessionId(event.getSessionId())
                .llmModel(event.getLlmModel())
                .agentName(event.getAgentName())
                .acceptedLocation(event.getAcceptedLocation())
                .fileChanges(fileChangeDtos)
                .totalFilesUpdated(event.getTotalFilesUpdated())
                .totalFilesAdded(event.getTotalFilesAdded())
                .totalFilesDeleted(event.getTotalFilesDeleted())
                .inputTokens(event.getInputTokens())
                .outputTokens(event.getOutputTokens())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .humanLocPercent(event.getHumanLocPercent())
                .build();
    }

    /** Build a LocSummary from a list of events. */
    private LocSummary buildSummary(List<LocEvent> events,
                                    String developerId, String projectId,
                                    String from, String to) {
        long totalEvents    = events.size();
        long genAiEvents    = events.stream().filter(LocEvent::isGenAiGenerated).count();
        long manualEvents   = totalEvents - genAiEvents;

        long totalAdded     = events.stream().mapToLong(LocEvent::getLinesAdded).sum();
        long totalModified  = events.stream().mapToLong(LocEvent::getLinesModified).sum();
        long totalDeleted   = events.stream().mapToLong(LocEvent::getLinesDeleted).sum();

        long genAiAdded     = events.stream()
                .filter(LocEvent::isGenAiGenerated)
                .mapToLong(LocEvent::getLinesAdded).sum();
        long manualAdded    = totalAdded - genAiAdded;

        double adoptionPct  = totalAdded == 0 ? 0.0
                : Math.round((genAiAdded * 100.0 / totalAdded) * 100.0) / 100.0;

        double manualPct = 100.0 - adoptionPct;

        double avgConf      = events.stream()
                .filter(e -> e.getGenAiConfidenceScore() != null)
                .mapToDouble(LocEvent::getGenAiConfidenceScore)
                .average().orElse(0.0);

        // determine most-used tool
        String primaryTool = events.stream()
                .filter(LocEvent::isGenAiGenerated)
                .collect(Collectors.groupingBy(LocEvent::getGenAiTool, Collectors.counting()))
                .entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .orElse("NONE");

        return LocSummary.builder()
                .developerId(developerId)
                .projectId(projectId)
                .genAiTool(primaryTool)
                .fromTimestamp(from)
                .toTimestamp(to)
                .totalEvents(totalEvents)
                .genAiEvents(genAiEvents)
                .manualEvents(manualEvents)
                .totalLinesAdded(totalAdded)
                .totalLinesModified(totalModified)
                .totalLinesDeleted(totalDeleted)
                .genAiLinesAdded(genAiAdded)
                .manualLinesAdded(manualAdded)
                .genAiAdoptionPct(adoptionPct)
                .manualContributionPct(manualPct)
                .avgConfidenceScore(avgConf)
                .build();
    }

    /**
     * List events by developer AND project within a timestamp range (not paginated).
     */
    public List<LocEventResponse> getByDeveloperAndProjectInRange(
            String developerId, String projectId, String from, String to) {
        return repository.findByDeveloperIdAndProjectIdAndEventTimestampBetween(
                developerId, projectId, from, to, Pageable.unpaged())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * List all events by developer within a timestamp range (not paginated).
     */
    public List<LocEventResponse> getAllByDeveloper(String developerId) {
        log.debug("START: getAllByDeveloperInRange: dev={} ", developerId);
        List<LocEvent> venLst = repository.findByDeveloperId(developerId);
        List<LocEventResponse> resLst = venLst.stream().map(this::toResponse).collect(Collectors.toList());
        log.debug("END: getAllByDeveloperInRange: dev={} totalEvents={}", developerId, resLst.size());
        return resLst;
    }

    /**
     * List all events by developer within a timestamp range (not paginated).
     */
    public List<LocEventResponse> getAllByDeveloperInRange(String developerId, String from, String to) {
        log.debug("START: getAllByDeveloperInRange: dev={} from={} to={}", developerId, from, to);
        List<LocEvent> venLst = repository.findByDeveloperIdInRange(developerId, from, to);
        List<LocEventResponse> resLst = venLst.stream().map(this::toResponse).collect(Collectors.toList());
        log.debug("END: getAllByDeveloperInRange: dev={} from={} to={} totalEvents={}", developerId, from, to, resLst.size());
        return resLst;
    }

}
