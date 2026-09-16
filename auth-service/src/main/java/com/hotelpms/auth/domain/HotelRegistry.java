package com.hotelpms.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/** Platform tenant registry, separate from each hotel's operational settings. */
@Entity
@Table(name = "hotel_registry")
@Getter
@Setter
public class HotelRegistry {
    private static final int NAME_MAX_LENGTH = 160;
    private static final int SLUG_MAX_LENGTH = 80;

    @Id
    private UUID id;

    @Column(nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    @Column(nullable = false, unique = true, length = SLUG_MAX_LENGTH)
    private String slug;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
