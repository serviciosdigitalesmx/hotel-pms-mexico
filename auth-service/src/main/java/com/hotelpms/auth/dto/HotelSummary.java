package com.hotelpms.auth.dto;

import java.util.UUID;

/**
 * Safe response for platform hotel listings.
 *
 * @param id tenant identifier
 * @param name hotel display name
 * @param slug public hotel slug
 */
public record HotelSummary(UUID id, String name, String slug) { }
