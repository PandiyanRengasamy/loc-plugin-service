package com.cts.plugin.loc.service.controller;

import com.cts.plugin.loc.service.dto.BatchLocEventRequest;
import com.cts.plugin.loc.service.dto.LocEventRequest;
import com.cts.plugin.loc.service.dto.LocEventResponse;
import com.cts.plugin.loc.service.dto.LocSummary;
import com.cts.plugin.loc.service.service.LocEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * LocEventController — RESTful API for GenAI LOC Tracker events.
 *
 * Base path : /api/v1/genai-loc
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  Endpoint                                   Method  Description         │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │  /events                                    POST    Create single event │
 * │  /events/batch                              POST    Create batch events │
 * │  /events                                    GET     List all (paged)    │
 * │  /events/{id}                               GET     Get by ID           │
 * │  /events/{id}                               PUT     Full update         │
 * │  /events/{id}                               DELETE  Delete by ID        │
 * │  /events/developer/{developerId}            GET     By developer        │
 * │  /events/project/{projectId}                GET     By project          │
 * │  /events/tool/{genAiTool}                   GET     By AI tool          │
 * │  /events/session/{sessionId}                GET     By session          │
 * │  /events/session/{sessionId}                DELETE  Delete by session   │
 * │  /summary/developer/{developerId}           GET     Developer summary   │
 * │  /summary/project/{projectId}               GET     Project summary     │
 * │  /summary/project/{projectId}/sprint/{id}   GET     Sprint summary      │
 * └─────────────────────────────────────────────────────────────────────────┘
 */
@Slf4j()
@RestController
@Controller
@RequestMapping("/api/v1/genai-loc")
@RequiredArgsConstructor
public class LocEventController {
    private static final Logger LOG = LoggerFactory.getLogger(LocEventController.class);
    private final LocEventService service;

    // ── CREATE ────────────────────────────────────────────────────────────────

