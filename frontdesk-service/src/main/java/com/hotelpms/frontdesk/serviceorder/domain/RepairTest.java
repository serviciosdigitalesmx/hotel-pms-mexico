package com.hotelpms.frontdesk.serviceorder.domain;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
@Entity @Table(name="service_order_tests") @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RepairTest {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @Column(nullable=false) private UUID hotelId;
 @Column(nullable=false) private UUID serviceOrderId;
 @Column(nullable=false,length=160) private String testName;
 @Column(nullable=false) private boolean passed;
 @Column(length=2000) private String notes;
 private UUID technicianId;
 @Column(nullable=false) private LocalDateTime createdAt;
}
