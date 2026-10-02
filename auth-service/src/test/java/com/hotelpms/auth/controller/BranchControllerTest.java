package com.hotelpms.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hotelpms.auth.domain.TenantBranch;
import com.hotelpms.auth.dto.CreateBranchRequest;
import com.hotelpms.auth.dto.TenantBranchResponse;
import com.hotelpms.auth.exception.GlobalExceptionHandler;
import com.hotelpms.auth.service.BranchAccessService;
import com.hotelpms.auth.service.CapabilityService;
import com.hotelpms.internalauth.contracts.Capability;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class BranchControllerTest {

    private static final String BASE_URL = "/api/v1/auth/branches";
    private static final String HEADER_HOTEL = "X-Auth-Hotel";
    private static final UUID HOTEL_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID BRANCH_ID = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    private static final String USERNAME = "owner1";

    @Mock
    private BranchAccessService branchAccessService;

    @Mock
    private CapabilityService capabilityService;

    @InjectMocks
    private BranchController branchController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UsernamePasswordAuthenticationToken auth;

    @BeforeEach
    void setUp() {
        auth = new UsernamePasswordAuthenticationToken(USERNAME, "", List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        final LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(branchController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listBranchesReturns200WhenCapabilityAllowed() throws Exception {
        final TenantBranchResponse branch = new TenantBranchResponse(
                BRANCH_ID, HOTEL_ID, "Main", true, LocalDateTime.now());
        when(branchAccessService.listBranches(HOTEL_ID)).thenReturn(List.of(branch));

        mockMvc.perform(get(BASE_URL)
                        .header(HEADER_HOTEL, HOTEL_ID.toString())
                        .with(req -> {
                            req.setUserPrincipal(auth);
                            return req;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(BRANCH_ID.toString()))
                .andExpect(jsonPath("$[0].hotelId").value(HOTEL_ID.toString()));

        verify(capabilityService).requireCapability(USERNAME, HOTEL_ID, Capability.BRANCHES_READ);
    }

    @Test
    void listBranchesReturns403WhenCapabilityDenied() throws Exception {
        doThrow(new AccessDeniedException("CAPABILITY_ACCESS_DENIED"))
                .when(capabilityService)
                .requireCapability(USERNAME, HOTEL_ID, Capability.BRANCHES_READ);

        mockMvc.perform(get(BASE_URL)
                        .header(HEADER_HOTEL, HOTEL_ID.toString())
                        .with(req -> {
                            req.setUserPrincipal(auth);
                            return req;
                        }))
                .andExpect(status().isForbidden());
    }

    @Test
    void getBranchReturns200WhenTenantAndBranchMatch() throws Exception {
        final TenantBranch branch = TenantBranch.builder()
                .id(BRANCH_ID)
                .hotelId(HOTEL_ID)
                .name("Main")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
        when(branchAccessService.getBranchForTenant(HOTEL_ID, BRANCH_ID)).thenReturn(branch);

        mockMvc.perform(get(BASE_URL + "/{branchId}", BRANCH_ID)
                        .header(HEADER_HOTEL, HOTEL_ID.toString())
                        .with(req -> {
                            req.setUserPrincipal(auth);
                            return req;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hotelId").value(HOTEL_ID.toString()))
                .andExpect(jsonPath("$.name").value("Main"));
    }

    @Test
    void createBranchReturns201WhenCapabilityAllowed() throws Exception {
        final TenantBranchResponse response = new TenantBranchResponse(
                BRANCH_ID, HOTEL_ID, "North", true, LocalDateTime.now());
        when(branchAccessService.createBranch(eq(HOTEL_ID), eq("North"), eq(USERNAME)))
                .thenReturn(response);
        final CreateBranchRequest request = new CreateBranchRequest("North");

        mockMvc.perform(post(BASE_URL)
                        .header(HEADER_HOTEL, HOTEL_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(req -> {
                            req.setUserPrincipal(auth);
                            return req;
                        }))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("North"))
                .andExpect(jsonPath("$.hotelId").value(HOTEL_ID.toString()));

        verify(capabilityService).requireCapability(USERNAME, HOTEL_ID, Capability.BRANCHES_MANAGE);
    }

    @Test
    void createBranchReturns403WhenCapabilityDenied() throws Exception {
        doThrow(new AccessDeniedException("CAPABILITY_ACCESS_DENIED"))
                .when(capabilityService)
                .requireCapability(USERNAME, HOTEL_ID, Capability.BRANCHES_MANAGE);
        final CreateBranchRequest request = new CreateBranchRequest("North");

        mockMvc.perform(post(BASE_URL)
                        .header(HEADER_HOTEL, HOTEL_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(req -> {
                            req.setUserPrincipal(auth);
                            return req;
                        }))
                .andExpect(status().isForbidden());
    }
}
