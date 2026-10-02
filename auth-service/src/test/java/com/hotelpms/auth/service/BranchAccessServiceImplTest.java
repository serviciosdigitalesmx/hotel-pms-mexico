package com.hotelpms.auth.service;

import com.hotelpms.auth.domain.TenantBranch;
import com.hotelpms.auth.domain.UserAccount;
import com.hotelpms.auth.domain.UserBranchMembership;
import com.hotelpms.auth.dto.TenantBranchResponse;
import com.hotelpms.auth.exception.DuplicateResourceException;
import com.hotelpms.auth.exception.NotFoundException;
import com.hotelpms.auth.repository.TenantBranchRepository;
import com.hotelpms.auth.repository.UserAccountRepository;
import com.hotelpms.auth.repository.UserBranchMembershipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchAccessServiceImplTest {

    private static final UUID HOTEL_A = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID HOTEL_B = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID BRANCH_A = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    private static final UUID BRANCH_B = UUID.fromString("00000000-0000-0000-0000-00000000000b");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final String USERNAME = "owner1";

    @Mock
    private TenantBranchRepository branchRepository;

    @Mock
    private UserBranchMembershipRepository membershipRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private BranchAccessServiceImpl branchAccessService;

    private TenantBranch branchA;
    private UserAccount user;

    @BeforeEach
    void setUp() {
        branchA = TenantBranch.builder()
                .id(BRANCH_A)
                .hotelId(HOTEL_A)
                .name("Main")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        user = UserAccount.builder()
                .id(USER_ID)
                .username(USERNAME)
                .hotelId(HOTEL_A)
                .build();
    }

    @Test
    void getBranchForTenantReturnsBranchWhenTenantMatches() {
        when(branchRepository.findByIdAndHotelId(BRANCH_A, HOTEL_A)).thenReturn(Optional.of(branchA));

        final TenantBranch result = branchAccessService.getBranchForTenant(HOTEL_A, BRANCH_A);

        assertThat(result.getId()).isEqualTo(BRANCH_A);
        assertThat(result.getHotelId()).isEqualTo(HOTEL_A);
    }

    @Test
    void getBranchForTenantThrowsNotFoundWhenBranchBelongsToAnotherTenant() {
        when(branchRepository.findByIdAndHotelId(BRANCH_A, HOTEL_B)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> branchAccessService.getBranchForTenant(HOTEL_B, BRANCH_A),
                "A branch UUID from another tenant must never resolve");
        verify(branchRepository, never()).findByIdAndHotelId(BRANCH_A, HOTEL_A);
    }

    @Test
    void assertUserCanSelectBranchDeniesWhenUserIsNotMember() {
        when(branchRepository.findByIdAndHotelId(BRANCH_A, HOTEL_A)).thenReturn(Optional.of(branchA));
        when(membershipRepository.existsByUserIdAndBranchIdAndHotelId(USER_ID, BRANCH_A, HOTEL_A))
                .thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> branchAccessService.assertUserCanSelectBranch(USER_ID, HOTEL_A, BRANCH_A));
    }

    @Test
    void assertUserCanSelectBranchAllowsMember() {
        when(branchRepository.findByIdAndHotelId(BRANCH_A, HOTEL_A)).thenReturn(Optional.of(branchA));
        when(membershipRepository.existsByUserIdAndBranchIdAndHotelId(USER_ID, BRANCH_A, HOTEL_A))
                .thenReturn(true);

        final TenantBranch result = branchAccessService.assertUserCanSelectBranch(USER_ID, HOTEL_A, BRANCH_A);

        assertThat(result.getId()).isEqualTo(BRANCH_A);
    }

    @Test
    void createBranchRejectsDuplicateNameInSameTenant() {
        when(branchRepository.existsByHotelIdAndNameIgnoreCase(HOTEL_A, "Main")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> branchAccessService.createBranch(HOTEL_A, "Main", USERNAME));
        verify(branchRepository, never()).save(any(TenantBranch.class));
    }

    @Test
    void createBranchCreatesTenantBranchAndMembershipForRequestingUser() {
        when(branchRepository.existsByHotelIdAndNameIgnoreCase(HOTEL_A, "North")).thenReturn(false);
        when(userAccountRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(branchRepository.save(any(TenantBranch.class))).thenAnswer(invocation -> {
            final TenantBranch unsaved = invocation.getArgument(0);
            unsaved.setId(BRANCH_A);
            return unsaved;
        });

        final TenantBranchResponse response = branchAccessService.createBranch(HOTEL_A, "North", USERNAME);

        final ArgumentCaptor<TenantBranch> branchCaptor = ArgumentCaptor.forClass(TenantBranch.class);
        verify(branchRepository).save(branchCaptor.capture());
        assertThat(branchCaptor.getValue().getHotelId()).isEqualTo(HOTEL_A);
        assertThat(branchCaptor.getValue().getName()).isEqualTo("North");

        final ArgumentCaptor<UserBranchMembership> membershipCaptor =
                ArgumentCaptor.forClass(UserBranchMembership.class);
        verify(membershipRepository).save(membershipCaptor.capture());
        assertThat(membershipCaptor.getValue().getUserId()).isEqualTo(USER_ID);
        assertThat(membershipCaptor.getValue().getHotelId()).isEqualTo(HOTEL_A);

        assertThat(response.hotelId()).isEqualTo(HOTEL_A);
        assertThat(response.name()).isEqualTo("North");
    }

    @Test
    void listBranchesMapsOnlyTenantBranches() {
        when(branchRepository.findAllByHotelIdAndActiveTrueOrderByNameAsc(HOTEL_A))
                .thenReturn(List.of(branchA));

        final List<TenantBranchResponse> result = branchAccessService.listBranches(HOTEL_A);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(BRANCH_A);
        assertThat(result.get(0).hotelId()).isEqualTo(HOTEL_A);
    }
}
