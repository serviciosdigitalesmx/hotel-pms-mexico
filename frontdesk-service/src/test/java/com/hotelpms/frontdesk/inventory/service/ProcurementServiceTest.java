package com.hotelpms.frontdesk.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hotelpms.frontdesk.client.BranchValidationService;
import com.hotelpms.frontdesk.inventory.domain.InventoryProduct;
import com.hotelpms.frontdesk.inventory.domain.PurchaseOrder;
import com.hotelpms.frontdesk.inventory.domain.PurchaseOrderLine;
import com.hotelpms.frontdesk.inventory.domain.Supplier;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.PurchaseReceiptRequest;
import com.hotelpms.frontdesk.inventory.dto.InventoryDtos.ReceiptLine;
import com.hotelpms.frontdesk.inventory.repository.InventoryBalanceRepository;
import com.hotelpms.frontdesk.inventory.repository.InventoryProductRepository;
import com.hotelpms.frontdesk.inventory.repository.PurchaseOrderRepository;
import com.hotelpms.frontdesk.inventory.repository.StockReservationRepository;
import com.hotelpms.frontdesk.inventory.repository.SupplierRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

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

    @Test
    void replayingSameReceiptRequestDoesNotAddStockTwice() {
        UUID hotelId = UUID.randomUUID();
        UUID purchaseId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        InventoryProduct product =
                InventoryProduct.builder()
                        .id(productId)
                        .hotelId(hotelId)
                        .cost(new BigDecimal("10.00"))
                        .build();
        PurchaseOrderLine line = new PurchaseOrderLine();
        line.setProductId(productId);
        line.setQuantityOrdered(5);
        line.setQuantityReceived(0);
        line.setUnitCost(new BigDecimal("12.00"));
        PurchaseOrder purchase =
                PurchaseOrder.builder()
                        .id(purchaseId)
                        .hotelId(hotelId)
                        .branchId(branchId)
                        .status("PENDING")
                        .lines(new java.util.ArrayList<>(java.util.List.of(line)))
                        .build();
        line.setPurchaseOrder(purchase);
        PurchaseReceiptRequest request =
                new PurchaseReceiptRequest(
                        requestId, java.util.List.of(new ReceiptLine(productId, 2)));
        when(purchases.findForUpdateByIdAndHotelId(purchaseId, hotelId))
                .thenReturn(Optional.of(purchase));
        when(products.findForUpdateByIdAndHotelId(productId, hotelId))
                .thenReturn(Optional.of(product));
        when(products.save(product)).thenReturn(product);
        when(balances.sumQuantityByHotelAndProduct(hotelId, productId)).thenReturn(10L);
        when(reservations.sumReservedQuantityByHotelAndProduct(hotelId, productId)).thenReturn(0L);
        when(purchases.save(purchase)).thenReturn(purchase);

        service.receive(hotelId, purchaseId, request);
        service.receive(hotelId, purchaseId, request);

        assertThat(line.getQuantityReceived()).isEqualTo(2);
        assertThat(purchase.getReceiptRequestDigests()).containsKey(requestId.toString());
        verify(inventory, times(1))
                .move(org.mockito.ArgumentMatchers.eq(hotelId), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsNonPositiveReceiptLineBeforeChangingInventory() {
        UUID hotelId = UUID.randomUUID();
        UUID purchaseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        PurchaseOrderLine line = new PurchaseOrderLine();
        line.setProductId(productId);
        line.setQuantityOrdered(5);
        line.setQuantityReceived(0);
        PurchaseOrder purchase =
                PurchaseOrder.builder()
                        .id(purchaseId)
                        .hotelId(hotelId)
                        .status("PENDING")
                        .lines(new java.util.ArrayList<>(java.util.List.of(line)))
                        .build();
        when(purchases.findForUpdateByIdAndHotelId(purchaseId, hotelId))
                .thenReturn(Optional.of(purchase));

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.receive(
                                hotelId,
                                purchaseId,
                                new PurchaseReceiptRequest(
                                        java.util.List.of(
                                                new ReceiptLine(productId, 2),
                                                new ReceiptLine(productId, -1)))));

        assertThat(line.getQuantityReceived()).isZero();
        verify(inventory, never())
                .move(org.mockito.ArgumentMatchers.eq(hotelId), org.mockito.ArgumentMatchers.any());
    }
}
