package com.hotelpms.guest.controller;

import com.hotelpms.guest.dto.request.DeviceCatalogMergeRequest;
import com.hotelpms.guest.dto.request.GuestRequest;
import com.hotelpms.guest.dto.response.DeviceCatalogEntry;
import com.hotelpms.guest.dto.response.DeviceCatalogMergeResponse;
import com.hotelpms.guest.dto.response.DeviceResponse;
import com.hotelpms.guest.dto.response.GuestResponse;
import com.hotelpms.guest.exception.GuestConflictException;
import com.hotelpms.guest.service.DeviceService;
import com.hotelpms.guest.service.GuestService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Fixi customer contract backed by the tenant-scoped guest aggregate. */
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@SuppressWarnings("checkstyle:DesignForExtension")
public class CustomerController {

  private static final String CUSTOMER_WRITE_ROLES =
      "hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST')";
  private static final String CSV_ROW_PREFIX = "Fila ";
  private static final int MAX_CSV_FILE_BYTES = 5_000_000;
  private static final int MAX_CSV_CUSTOMERS = 500;
  private static final int MIN_CSV_ROWS = 2;
  private static final int FULL_NAME_SPLIT_LIMIT = 2;
  private static final int CUSTOMER_PAGE_SIZE = 50;
  private static final int DEVICE_PAGE_SIZE = 25;

  private final GuestService service;
  private final Validator validator;
  private final DeviceService devices;

  /**
   * Looks for a customer that may already exist based on contact or tax data.
   *
   * @param email possible customer email
   * @param phone possible customer phone
   * @param rfc possible customer tax identifier
   * @return matching customer, or an empty response when none exists
   */
  @GetMapping("/potential-duplicate")
  public ResponseEntity<GuestResponse> potentialDuplicate(
      @RequestParam(required = false) final String email,
      @RequestParam(required = false) final String phone,
      @RequestParam(required = false) final String rfc) {
    final GuestRequest lookup = new GuestRequest(
        "Consulta", "Duplicado", email, phone, null, null, null, null, rfc,
        null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    final GuestResponse match = service.findPotentialDuplicate(lookup);
    return match == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(match);
  }

  /**
   * Imports validated customers from a UTF-8 CSV file.
   *
   * @param file uploaded customer data
   * @return import counts and row-level errors
   * @throws IOException if uploaded data cannot be read
   */
  @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize(CUSTOMER_WRITE_ROLES)
  public ResponseEntity<CsvImportResult> importCsv(@RequestPart("file") final MultipartFile file)
      throws IOException {
    if (file.isEmpty() || file.getSize() > MAX_CSV_FILE_BYTES) {
      return ResponseEntity.badRequest().body(new CsvImportResult(
          0, List.of("El archivo debe tener contenido y pesar máximo 5 MB.")));
    }

    final List<List<String>> rows;
    try {
      rows = parseCsv(new String(file.getBytes(), StandardCharsets.UTF_8));
    } catch (final IllegalArgumentException exception) {
      return ResponseEntity.unprocessableEntity().body(
          new CsvImportResult(0, List.of(exception.getMessage())));
    }
    if (rows.size() < MIN_CSV_ROWS) {
      return ResponseEntity.unprocessableEntity().body(
          new CsvImportResult(0, List.of("El CSV no contiene clientes.")));
    }
    if (rows.size() > MAX_CSV_CUSTOMERS + 1) {
      return ResponseEntity.badRequest().body(
          new CsvImportResult(0, List.of("El límite es 500 clientes por archivo.")));
    }

    final Map<String, Integer> headers = new HashMap<>();
    final List<String> headerRow = rows.get(0);
    for (int index = 0; index < headerRow.size(); index++) {
      headers.put(headerRow.get(index).replace("\uFEFF", "").trim().toLowerCase(Locale.ROOT), index);
    }
    final boolean splitName = headers.containsKey("first_name") && headers.containsKey("last_name");
    if (!splitName && !headers.containsKey("fullname")) {
      return ResponseEntity.unprocessableEntity().body(new CsvImportResult(
          0, List.of("Incluye FullName o las columnas first_name y last_name.")));
    }

    final List<Map.Entry<Integer, GuestRequest>> requests = new ArrayList<>();
    final List<String> errors = new ArrayList<>();
    for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
      final List<String> row = rows.get(rowIndex);
      if (row.stream().allMatch(String::isBlank)) {
        continue;
      }
      if (row.size() != headerRow.size()) {
        errors.add(CSV_ROW_PREFIX + (rowIndex + 1) + ": se esperaban " + headerRow.size()
            + " columnas y se recibieron " + row.size() + ".");
        continue;
      }

      String firstName = cell(row, headers, splitName ? "first_name" : "fullname");
      String lastName = cell(row, headers, "last_name");
      if (!splitName) {
        final String[] names = firstName.trim().split("\\s+", FULL_NAME_SPLIT_LIMIT);
        firstName = names[0];
        lastName = names.length > 1 ? names[1] : "";
      }
      final String email = cell(row, headers, "email");
      final String phone = cell(row, headers, "phone");
      final String rfc = cell(row, headers, "rfc");
      final String dni = cell(row, headers, "dni");
      final GuestRequest request = new GuestRequest(
          firstName, lastName, email, phone,
          cell(row, headers, "contactdescription", "address"),
          cell(row, headers, "city"), cell(row, headers, "country"), null, rfc,
          cell(row, headers, "fiscal_name"), cell(row, headers, "fiscal_postal_code"),
          cell(row, headers, "fiscal_regime"), cell(row, headers, "cfdi_use"),
          cell(row, headers, "billing_email"), null, null, null, null, null, null, null, null, dni);
      final Set<ConstraintViolation<GuestRequest>> violations = validator.validate(request);
      if (!violations.isEmpty()) {
        final String messages = violations.stream()
            .map(ConstraintViolation::getMessage)
            .sorted()
            .distinct()
            .collect(Collectors.joining(", "));
        errors.add(CSV_ROW_PREFIX + (rowIndex + 1) + ": " + messages);
      } else {
        requests.add(Map.entry(rowIndex + 1, request));
      }
    }

    int imported = 0;
    for (final Map.Entry<Integer, GuestRequest> entry : requests) {
      try {
        service.importGuests(List.of(entry.getValue()));
        imported++;
      } catch (final GuestConflictException | DataIntegrityViolationException exception) {
        errors.add(CSV_ROW_PREFIX + entry.getKey()
            + ": cliente duplicado o en conflicto con un registro existente.");
      }
    }
    if (imported == 0 && errors.isEmpty()) {
      errors.add("El CSV no contiene filas de clientes válidas.");
    }
    return ResponseEntity.ok(new CsvImportResult(imported, errors));
  }

