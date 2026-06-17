package com.cts.plugin.loc.service;

import com.cts.plugin.loc.service.dto.BatchLocEventRequest;
import com.cts.plugin.loc.service.dto.LocEventRequest;
import com.cts.plugin.loc.service.dto.LocEventResponse;
import com.cts.plugin.loc.service.dto.LocSummary;
import com.cts.plugin.loc.service.repository.LocEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * LocEventControllerIT — Integration tests using embedded MongoDB (Flapdoodle).
 * Exercises all CRUD endpoints and the summary endpoint.
 */
@SpringBootTest
@AutoConfigureMockMvc
class LocEventControllerIT {

    private static final String BASE = "/genai-loc";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired LocEventRepository repository;

    @BeforeEach
    void cleanUp() {
        repository.deleteAll();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private LocEventRequest sampleRequest(String dev, String project, String tool,
                                          boolean genAi, int linesAdded) {
        return LocEventRequest.builder()
                .developerId(dev)
                .developerName("Dev " + dev)
                .projectId(project)
                .filePath("/src/main/java/Foo.java")
                .fileName("Foo.java")
                .ideType("INTELLIJ")
                .genAiTool(tool)
                .developmentMode("BROWNFIELD")
                .linesAdded(linesAdded)
                .linesModified(0)
                .linesDeleted(0)
                .genAiGenerated(genAi)
                .genAiConfidenceScore(genAi ? 0.85 : null)
                .eventTimestamp("2026-04-09T10:00:00")
                .sessionId("session-abc-123")
                .build();
    }

    private LocEventResponse postEvent(LocEventRequest req) throws Exception {
        MvcResult r = mvc.perform(post(BASE + "/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readValue(r.getResponse().getContentAsString(), LocEventResponse.class);
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /events — creates event, returns 201 with ID")
    void createEvent_returnsCreated() throws Exception {
        LocEventRequest req = sampleRequest("dev1", "proj1", "COPILOT", true, 10);
        LocEventResponse resp = postEvent(req);

        assertThat(resp.getId()).isNotBlank();
        assertThat(resp.getDeveloperId()).isEqualTo("dev1");
        assertThat(resp.getGenAiTool()).isEqualTo("COPILOT");
        assertThat(resp.getLinesAdded()).isEqualTo(10);
        assertThat(resp.isGenAiGenerated()).isTrue();
    }

    @Test
    @DisplayName("POST /events — duplicate event returns existing record (idempotent)")
    void createEvent_duplicateIsIdempotent() throws Exception {
        LocEventRequest req = sampleRequest("dev1", "proj1", "COPILOT", true, 10);
        LocEventResponse first  = postEvent(req);
        LocEventResponse second = postEvent(req);  // same dev + file + ts + session
        assertThat(first.getId()).isEqualTo(second.getId());
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("POST /events/batch — saves all events")
    void createBatch() throws Exception {
        BatchLocEventRequest batch = new BatchLocEventRequest(List.of(
                sampleRequest("dev1", "proj1", "COPILOT", true,  5),
                sampleRequest("dev2", "proj1", "CLAUDE",  true, 20),
                sampleRequest("dev3", "proj1", "NONE",   false,  3)
        ));
        mvc.perform(post(BASE + "/events/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(batch)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(3));
        assertThat(repository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("GET /events/{id} — returns correct event")
    void getById() throws Exception {
        LocEventResponse created = postEvent(
                sampleRequest("dev1", "proj1", "GEMINI", true, 7));
        mvc.perform(get(BASE + "/events/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.genAiTool").value("GEMINI"));
    }

    @Test
    @DisplayName("GET /events/{id} — 404 for unknown ID")
    void getById_notFound() throws Exception {
        mvc.perform(get(BASE + "/events/unknown-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /events/{id} — updates event fields")
    void updateEvent() throws Exception {
        LocEventResponse created = postEvent(
                sampleRequest("dev1", "proj1", "COPILOT", true, 5));

        LocEventRequest update = sampleRequest("dev1", "proj1", "CLAUDE", true, 99);
        mvc.perform(put(BASE + "/events/" + created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.genAiTool").value("CLAUDE"))
                .andExpect(jsonPath("$.linesAdded").value(99));
    }

    @Test
    @DisplayName("DELETE /events/{id} — removes event, returns 204")
    void deleteEvent() throws Exception {
        LocEventResponse created = postEvent(
                sampleRequest("dev1", "proj1", "COPILOT", true, 5));
        mvc.perform(delete(BASE + "/events/" + created.getId()))
                .andExpect(status().isNoContent());
        assertThat(repository.existsById(created.getId())).isFalse();
    }

    @Test
    @DisplayName("GET /events/developer/{id} — filters by developer")
    void getByDeveloper() throws Exception {
        postEvent(sampleRequest("devA", "proj1", "COPILOT", true,  5));
        postEvent(sampleRequest("devB", "proj1", "CLAUDE",  true, 10));
        mvc.perform(get(BASE + "/events/developer/devA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].developerId").value("devA"));
    }

    @Test
    @DisplayName("GET /events/tool/{tool} — filters by genAiTool")
    void getByTool() throws Exception {
        postEvent(sampleRequest("dev1", "proj1", "COPILOT", true,  5));
        postEvent(sampleRequest("dev2", "proj1", "CLAUDE",  true, 10));
        postEvent(sampleRequest("dev3", "proj1", "COPILOT", true,  3));
        mvc.perform(get(BASE + "/events/tool/COPILOT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("GET /summary/developer/{id} — returns correct LOC aggregation")
    void summaryByDeveloper() throws Exception {
        postEvent(sampleRequest("dev1", "proj1", "COPILOT", true, 10));
        postEvent(sampleRequest("dev1", "proj1", "COPILOT", true, 20));
        postEvent(sampleRequest("dev1", "proj1", "NONE",   false,  5));

        MvcResult r = mvc.perform(get(BASE + "/summary/developer/dev1")
                        .param("from", "2026-01-01T00:00:00")
                        .param("to",   "2026-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andReturn();

        LocSummary summary = mapper.readValue(
                r.getResponse().getContentAsString(), LocSummary.class);

        assertThat(summary.getTotalEvents()).isEqualTo(3);
        assertThat(summary.getGenAiEvents()).isEqualTo(2);
        assertThat(summary.getManualEvents()).isEqualTo(1);
        assertThat(summary.getTotalLinesAdded()).isEqualTo(35);
        assertThat(summary.getGenAiLinesAdded()).isEqualTo(30);
        assertThat(summary.getGenAiAdoptionPct()).isEqualTo(85.71);
    }

    @Test
    @DisplayName("POST /events — 400 when required fields missing")
    void createEvent_validationError() throws Exception {
        // missing developerId, projectId, etc.
        mvc.perform(post(BASE + "/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid Request"));
    }
}

