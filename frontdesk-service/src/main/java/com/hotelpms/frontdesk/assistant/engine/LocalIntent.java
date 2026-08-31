package com.hotelpms.frontdesk.assistant.engine;

/** Operations handled locally without an AI provider. */
public enum LocalIntent {
    IDLE,
    FIND_GUEST,
    CREATE_GUEST,
    ROOM_AVAILABILITY,
    PREPARE_CHECK_IN,
    CHECK_IN,
    BATCH_CHECK_IN,
    CONFIRM,
    DECLINE,
    CANCEL,
    UNKNOWN
}
