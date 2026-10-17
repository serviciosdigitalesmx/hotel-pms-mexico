package com.hotelpms.guest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Tenant-local request to normalize one or more device catalog identities into a target. */
public record DeviceCatalogMergeRequest(
    @NotEmpty List<@Valid Identity> sources,
    @NotNull @Valid Identity target) {

  /** A catalog identity; blank manufacturer/model values are valid. */
  public record Identity(
      @NotBlank @Size(max = 80) String category,
      @Size(max = 150) String manufacturer,
      @Size(max = 150) String model) { }
}
