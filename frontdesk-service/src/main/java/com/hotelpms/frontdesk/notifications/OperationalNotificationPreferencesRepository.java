package com.hotelpms.frontdesk.notifications;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/** Tenant-keyed notification preferences. */
public interface OperationalNotificationPreferencesRepository
        extends JpaRepository<OperationalNotificationPreferences, UUID> {}
