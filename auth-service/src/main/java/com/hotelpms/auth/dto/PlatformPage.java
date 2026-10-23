package com.hotelpms.auth.dto;

import java.util.List;

/** Stable page envelope for platform console tables.
 * @param <T> row value type
 * @param content rows on the requested page
 * @param page zero-based page index
 * @param size maximum rows requested per page
 * @param totalElements total matching rows
 * @param totalPages number of pages for the query
 */
public record PlatformPage<T>(List<T> content, int page, int size, long totalElements, int totalPages) { }
