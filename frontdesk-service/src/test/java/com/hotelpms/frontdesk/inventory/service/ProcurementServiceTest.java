package com.hotelpms.frontdesk.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hotelpms.frontdesk.inventory.domain.Supplier;
import com.hotelpms.frontdesk.inventory.repository.InventoryBalanceRepository;
import com.hotelpms.frontdesk.inventory.repository.InventoryProductRepository;
import com.hotelpms.frontdesk.inventory.repository.PurchaseOrderRepository;
import com.hotelpms.frontdesk.inventory.repository.StockReservationRepository;
import com.hotelpms.frontdesk.inventory.repository.SupplierRepository;
import com.hotelpms.frontdesk.client.BranchValidationService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcurementServiceTest {
  @Mock private SupplierRepository suppliers;
  @Mock private PurchaseOrderRepository purchases;
  @Mock private InventoryService inventory;
  @Mock private InventoryProductRepository products;
  @Mock private InventoryBalanceRepository balances;
  @Mock private StockReservationRepository reservations;
  @Mock private BranchValidationService branchValidation;
  @InjectMocks private ProcurementService service;

  @Test
  void archivesSupplierWithinTenantAndKeepsRecordForHistory() {
    UUID hotelId = UUID.randomUUID();
    UUID supplierId = UUID.randomUUID();
    Supplier supplier = new Supplier();
    supplier.setId(supplierId);
    supplier.setHotelId(hotelId);
    supplier.setName("Proveedor histórico");
    when(suppliers.findByIdAndHotelId(supplierId, hotelId)).thenReturn(Optional.of(supplier));
    when(suppliers.save(supplier)).thenReturn(supplier);

    Supplier archived = service.setSupplierActive(hotelId, supplierId, false);

    assertThat(archived.isActive()).isFalse();
    assertThat(archived.getName()).isEqualTo("Proveedor histórico");
    verify(suppliers).findByIdAndHotelId(supplierId, hotelId);
    verify(suppliers).save(supplier);
  }
}
