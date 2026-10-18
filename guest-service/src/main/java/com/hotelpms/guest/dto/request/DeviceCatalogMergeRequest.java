package com.hotelpms.guest.dto.request;

import com.hotelpms.guest.util.ValidationConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Tenant-local request to normalize one or more device catalog identities into a target.
 *
 * @param sources source identities to normalize
 * @param target destination identity
 */
public record DeviceCatalogMergeRequest(
    @NotEmpty List<@Valid Identity> sources,
    @NotNull @Valid Identity target) {

  /** Copies the source collection to prevent callers from mutating a validated merge request. */
  public DeviceCatalogMergeRequest {
    if (sources != null) {
      sources = List.copyOf(sources);
    }
  }

  /**
   * Returns an immutable copy of the selected source identities.
   *
   * @return immutable source identities, or {@code null} when not supplied
   */
  @Override
  public List<Identity> sources() {
    return sources == null ? null : List.copyOf(sources);
  }

  /**
   * A catalog identity; blank manufacturer/model values are valid.
   *
   * @param category device category
   * @param manufacturer device manufacturer, if known
   * @param model device model, if known
   */
  public record Identity(
      @NotBlank @Size(max = ValidationConstants.MAX_DEVICE_CATEGORY_LENGTH) String category,
      @Size(max = ValidationConstants.MAX_DEVICE_MANUFACTURER_LENGTH) String manufacturer,
      @Size(max = ValidationConstants.MAX_DEVICE_MODEL_LENGTH) String model) {
  }
}
