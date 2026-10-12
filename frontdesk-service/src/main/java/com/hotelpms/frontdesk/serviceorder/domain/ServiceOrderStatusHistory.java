package com.hotelpms.frontdesk.serviceorder.domain;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
@Entity @Table(name="service_order_status_history") @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ServiceOrderStatusHistory {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @Column(nullable=false) private UUID hotelId;
 @Column(nullable=false) private UUID serviceOrderId;
 @Enumerated(EnumType.STRING) private ServiceOrderStatus fromStatus;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private ServiceOrderStatus toStatus;
 @Column(nullable=false,length=100) private String actor;
 @Column(length=1000) private String notes;
 @Column(nullable=false) private LocalDateTime createdAt;
}
