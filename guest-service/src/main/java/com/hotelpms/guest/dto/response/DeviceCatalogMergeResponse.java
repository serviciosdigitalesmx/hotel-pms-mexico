package com.hotelpms.guest.dto.response;

/**
 * Number of tenant-owned customer devices normalized by a catalog merge.
 *
 * @param updatedDevices number of updated tenant devices
 */
public record DeviceCatalogMergeResponse(int updatedDevices) {
}
