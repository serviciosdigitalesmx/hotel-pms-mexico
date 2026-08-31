package com.hotelpms.frontdesk.assistant.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Provider-neutral conversation message, including tool calls and tool results.
 *
 * @param role message role
 * @param content message content
 * @param toolCallId provider tool call identifier for a tool result
 * @param toolName tool name for a tool result
 * @param toolCalls operations proposed by an assistant message
 */
public record AssistantMessage(
        @NotBlank @Pattern(regexp = "user|assistant|tool") String role,
        @Size(max = MAX_CONTENT_LENGTH) String content,
        @Size(max = MAX_TOOL_ID_LENGTH) String toolCallId,
        @Size(max = MAX_TOOL_NAME_LENGTH) String toolName,
        @Size(max = MAX_TOOL_CALLS) List<@Valid AssistantToolCall> toolCalls) {

    /** Maximum message payload. */
    public static final int MAX_CONTENT_LENGTH = 12_000;
    /** Maximum provider tool identifier length. */
    public static final int MAX_TOOL_ID_LENGTH = 200;
    /** Maximum tool name length. */
    public static final int MAX_TOOL_NAME_LENGTH = 100;
    /** A model response may request at most four operations. */
    public static final int MAX_TOOL_CALLS = 4;

    /** Defensive copy. */
    public AssistantMessage {
        toolCalls = toolCalls == null ? List.of() : List.copyOf(toolCalls);
    }
}
