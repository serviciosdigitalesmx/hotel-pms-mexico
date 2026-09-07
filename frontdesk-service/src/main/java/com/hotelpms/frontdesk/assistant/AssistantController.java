package com.hotelpms.frontdesk.assistant;

import com.hotelpms.frontdesk.assistant.dto.AssistantChatRequest;
import com.hotelpms.frontdesk.assistant.dto.AssistantChatResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** REST boundary for the conversational PMS assistant. */
@RestController
@RequestMapping("/api/v1/stays/assistant")
@RequiredArgsConstructor
@SuppressWarnings("checkstyle:DesignForExtension")
public class AssistantController {

    private final AssistantService assistantService;

    /**
     * Resolves one assistant turn for the authenticated hotel operator.
     *
     * @param request validated chat request
     * @return assistant response with the next answer or proposed operation
     */
    @PostMapping("/chat")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'RECEPTIONIST')")
    public AssistantChatResponse chat(
            @Valid @RequestBody final AssistantChatRequest request) {

        return assistantService.chat(
                resolveHotelId(),
                resolveRoles(),
                request
        );
    }

    private UUID resolveHotelId() {
        final Object details = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getDetails();

        return UUID.fromString(String.valueOf(details));
    }

    private Set<String> resolveRoles() {
        return SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .map(authority ->
                        authority.getAuthority().replaceFirst("^ROLE_", ""))
                .collect(Collectors.toUnmodifiableSet());
    }
}
