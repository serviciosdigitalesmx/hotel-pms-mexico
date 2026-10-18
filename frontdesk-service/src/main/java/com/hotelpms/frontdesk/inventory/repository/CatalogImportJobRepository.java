package com.hotelpms.frontdesk.inventory.repository;

import com.hotelpms.frontdesk.inventory.domain.CatalogImportJob;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Persistence for tenant-scoped catalog ingestion jobs. */
public interface CatalogImportJobRepository extends JpaRepository<CatalogImportJob, UUID> {
    List<CatalogImportJob> findAllByHotelIdOrderByCreatedAtDesc(UUID hotelId);
}
