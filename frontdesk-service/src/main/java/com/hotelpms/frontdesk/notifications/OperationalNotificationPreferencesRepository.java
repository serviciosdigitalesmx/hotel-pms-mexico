package com.hotelpms.frontdesk.notifications;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Tenant-keyed notification preferences. */
public interface OperationalNotificationPreferencesRepository
    extends JpaRepository<OperationalNotificationPreferences, UUID> {}
