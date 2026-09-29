package com.hotelpms.guest.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

/** A persistent customer-owned asset. It is deliberately not embedded in an order. */
@Entity
@Table(name = "devices", indexes = {
        @Index(name = "idx_devices_hotel_customer", columnList = "hotel_id,guest_id")
})
@SQLDelete(sql = "UPDATE devices SET active = false WHERE id = ?")
@SQLRestriction("active = true")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Device {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "hotel_id", nullable = false) private UUID hotelId;
    @Column(name = "guest_id", nullable = false) private UUID guestId;
    @Column(nullable = false, length = 80) private String category;
    @Column(length = 150) private String manufacturer;
    @Column(length = 150) private String model;
    @Column(length = 150) private String serialNumber;
    @Column(length = 150) private String imei;
    @Column(length = 2000) private String customFields;
    @Column(nullable = false) @Builder.Default private boolean active = true;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(nullable = false) private LocalDateTime updatedAt;
}
