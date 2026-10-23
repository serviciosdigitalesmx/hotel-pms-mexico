package com.hotelpms.auth.dto;

import java.util.List;

/** Stable page envelope for platform console tables. */
public record PlatformPage<T>(List<T> content, int page, int size, long totalElements, int totalPages) { }
