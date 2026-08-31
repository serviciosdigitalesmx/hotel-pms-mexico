package com.hotelpms.frontdesk.assistant.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Exact operation requested by the model.
 *
 * @param id provider tool call identifier
 * @param name allow-listed tool name
 * @param arguments structured operation arguments
 * @param requiresConfirmation whether a human click is mandatory
 */
public record AssistantToolCall(
        @NotBlank @Size(max = MAX_ID_LENGTH) String id,
        @NotBlank @Size(max = MAX_NAME_LENGTH) String name,
        @NotNull JsonNode arguments,
        boolean requiresConfirmation) {

    /** Maximum provider identifier length. */
    public static final int MAX_ID_LENGTH = 200;
    /** Maximum tool name length. */
    public static final int MAX_NAME_LENGTH = 100;
}