    /**
     * POST /genai-loc/events
     * Accepts a single LOC event from the IntelliJ plugin.
     */
    @PostMapping("/events")
    public ResponseEntity<LocEventResponse> createEvent(
            @Valid @RequestBody LocEventRequest req) {
        LOG.debug("POST /events dev={} file={} tool={} \t Request Body: {}", req.getDeveloperId(),
                req.getFileName(), req.getGenAiTool(), req.toString());
        LocEventResponse saved = service.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * POST /genai-loc/events/batch
     * Accepts a batch of LOC events (sent when queue > 1 in the plugin).
     */
    @PostMapping("/events/batch")
    public ResponseEntity<List<LocEventResponse>> createBatch(
            @Valid @RequestBody BatchLocEventRequest req) {
        LOG.debug("POST /events/batch count={}", req.getEvents().size());
        List<LocEventResponse> saved = service.createBatch(req.getEvents());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    /**
     * GET /genai-loc/events
     * Returns all events (not paginated).
     */
    @GetMapping("/events")
    public ResponseEntity<List<LocEventResponse>> getAllEvents() {
        LOG.debug("START: GET /events (no pagination)");
        List<LocEventResponse> allEvents = service.getAll();
        LOG.debug("END: GET /events -> returned {} events", allEvents.size());
        return ResponseEntity.ok(allEvents);
    }

    /**
     * GET /genai-loc/events/{id}
     * Returns a single event by MongoDB document ID.
     */
    @GetMapping("/events/{id}")
    public ResponseEntity<LocEventResponse> getEventById(@PathVariable String id) {
        LOG.debug("START: GET /events/{}", id);
        return ResponseEntity.ok(service.getById(id));
    }

    /**
     * GET /genai-loc/events/developer/{developerId}
     * Optional query params: from, to (ISO-8601 timestamps for range filter).
     */
    @GetMapping("/events/developer/{developerId}")
    public ResponseEntity<Page<LocEventResponse>> getByDeveloper(
            @PathVariable String developerId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @PageableDefault(size = 20, sort = "eventTimestamp",
                             direction = Sort.Direction.DESC) Pageable pageable) {
        LOG.info("START: GET /events/developer/{} from={} to={} page={} size={}", developerId, from, to, pageable.getPageNumber(), pageable.getPageSize());

        Page<LocEventResponse> result = (from != null && to != null)
                ? service.getByDeveloperInRange(developerId, from, to, pageable)
                : service.getByDeveloper(developerId, pageable);

        LOG.info("END: GET /events/developer/{} from={} to={} -> returned Event size: {} events (page {} of {})", developerId, from, to, result.getNumberOfElements(), result.getNumber(), result.getTotalPages());

        return ResponseEntity.ok(result);
    }

    /**
     * GET /genai-loc/events/project/{projectId}
     * Optional query params: from, to for range filter.
     */
    @GetMapping("/events/project/{projectId}")
    public ResponseEntity<Page<LocEventResponse>> getByProject(
            @PathVariable String projectId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @PageableDefault(size = 20, sort = "eventTimestamp",
                             direction = Sort.Direction.DESC) Pageable pageable) {
        LOG.debug("START: GET /events/project/{} from={} to={} page={} size={}", projectId, from, to, pageable.getPageNumber(), pageable.getPageSize());
        Page<LocEventResponse> result = (from != null && to != null)
                ? service.getByProjectInRange(projectId, from, to, pageable)
                : service.getByProject(projectId, pageable);
        LOG.info("END: GET /events/project/{} from={} to={} -> returned Event size: {} events (page {} of {})", projectId, from, to, result.getNumberOfElements(), result.getNumber(), result.getTotalPages());
        return ResponseEntity.ok(result);
    }

    /**
     * GET /genai-loc/events/developer/{developerId}/project/{projectId}
     * Returns all LOC events for a developer and project within a date range (not paginated).
     */
    @GetMapping("/events/developer/{developerId}/project/{projectId}")
    public ResponseEntity<List<LocEventResponse>> getEventsByDeveloperAndProjectInRange(
            @PathVariable String developerId,
            @PathVariable String projectId,
            @RequestParam String from,
            @RequestParam String to) {
        LOG.debug("START: GET /events/developer/{}/project/{} from={} to={}", developerId, projectId, from, to);
        List<LocEventResponse> events = service.getByDeveloperAndProjectInRange(developerId, projectId, from, to);
        LOG.info("END: GET /events/developer/{}/project/{} from={} to={} -> returned {} events", developerId, projectId, from, to, events.size());
        return ResponseEntity.ok(events);
    }

    /**
     * GET /genai-loc/events/tool/{genAiTool}
     * Filter events by GenAI tool (COPILOT, CLAUDE, CHATGPT, GEMINI, NONE, etc.)
     */
    @GetMapping("/events/tool/{genAiTool}")
    public ResponseEntity<Page<LocEventResponse>> getByTool(
            @PathVariable String genAiTool,
            @PageableDefault(size = 20, sort = "eventTimestamp",
                             direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.getByTool(genAiTool, pageable));
    }

    /**
     * GET /genai-loc/events/session/{sessionId}
     * Returns all events from a specific plugin session.
     */
    @GetMapping("/events/session/{sessionId}")
    public ResponseEntity<List<LocEventResponse>> getBySession(@PathVariable String sessionId) {
        return ResponseEntity.ok(service.getBySession(sessionId));
    }

    /**
     * GET /genai-loc/events/project/{projectId}/sprint/{sprintId}
     */
    @GetMapping("/events/project/{projectId}/sprint/{sprintId}")
    public ResponseEntity<List<LocEventResponse>> getBySprint(
            @PathVariable String projectId,
            @PathVariable String sprintId) {
        LOG.debug("GET /events/project/{}/sprint/{}", projectId, sprintId);
        List<LocEventResponse> resLst = service.getBySprint(projectId, sprintId);
        LOG.debug("GET /events/project/{}/sprint/{} -> found {} events", projectId, sprintId, resLst.size());
        return ResponseEntity.ok(resLst);
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    /**
     * PUT /genai-loc/events/{id}
     * Full replacement of an existing event.
     */
    @PutMapping("/events/{id}")
    public ResponseEntity<LocEventResponse> updateEvent(
            @PathVariable String id,
            @Valid @RequestBody LocEventRequest req) {
        LOG.debug("PUT /events/{} dev={} file={}", id, req.getDeveloperId(), req.getFileName());
        return ResponseEntity.ok(service.update(id, req));
    }

    /**
     * PUT /genai-loc/events/{id}/human-loc
     * Updates only the humanLocPercent field for an event.
     * Request body: { "humanLocPercent": 42.0 }
     */
    @PutMapping("/events/{id}/human-loc")
    public ResponseEntity<LocEventResponse> updateHumanLocPercent(
            @PathVariable String id,
            @RequestBody Map<String, Double> payload) {
        if (!payload.containsKey("humanLocPercent")) {
            return ResponseEntity.badRequest().build();
        }
        Double humanLocPercent = payload.get("humanLocPercent");
        LocEventResponse updated = service.updateHumanLocPercent(id, humanLocPercent);
        return ResponseEntity.ok(updated);
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    /**
     * DELETE /genai-loc/events/{id}
     * Removes a single event by ID.
     */
    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable String id) {
        LOG.debug("DELETE /events/{}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /genai-loc/events/session/{sessionId}
     * Removes all events from a session (used for replay cleanup).
     */
    @DeleteMapping("/events/session/{sessionId}")
    public ResponseEntity<Map<String, Long>> deleteBySession(@PathVariable String sessionId) {
        long removed = service.deleteBySession(sessionId);
        return ResponseEntity.ok(Map.of("removed", removed));
    }

    // ── SUMMARY ───────────────────────────────────────────────────────────────

    /**
     * GET /genai-loc/summary/developer/{developerId}?from=...&to=...
     * Aggregated LOC stats for a developer within a time range.
     */
    @GetMapping("/summary/developer/{developerId}")
    public ResponseEntity<LocSummary> getSummaryByDeveloper(
            @PathVariable String developerId,
            @RequestParam String from,
            @RequestParam String to) {
        LOG.debug("Start: getSummaryByDeveloper () method called -> GET /summary/developer/{} from={} to={}", developerId, from, to);
        LocSummary res = service.getSummaryByDeveloper(developerId, from, to);
        LOG.info("END: getSummaryByDeveloper () method called -> Summary for developer {} from {} to {}: totalEvents={}, genAiEvents={}, manualEvents={}, genAiAdoptionPct={:.2f}%",
                developerId, from, to, res.getTotalEvents(), res.getGenAiEvents(),
                res.getManualEvents(), res.getGenAiAdoptionPct());
        return ResponseEntity.ok(res);
    }

    /**
     * GET /genai-loc/summary/project/{projectId}?from=...&to=...
     * Aggregated LOC stats for a project within a time range.
     */
    @GetMapping("/summary/project/{projectId}")
    public ResponseEntity<LocSummary> getSummaryByProject(
            @PathVariable String projectId,
            @RequestParam String from,
            @RequestParam String to) {
        LOG.debug("Start: getSummaryByProject () method called -> GET /summary/project/{} from={} to={}", projectId, from, to);
        LocSummary res = service.getSummaryByProject(projectId, from, to);
        LOG.info("END: getSummaryByProject () method called -> Summary for project {} from {} to {}: " +
                        "totalEvents={}, genAiEvents={}, manualEvents={}, genAiAdoptionPct={:.2f}%",
                projectId, from, to, res.getTotalEvents(), res.getGenAiEvents(), res.getManualEvents(), res.getGenAiAdoptionPct());
        return ResponseEntity.ok(res);
    }

    /**
     * GET /genai-loc/summary/project/{projectId}/sprint/{sprintId}
     * Aggregated LOC stats for a project sprint.
     */
    @GetMapping("/summary/project/{projectId}/sprint/{sprintId}")
    public ResponseEntity<LocSummary> getSummaryBySprint(
            @PathVariable String projectId,
            @PathVariable String sprintId) {
        LOG.debug("Start: getSummaryBySprint () method called -> GET /summary/project/{}/sprint/{}", projectId, sprintId);
        LocSummary res = service.getSummaryBySprint(projectId, sprintId);
        LOG.info("END: getSummaryBySprint () method called -> Summary for project {} sprint {}: totalEvents={}, genAiEvents={}, manualEvents={}, genAiAdoptionPct={:.2f}%",
                projectId, sprintId, res.getTotalEvents(), res.getGenAiEvents(), res.getManualEvents(), res.getGenAiAdoptionPct());
        return ResponseEntity.ok(res);
    }

    /**
     * GET /genai-loc/events/developer/{developerId}/all
     * Returns all events for a developer in a date range (not paginated).
     */
    @GetMapping("/events/developer/{developerId}/all")
    public ResponseEntity<List<LocEventResponse>> getAllByDeveloperInRange(
            @PathVariable String developerId,
            @RequestParam String from,
            @RequestParam String to) {
        LOG.debug("START: GET /events/developer/{}/all", developerId);
        List<LocEventResponse> events = service.getAllByDeveloperInRange(developerId, from, to);
        List<LocEventResponse> events1 = service.getAllByDeveloper(developerId);
        LOG.debug("END: GET /events/developer/{}/all -> returned Data Size {} events", developerId, events.size());
        return ResponseEntity.ok(events);
    }
}
