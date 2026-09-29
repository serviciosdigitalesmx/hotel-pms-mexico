package com.hotelpms.guest.service.impl;

import com.hotelpms.guest.dto.request.DeviceRequest;
import com.hotelpms.guest.exception.NotFoundException;
import com.hotelpms.guest.model.Device;
import com.hotelpms.guest.repository.DeviceRepository;
import com.hotelpms.guest.repository.GuestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceImplTest {
    @Mock DeviceRepository devices;
    @Mock GuestRepository guests;
    @InjectMocks DeviceServiceImpl service;
    private UUID hotelId;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        hotelId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        var authentication = new UsernamePasswordAuthenticationToken("worker", null);
        authentication.setDetails(hotelId.toString());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        lenient().when(guests.findByIdAndHotelId(customerId, hotelId))
                .thenReturn(Optional.of(mock(com.hotelpms.guest.model.Guest.class)));
    }

    @Test
    void createsDeviceForCustomerAndRejectsDuplicateImei() {
        var request = new DeviceRequest("phone", "Acme", "X", null, "imei-1", null);
        when(devices.findByHotelIdAndGuestIdAndImei(hotelId, customerId, "imei-1")).thenReturn(Optional.empty());
        when(devices.save(any(Device.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(customerId, service.create(customerId, request).customerId());

        when(devices.findByHotelIdAndGuestIdAndImei(hotelId, customerId, "imei-1"))
                .thenReturn(Optional.of(Device.builder().id(UUID.randomUUID()).build()));
        assertThrows(IllegalArgumentException.class, () -> service.create(customerId, request));
    }

    @Test
    void doesNotExposeCustomerFromAnotherTenant() {
        UUID foreignCustomer = UUID.randomUUID();
        when(guests.findByIdAndHotelId(foreignCustomer, hotelId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.list(foreignCustomer));
        verifyNoInteractions(devices);
    }
}
