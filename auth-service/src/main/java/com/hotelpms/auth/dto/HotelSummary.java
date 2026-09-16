package com.hotelpms.auth.dto;

import java.util.UUID;

/** Safe response for platform hotel listings. */
public record HotelSummary(UUID id, String name, String slug) { }
