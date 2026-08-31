package com.hotelpms.frontdesk.assistant.dto;

import java.util.List;

/**
 * Assistant answer and any exact PMS operations requested by the model.
 *
 * @param answer natural-language answer
 * @param toolCalls exact PMS operations requested by the model
 */
public record AssistantChatResponse(String answer, List<AssistantToolCall> toolCalls) {

    /** Defensive copy. */
    public AssistantChatResponse {
        answer = answer == null ? "" : answer;
        toolCalls = toolCalls == null ? List.of() : List.copyOf(toolCalls);
    }
}