  /**
   * Creates a customer.
   *
   * @param request validated customer data
   * @return created customer response
   */
  @PostMapping
  @PreAuthorize(CUSTOMER_WRITE_ROLES)
  public ResponseEntity<GuestResponse> create(@Valid @RequestBody final GuestRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.createGuest(request));
  }

  /**
   * Lists customers using the requested page.
   *
   * @param pageable pagination and ordering criteria
   * @return the requested page of customers
   */
  @GetMapping
  public Page<GuestResponse> list(
      @PageableDefault(size = CUSTOMER_PAGE_SIZE, sort = "lastName", direction = Sort.Direction.ASC)
      final Pageable pageable) {
    return service.getAllGuests(pageable);
  }

  /**
   * Exports customers as spreadsheet-safe UTF-8 CSV.
   *
   * @return downloadable CSV response
   */
  @GetMapping(value = "/export", produces = "text/csv")
  @PreAuthorize(CUSTOMER_WRITE_ROLES)
  public ResponseEntity<byte[]> export() {
    final StringBuilder csv = new StringBuilder(
        "first_name,last_name,email,billing_email,phone,rfc,identity_number,fiscal_name,"
            + "fiscal_postal_code,fiscal_regime,cfdi_use,address,city,country\r\n");
    service.getAllGuests(Pageable.unpaged()).getContent().forEach(customer -> csv.append(String.join(",",
        cell(customer.firstName()), cell(customer.lastName()), cell(customer.email()),
        cell(customer.billingEmail()), cell(customer.phone()), cell(customer.rfc()),
        cell(customer.identityNumber()), cell(customer.fiscalName()), cell(customer.fiscalPostalCode()),
        cell(customer.fiscalRegime()), cell(customer.cfdiUse()), cell(customer.address()),
        cell(customer.city()), cell(customer.country()))).append("\r\n"));
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=fixi-clientes.csv")
        .body(("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Retrieves a customer by its identifier.
   *
   * @param id customer identifier
   * @return customer response
   */
  @GetMapping("/{id}")
  public GuestResponse get(@PathVariable final UUID id) {
    return service.getGuestById(id);
  }

  /**
   * Updates a customer.
   *
   * @param id customer identifier
   * @param request validated replacement data
   * @return updated customer response
   */
  @PutMapping("/{id}")
  @PreAuthorize(CUSTOMER_WRITE_ROLES)
  public GuestResponse update(
      @PathVariable final UUID id, @Valid @RequestBody final GuestRequest request) {
    return service.updateGuest(id, request);
  }

  /**
   * Anonymizes a customer.
   *
   * @param id customer identifier
   * @return empty response after anonymization
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
  public ResponseEntity<Void> anonymize(@PathVariable final UUID id) {
    service.deleteGuest(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Searches customers by the supplied query.
   *
   * @param query search text
   * @param pageable pagination criteria
   * @return matching customer page
   */
  @GetMapping("/search")
  public Page<GuestResponse> search(
      @RequestParam(required = false) final String query,
      @PageableDefault(size = 50) final Pageable pageable) {
    return service.searchGuests(query, pageable);
  }

  /**
   * Searches the tenant's device catalog.
   *
   * @param query device search text
   * @param pageable pagination criteria
   * @return matching tenant devices
   */
  @GetMapping("/search-devices")
  public Page<DeviceResponse> searchDevices(
      @RequestParam final String query,
      @PageableDefault(size = DEVICE_PAGE_SIZE) final Pageable pageable) {
    return devices.search(query, pageable);
  }

  /**
   * Lists device catalog entries visible to the current tenant.
   *
   * @return tenant device catalog entries
   */
  @GetMapping("/device-catalog")
  @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST','TECHNICIAN')")
  public List<DeviceCatalogEntry> deviceCatalog() {
    return devices.catalog();
  }

  /**
   * Merges authorized device catalog entries.
   *
   * @param request catalog merge selection
   * @return resulting catalog entry and affected count
   */
  @PostMapping("/device-catalog/merge")
  @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER')")
  public DeviceCatalogMergeResponse mergeDeviceCatalog(
      @Valid @RequestBody final DeviceCatalogMergeRequest request) {
    return devices.mergeCatalog(request);
  }

  private static String cell(final String value) {
    if (value == null) {
      return "\"\"";
    }
    final String safe = value.matches("^[\\t\\r ]*[=+@-].*") ? "'" + value : value;
    return "\"" + safe.replace("\"", "\"\"") + "\"";
  }

  private static String cell(
      final List<String> row, final Map<String, Integer> headers, final String... names) {
    for (final String name : names) {
      final Integer index = headers.get(name);
      if (index != null && index < row.size()) {
        return row.get(index).trim();
      }
    }
    return "";
  }

  private static List<List<String>> parseCsv(final String csv) {
    final List<List<String>> rows = new ArrayList<>();
    final List<String> row = new ArrayList<>();
    final StringBuilder value = new StringBuilder();
    boolean quoted = false;
    for (int index = 0; index < csv.length(); index++) {
      final char character = csv.charAt(index);
      if (quoted) {
        if (character == '"' && index + 1 < csv.length() && csv.charAt(index + 1) == '"') {
          value.append('"');
          index++;
        } else if (character == '"') {
          quoted = false;
        } else {
          value.append(character);
        }
      } else if (character == '"' && value.isEmpty()) {
        quoted = true;
      } else if (character == ',') {
        row.add(value.toString());
        value.setLength(0);
      } else if (character == '\n') {
        row.add(value.toString().replaceFirst("\\r$", ""));
        value.setLength(0);
        if (row.stream().anyMatch(item -> !item.isBlank())) {
          rows.add(new ArrayList<>(row));
        }
        row.clear();
      } else {
        value.append(character);
      }
    }
    if (quoted) {
      throw new IllegalArgumentException("El CSV contiene comillas sin cerrar.");
    }
    if (value.length() > 0 || !row.isEmpty()) {
      row.add(value.toString().replaceFirst("\\r$", ""));
      if (row.stream().anyMatch(item -> !item.isBlank())) {
        rows.add(row);
      }
    }
    return rows;
  }

  /**
   * CSV import summary returned to the customer-management screen.
   *
   * @param imported number of customers persisted
   * @param errors row-level validation or conflict messages
   */
  public record CsvImportResult(int imported, List<String> errors) {
  }
}
