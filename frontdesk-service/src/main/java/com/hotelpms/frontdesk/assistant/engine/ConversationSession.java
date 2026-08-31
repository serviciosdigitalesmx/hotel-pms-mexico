package com.hotelpms.frontdesk.assistant.engine;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/** Redis-serializable state for one tenant/operator conversation. */
@Getter
@Setter
public final class ConversationSession {

    private LocalIntent intent = LocalIntent.IDLE;
    private ConversationStep step = ConversationStep.NONE;
    private Map<String, String> slots = new HashMap<>();
    private Map<String, String> optionIds = new HashMap<>();
    private Map<String, String> optionLabels = new HashMap<>();

    /**
     * Checks whether a non-blank slot exists.
     *
     * @param key slot name
     * @return whether the slot has a value
     */
    public boolean has(final String key) {
        return slots.containsKey(key) && !slots.get(key).isBlank();
    }

    /**
     * Reads one slot.
     *
     * @param key slot name
     * @return slot value, or {@code null}
     */
    public String get(final String key) {
        return slots.get(key);
    }

    /**
     * Stores a string representation of a conversational value.
     *
     * @param key slot name
     * @param value value to store
     */
    public void put(final String key, final Object value) {
        slots.put(key, String.valueOf(value));
    }

    /**
     * Removes one conversational slot.
     *
     * @param key slot name
     */
    public void remove(final String key) {
        slots.remove(key);
    }

    /** Clears selection maps after the operator chooses an option. */
    public void clearOptions() {
        optionIds.clear();
        optionLabels.clear();
    }
}
