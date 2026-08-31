package com.hotelpms.frontdesk.assistant.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Conversation sent by an authenticated hotel operator to the AI assistant.
 *
 * @param messages provider-neutral conversation history
 */
public record AssistantChatRequest(
        @NotEmpty @Size(max = MAX_MESSAGES) List<@Valid AssistantMessage> messages) {

    /** Maximum messages accepted in one tool conversation. */
    public static final int MAX_MESSAGES = 40;

    /** Defensive copy. */
    public AssistantChatRequest {
        messages = messages == null ? List.of() : List.copyOf(messages);
    }
}
