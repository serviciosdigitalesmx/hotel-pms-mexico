package com.hotelpms.guest.controller;

import com.hotelpms.guest.dto.request.GuestRequest;
import com.hotelpms.guest.service.GuestService;
import com.hotelpms.guest.dto.response.GuestResponse;
import com.hotelpms.guest.service.DeviceService;
import com.hotelpms.guest.dto.response.DeviceResponse;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Fixi customer contract backed by the mature tenant-scoped guest aggregate. */
@RestController @RequestMapping("/api/v1/customers") @RequiredArgsConstructor
public class CustomerController {
  private final GuestService service;
  private final Validator validator;
  public record CsvImportResult(int imported, List<String> errors) { }
  @GetMapping("/potential-duplicate") public ResponseEntity<GuestResponse> potentialDuplicate(@RequestParam(required=false) String email,@RequestParam(required=false) String phone,@RequestParam(required=false) String rfc){var match=service.findPotentialDuplicate(new GuestRequest("Consulta","Duplicado",email,phone,null,null,null,null,rfc,null,null,null,null,null,null,null,null,null,null,null,null,null));return match==null?ResponseEntity.noContent().build():ResponseEntity.ok(match);}
  private final DeviceService devices;
  @PostMapping(value="/import",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST')")
  public ResponseEntity<CsvImportResult> importCsv(@RequestPart("file") MultipartFile file) throws java.io.IOException {
    if(file.isEmpty()||file.getSize()>5_000_000)return ResponseEntity.badRequest().body(new CsvImportResult(0,List.of("El archivo debe tener contenido y pesar máximo 5 MB.")));
    final List<List<String>> rows;
    try{rows=parseCsv(new String(file.getBytes(),StandardCharsets.UTF_8));}catch(IllegalArgumentException ex){return ResponseEntity.unprocessableEntity().body(new CsvImportResult(0,List.of(ex.getMessage())));}
    if(rows.size()<2)return ResponseEntity.unprocessableEntity().body(new CsvImportResult(0,List.of("El CSV no contiene clientes.")));
    if(rows.size()>501)return ResponseEntity.badRequest().body(new CsvImportResult(0,List.of("El límite es 500 clientes por archivo.")));
    final Map<String,Integer> headers=new HashMap<>();
    for(int i=0;i<rows.get(0).size();i++)headers.put(rows.get(0).get(i).replace("\uFEFF","").trim().toLowerCase(Locale.ROOT),i);
    final boolean splitName=headers.containsKey("first_name")&&headers.containsKey("last_name");
    if(!splitName&&!headers.containsKey("fullname"))return ResponseEntity.unprocessableEntity().body(new CsvImportResult(0,List.of("Incluye FullName o las columnas first_name y last_name.")));
    final List<GuestRequest> requests=new ArrayList<>();final List<String> errors=new ArrayList<>();
    for(int rowIndex=1;rowIndex<rows.size();rowIndex++){
      final List<String> row=rows.get(rowIndex);if(row.stream().allMatch(String::isBlank))continue;
      String first=cell(row,headers,splitName?"first_name":"fullname");String last=cell(row,headers,"last_name");
      if(!splitName){String[] names=first.trim().split("\\s+",2);first=names[0];last=names.length>1?names[1]:"";}
      final String email=cell(row,headers,"email"),phone=cell(row,headers,"phone");
      final String rfc=cell(row,headers,"rfc");final String dni=cell(row,headers,"dni");
      GuestRequest request=new GuestRequest(first,last,email,phone,cell(row,headers,"contactdescription","address"),cell(row,headers,"city"),cell(row,headers,"country"),null,rfc,cell(row,headers,"fiscal_name"),cell(row,headers,"fiscal_postal_code"),cell(row,headers,"fiscal_regime"),cell(row,headers,"cfdi_use"),cell(row,headers,"billing_email"),null,null,null,null,null,null,null,null,dni);
      Set<ConstraintViolation<GuestRequest>> violations=validator.validate(request);
      if(!violations.isEmpty())errors.add("Fila "+(rowIndex+1)+": "+violations.stream().map(ConstraintViolation::getMessage).sorted().distinct().reduce((a,b)->a+", "+b).orElse("datos inválidos"));
      else requests.add(request);
    }
    if(!errors.isEmpty())return ResponseEntity.unprocessableEntity().body(new CsvImportResult(0,errors));
    if(requests.isEmpty())return ResponseEntity.unprocessableEntity().body(new CsvImportResult(0,List.of("El CSV no contiene filas de clientes válidas.")));
    return ResponseEntity.ok(new CsvImportResult(service.importGuests(requests).size(),List.of()));
  }
  @PostMapping public ResponseEntity<GuestResponse> create(@Valid @RequestBody GuestRequest request){return ResponseEntity.status(HttpStatus.CREATED).body(service.createGuest(request));}
  @GetMapping public Page<GuestResponse> list(@PageableDefault(size=50,sort="lastName",direction=Sort.Direction.ASC) Pageable pageable){return service.getAllGuests(pageable);}
  @GetMapping(value="/export", produces="text/csv")
  @PreAuthorize("hasAnyRole('ADMIN','OWNER','MANAGER','RECEPTIONIST')")
  public ResponseEntity<byte[]> export(){
    final StringBuilder csv=new StringBuilder("first_name,last_name,email,billing_email,phone,rfc,identity_number,fiscal_name,fiscal_postal_code,fiscal_regime,cfdi_use,address,city,country\r\n");
    service.getAllGuests(Pageable.unpaged()).getContent().forEach(customer->csv.append(String.join(",",
        cell(customer.firstName()),cell(customer.lastName()),cell(customer.email()),cell(customer.billingEmail()),cell(customer.phone()),
        cell(customer.rfc()),cell(customer.identityNumber()),cell(customer.fiscalName()),cell(customer.fiscalPostalCode()),
        cell(customer.fiscalRegime()),cell(customer.cfdiUse()),cell(customer.address()),cell(customer.city()),
        cell(customer.country()))).append("\r\n"));
    return ResponseEntity.ok().contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
        .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=fixi-clientes.csv")
        .body(("\uFEFF"+csv).getBytes(java.nio.charset.StandardCharsets.UTF_8));
  }
  private static String cell(final String value){
    if(value==null)return "\"\"";
    final String safe=value.matches("^[\\t\\r ]*[=+@-].*")?"'"+value:value;
    return "\""+safe.replace("\"","\"\"")+"\"";
  }
  private static String cell(final List<String> row,final Map<String,Integer> headers,final String... names){
    for(final String name:names){final Integer index=headers.get(name);if(index!=null&&index<row.size())return row.get(index).trim();}return "";
  }
  private static List<List<String>> parseCsv(final String csv){
    final List<List<String>> rows=new ArrayList<>();final List<String> row=new ArrayList<>();final StringBuilder value=new StringBuilder();boolean quoted=false;
    for(int i=0;i<csv.length();i++){
      final char c=csv.charAt(i);
      if(quoted){if(c=='"'&&i+1<csv.length()&&csv.charAt(i+1)=='"'){value.append('"');i++;}else if(c=='"')quoted=false;else value.append(c);}
      else if(c=='"'&&value.isEmpty())quoted=true;
      else if(c==','){row.add(value.toString());value.setLength(0);}
      else if(c=='\n'){row.add(value.toString().replaceFirst("\\r$",""));value.setLength(0);if(row.stream().anyMatch(v->!v.isBlank()))rows.add(new ArrayList<>(row));row.clear();}
      else value.append(c);
    }
    if(quoted)throw new IllegalArgumentException("El CSV contiene comillas sin cerrar.");
    if(value.length()>0||!row.isEmpty()){row.add(value.toString().replaceFirst("\\r$",""));if(row.stream().anyMatch(v->!v.isBlank()))rows.add(row);}
    return rows;
  }
  @GetMapping("/{id}") public GuestResponse get(@PathVariable UUID id){return service.getGuestById(id);}
  @PutMapping("/{id}") public GuestResponse update(@PathVariable UUID id,@Valid @RequestBody GuestRequest request){return service.updateGuest(id,request);}
  @GetMapping("/search") public Page<GuestResponse> search(@RequestParam(required=false) String query,@PageableDefault(size=50) Pageable pageable){return service.searchGuests(query,pageable);}
  @GetMapping("/search-devices") public Page<DeviceResponse> searchDevices(@RequestParam String query,@PageableDefault(size=25) Pageable pageable){return devices.search(query,pageable);}
}
