# ============================================================
# CAMRA -> FIXI
# WORKPACK A02 — SUCURSALES / BRANCHES
# Ejecutar desde:
# D:\Projects\hotel-pms-mexico
# ============================================================

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$Repo = 'D:\Projects\hotel-pms-mexico'
$Workpack = 'A02'
$WorkpackName = 'SUCURSALES / BRANCHES'
$Timestamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$IsoNow = Get-Date -Format 'yyyy-MM-ddTHH:mm:ssK'

if (-not (Test-Path $Repo -PathType Container)) {
    throw "Repositorio no encontrado: $Repo"
}

Set-Location $Repo

if (-not (Test-Path '.git')) {
    throw "La ruta actual no parece ser el repositorio Git esperado: $Repo"
}

$RequiredFiles = @(
    'auth-service/src/main/java/com/hotelpms/auth/controller/BranchController.java',
    'auth-service/src/main/java/com/hotelpms/auth/service/BranchAccessService.java',
    'auth-service/src/main/java/com/hotelpms/auth/service/BranchAccessServiceImpl.java',
    'auth-service/src/main/java/com/hotelpms/auth/repository/TenantBranchRepository.java',
    'auth-service/src/main/java/com/hotelpms/auth/dto/CreateBranchRequest.java',
    'auth-service/src/main/java/com/hotelpms/auth/dto/TenantBranchResponse.java',
    'auth-service/src/main/resources/db/migration/V11__add_tenant_branch_contracts.sql'
)

foreach ($File in $RequiredFiles) {
    if (-not (Test-Path $File -PathType Leaf)) {
        throw "Falta archivo requerido del estado esperado de CAMRA: $File"
    }
}

Write-Host ''
Write-Host '=== ESTADO GIT ANTES DEL WORKPACK ==='
git status --short

if ($LASTEXITCODE -ne 0) {
    throw 'No fue posible consultar git status.'
}

# ------------------------------------------------------------
# No pisar cambios locales en archivos que este workpack toca.
# CAMRA_TO_FIXI_PROGRESS.md queda fuera porque pertenece al loop.
# ------------------------------------------------------------
$SourceTargets = @(
    'auth-service/src/main/java/com/hotelpms/auth/controller/BranchController.java',
    'auth-service/src/main/java/com/hotelpms/auth/service/BranchAccessService.java',
    'auth-service/src/main/java/com/hotelpms/auth/service/BranchAccessServiceImpl.java',
    'auth-service/src/main/java/com/hotelpms/auth/repository/TenantBranchRepository.java'
)

$DirtyTargets = @()

foreach ($Target in $SourceTargets) {
    $Dirty = git status --porcelain -- $Target
    if ($LASTEXITCODE -ne 0) {
        throw "No fue posible comprobar estado Git de $Target"
    }

    if ($Dirty) {
        $DirtyTargets += $Target
    }
}

if ($DirtyTargets.Count -gt 0) {
    throw @"
Hay cambios locales previos en archivos que A02 necesita modificar.
No se sobrescribirá nada.

Archivos:
$($DirtyTargets -join "`n")

Guarda/integra esos cambios y vuelve a ejecutar este mismo script.
"@
}

# ------------------------------------------------------------
# Checkpoint local. backup/ ya está ignorado por el repo.
# ------------------------------------------------------------
$BackupDir = Join-Path $Repo "backup/camra-to-fixi/$Workpack-$Timestamp"
New-Item -ItemType Directory -Path $BackupDir -Force | Out-Null

foreach ($Target in $SourceTargets) {
    $Dest = Join-Path $BackupDir $Target
    $DestDir = Split-Path $Dest -Parent
    New-Item -ItemType Directory -Path $DestDir -Force | Out-Null
    Copy-Item $Target $Dest -Force
}

Write-Host "Checkpoint: $BackupDir"

# ------------------------------------------------------------
# Helpers de edición segura / idempotente.
# ------------------------------------------------------------
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)

function Get-TextLf {
    param(
        [Parameter(Mandatory)]
        [string] $Path
    )

    $Text = [System.IO.File]::ReadAllText((Join-Path $Repo $Path))
    return $Text.Replace("`r`n", "`n").Replace("`r", "`n")
}

function Set-TextLf {
    param(
        [Parameter(Mandatory)]
        [string] $Path,

        [Parameter(Mandatory)]
        [string] $Content
    )

    $Absolute = Join-Path $Repo $Path
    $Normalized = $Content.Replace("`r`n", "`n").Replace("`r", "`n").TrimEnd() + "`n"
    [System.IO.File]::WriteAllText($Absolute, $Normalized, $Utf8NoBom)
}

function Replace-ExactOnce {
    param(
        [Parameter(Mandatory)]
        [string] $Path,

        [Parameter(Mandatory)]
        [string] $Old,

        [Parameter(Mandatory)]
        [string] $New,

        [Parameter(Mandatory)]
        [string] $AlreadyAppliedMarker
    )

    $Text = Get-TextLf $Path

    if ($Text.Contains($AlreadyAppliedMarker)) {
        Write-Host "SKIP ya aplicado: $Path -> $AlreadyAppliedMarker"
        return
    }

    if (-not $Text.Contains($Old)) {
        throw "No se encontró el anchor esperado en $Path. Se detiene para no corromper código."
    }

    $Occurrences = ([regex]::Matches(
        $Text,
        [regex]::Escape($Old)
    )).Count

    if ($Occurrences -ne 1) {
        throw "Anchor ambiguo en $Path. Coincidencias encontradas: $Occurrences"
    }

    $Updated = $Text.Replace($Old, $New)
    Set-TextLf -Path $Path -Content $Updated
    Write-Host "PATCH: $Path"
}

function Ensure-FileExact {
    param(
        [Parameter(Mandatory)]
        [string] $Path,

        [Parameter(Mandatory)]
        [string] $Content
    )

    $Absolute = Join-Path $Repo $Path

    if (Test-Path $Absolute) {
        $Existing = Get-TextLf $Path
        $Wanted = $Content.Replace("`r`n", "`n").Replace("`r", "`n").TrimEnd() + "`n"

        if ($Existing.TrimEnd() + "`n" -eq $Wanted) {
            Write-Host "SKIP archivo ya correcto: $Path"
            return
        }

        throw "El archivo $Path ya existe con contenido distinto. No se sobrescribe."
    }

    $Parent = Split-Path $Absolute -Parent
    New-Item -ItemType Directory -Path $Parent -Force | Out-Null
    Set-TextLf -Path $Path -Content $Content
    Write-Host "CREATE: $Path"
}

function Sanitize-ProgressCell {
    param([string] $Value)

    if ([string]::IsNullOrWhiteSpace($Value)) {
        return '-'
    }

    return $Value.Replace('|', '/').Replace("`r", ' ').Replace("`n", ' ')
}

# ============================================================
# 1. CREAR BASELINE PERSISTENTE DEL BACKLOG
# ============================================================
$ProgressFile = Join-Path $Repo 'CAMRA_TO_FIXI_PROGRESS.md'

$WorkpackCatalog = @'
A01|TENANT / NEGOCIO
A02|SUCURSALES / BRANCHES
A03|EMPLEADOS Y MEMBRESÍAS DE SUCURSAL
A04|CONTEXTO DE SUCURSAL ACTIVA
A05|MOTOR DE PERMISOS GRANULARES
A06|ADMINISTRACIÓN DE USUARIOS Y PERMISOS
A07|SECURITY ISOLATION HARDENING
B01|PLANS / CAPABILITIES ENGINE
B02|CONFIGURACIÓN MULTI-INDUSTRIA
B03|CAMPOS DINÁMICOS
B04|FORMULARIOS Y LABELS CONFIGURABLES
B05|CATÁLOGOS Y TEMPLATES POR INDUSTRIA
C01|FASES SEMÁNTICAS
C02|ESTADOS CONFIGURABLES
C03|TRANSICIONES CONFIGURABLES
C04|TRANSICIONES ATÓMICAS / CONCURRENCIA
C05|WORKFLOW ADMIN UI
D01|CUSTOMER
D02|CUSTOMER DEDUPLICATION / MATCHING
D03|DEVICE / ASSET
D04|DEVICE HISTORY
D05|HISTORICAL SNAPSHOTS
E01|REQUEST INBOX
E02|REQUEST REVIEW
E03|REQUEST CONVERSION
E04|PUBLIC REQUEST ENTRY
F01|SERVICE ORDER CORE
F02|SERVICE ORDER API
F03|SERVICE ORDER FRONTEND
F04|SERVICE ORDER TIMELINE
F05|ASSIGNMENT / TECHNICIANS
F06|CANCELLATION / CLOSURE
G01|DIAGNÓSTICO
G02|WORK LOGS
G03|TIME TRACKING
G04|QA / COMPLETION
H01|QUOTE AGGREGATE
H02|QUOTE VERSIONING
H03|QUOTE ITEMS
H04|QUOTE IMMUTABILITY
H05|QUOTE FRONTEND
I01|AUTHORIZATION MODEL
I02|CUSTOMER SELF-AUTHORIZATION
I03|EMPLOYEE-RECORDED AUTHORIZATION
I04|AUTHORIZATION EVIDENCE
I05|REAUTHORIZATION
J01|PRODUCT CATALOG
J02|INVENTORY PER BRANCH
J03|INVENTORY LEDGER / MOVEMENTS
J04|STOCK RESERVATIONS
J05|STOCK CONSUMPTION
J06|RESERVATION RELEASE
J07|INVENTORY CONCURRENCY
J08|OPTIONAL INVENTORY
K01|SUPPLIERS
K02|PURCHASES
K03|JUST-IN-TIME PURCHASE
K04|PURCHASE -> INVENTORY
K05|PURCHASE -> SERVICE ORDER COST
K06|PENDING PURCHASE FLOW
L01|DOCUMENT INGESTION
L02|OCR / AI EXTRACTION
L03|CATALOG MATCHING
L04|NEW PRODUCT PROPOSAL
L05|HUMAN REVIEW
L06|TRANSACTIONAL APPLY
M01|XLSX IMPORT
M02|CSV IMPORT
M03|PDF IMPORT
M04|COLUMN MAPPING
M05|IMPORT PREVIEW / VALIDATION
M06|BULK APPLY
N01|FINANCIAL LEDGER
N02|ORDER VALUE VS COLLECTED
N03|ACCOUNTS RECEIVABLE
N04|FINANCIAL REPORTING FOUNDATION
O01|PAYMENTS
O02|DEPOSITS
O03|PARTIAL PAYMENTS
O04|FINAL PAYMENT
O05|PAYMENT METHODS / REFERENCES
O06|REFUNDS / REVERSALS
O07|PAYMENT IDEMPOTENCY / CONCURRENCY
P01|CASH SESSION
P02|CASH MOVEMENTS
P03|CASH RECONCILIATION
P04|CASH PERMISSIONS / AUDIT
Q01|EXPENSES
Q02|EXPENSE -> LEDGER/CASH
R01|POS SALE
R02|POS ITEMS
R03|POS PAYMENT
R04|POS INVENTORY
R05|POS CASH
R06|POS REFUND
R07|POS FRONTEND
S01|WARRANTY MODEL
S02|WARRANTY PERIOD / TERMS
S03|WARRANTY CLAIM
S04|WARRANTY SERVICE FLOW
T01|DOCUMENT / EVIDENCE FOUNDATION
T02|SERVICE ORDER PHOTOS
T03|FINANCIAL / PURCHASE DOCUMENTS
T04|AUTHORIZATION EVIDENCE
T05|DOCUMENT ACCESS CONTROL
U01|SECURE PORTAL ACCESS
U02|ORDER TRACKING
U03|DEVICE INFORMATION
U04|QUOTE VIEW
U05|QUOTE AUTHORIZATION
U06|PAYMENTS / BALANCE VIEW
U07|PORTAL DOCUMENTS
U08|REMOVE LEGACY PUBLIC CONTRACTS
V01|TENANT WHATSAPP CONNECTION
V02|QR / CONNECTION STATUS
V03|INBOUND WEBHOOK
V04|WEBHOOK SECURITY / IDEMPOTENCY
V05|OUTBOUND MESSAGES
V06|DELIVERY / RETRIES
V07|CONVERSATIONS
V08|CUSTOMER CORRELATION
V09|SERVICE ORDER CORRELATION
V10|WHATSAPP OPERATIONAL UI
W01|MULTICHANNEL NOTIFICATION FOUNDATION
W02|NOTIFICATION TEMPLATES
W03|EVENT-DRIVEN NOTIFICATIONS
W04|DELIVERY / RETRY / AUDIT
X01|FIXI AI CONTEXT
X02|CUSTOMER TOOLS
X03|DEVICE TOOLS
X04|SERVICE ORDER TOOLS
X05|QUOTE TOOLS
X06|INVENTORY TOOLS
X07|FINANCIAL TOOLS
X08|AI MUTATION SECURITY
X09|AI TOOL AUDIT
Y01|AUTOMATION ENGINE FOUNDATION
Y02|ORDER AUTOMATIONS
Y03|QUOTE / AUTHORIZATION AUTOMATIONS
Y04|PROCUREMENT AUTOMATIONS
Y05|PAYMENT AUTOMATIONS
Y06|WARRANTY AUTOMATIONS
Y07|AUTOMATION RETRY / IDEMPOTENCY
Z01|OWNER DASHBOARD
Z02|BRANCH DASHBOARD
Z03|ORDER REPORTING
Z04|FINANCIAL REPORTING
Z05|INVENTORY REPORTING
Z06|TECHNICIAN / PRODUCTIVITY REPORTING
AA01|AUDIT FOUNDATION
AA02|SECURITY AUDIT EVENTS
AA03|OPERATIONAL AUDIT EVENTS
AA04|FINANCIAL AUDIT EVENTS
AA05|INVENTORY AUDIT EVENTS
AB01|FIXI NAVIGATION
AB02|FIXI DASHBOARD SHELL
AB03|PERMISSION-AWARE UI
AB04|CAPABILITY-AWARE UI
AB05|INDUSTRY-AWARE UI
AB06|RESPONSIVE / OPERATIONAL POLISH
AC01|API CONTRACT CONSISTENCY
AC02|VALIDATION / NORMALIZED ERRORS
AC03|PAGINATION / FILTERING / SORTING
AC04|IDEMPOTENCY FRAMEWORK
AC05|CROSS-SERVICE EVENTS
AD01|FIXI METRICS
AD02|STRUCTURED LOGGING / CORRELATION
AD03|TRACING
AD04|ALERTS
AD05|CRITICAL INTEGRATION MONITORING
AE01|HOTEL DOMAIN DEPENDENCY AUDIT
AE02|REMOVE HOTEL FRONTEND
AE03|REMOVE HOTEL API ROUTES
AE04|REMOVE HOTEL DOMAIN CODE
AE05|DATABASE CLEANUP
AE06|HOTEL TERMINOLOGY CLEANUP
AE07|DEAD CODE CLEANUP
AF01|TENANT ISOLATION E2E
AF02|BRANCH ISOLATION E2E
AF03|PERMISSIONS E2E
AF04|CUSTOMER -> DEVICE -> ORDER E2E
AF05|DIAGNOSIS -> QUOTE -> AUTHORIZATION E2E
AF06|NO-INVENTORY REPAIR E2E
AF07|INVENTORY REPAIR E2E
AF08|PAYMENTS E2E
AF09|FINANCIAL CONSISTENCY E2E
AF10|POS E2E
AF11|WHATSAPP E2E
AF12|PORTAL E2E
AF13|AI SECURITY E2E
AF14|WORKFLOW E2E
AF15|MULTI-INDUSTRY E2E
AF16|MIGRATION VALIDATION
AF17|BACKEND FULL BUILD
AF18|FRONTEND FULL BUILD
AF19|DOCKER / RUNTIME VALIDATION
AF20|OBSERVABILITY VALIDATION
AG01|FINAL HOTEL LEFTOVER SCAN
AG02|SECURITY FINAL AUDIT
AG03|DEAD CODE / DUPLICATE CONTRACT AUDIT
AG04|FINAL CRITICAL REGRESSION
AG05|CAMRA -> FIXI COMPLETE
'@

if (-not (Test-Path $ProgressFile)) {

    $Initial = @{
        A01 = @{
            Status = '[x]'
            Files = 'auth-service; api-gateway; internal-auth-lib; V10 tenant/branch contracts'
            Tests = 'historical evidence: foundation-contracts Gradle auth-service + api-gateway PASS'
            Notes = 'Foundation integrated; tenant boundary and server-side isolation already exist.'
        }
        A02 = @{
            Status = '[~]'
            Files = 'TenantBranch; BranchController; BranchAccessService; V10 existing schema'
            Tests = 'current workpack executing'
            Notes = 'Create/read/validation already exist; edit/deactivate are the verified gap being completed now.'
        }
        A03 = @{
            Status = '[~]'
            Files = 'UserBranchMembership; V10'
            Tests = 'foundation tests exist'
            Notes = 'Membership persistence exists; complete employee assignment/admin lifecycle still requires backlog validation.'
        }
        A04 = @{
            Status = '[~]'
            Files = 'SelectBranchRequest; BranchAccessService'
            Tests = 'foundation branch isolation tests exist'
            Notes = 'Server-side selection validation exists; complete backlog behavior still requires dedicated validation.'
        }
        A05 = @{
            Status = '[~]'
            Files = 'UserCapabilityGrant; CapabilityService; internal-auth-lib'
            Tests = 'foundation capability tests exist'
            Notes = 'Granular capability foundation exists; complete Fixi permission catalog/enforcement remains to be audited.'
        }
        B01 = @{
            Status = '[x]'
            Files = 'auth-service tenant_plan; tenant_capability; capability audit schema'
            Tests = 'historical evidence: auth-service + config-service BUILD SUCCESSFUL'
            Notes = 'plans-capabilities worker integrated; no commercial prices hardcoded.'
        }
        C01 = @{
            Status = '[~]'
            Files = 'frontdesk-service workflow engine'
            Tests = 'workflow module tests historically passed'
            Notes = 'Implementation exists; final integration gate was not clean globally.'
        }
        C02 = @{
            Status = '[~]'
            Files = 'frontdesk-service workflow definitions/states'
            Tests = 'workflow module tests historically passed'
            Notes = 'Implementation exists; backlog-level final validation pending.'
        }
        C03 = @{
            Status = '[~]'
            Files = 'frontdesk-service workflow transitions'
            Tests = 'workflow module tests historically passed'
            Notes = 'Implementation exists; backlog-level final validation pending.'
        }
        C04 = @{
            Status = '[~]'
            Files = 'frontdesk-service workflow transition locking'
            Tests = 'transition concurrency tests historically passed'
            Notes = 'Implementation exists; backlog-level final validation pending.'
        }
        D03 = @{
            Status = '[~]'
            Files = 'guest-service Device; V11 customer devices'
            Tests = 'customer-device worker PASS'
            Notes = 'Device implementation integrated; full backlog requirements such as configurable fields require later verification.'
        }
        D04 = @{
            Status = '[~]'
            Files = 'Device + order history related implementation'
            Tests = 'customer-device worker PASS'
            Notes = 'Partial verified implementation; complete history behavior still requires backlog validation.'
        }
        D05 = @{
            Status = '[~]'
            Files = 'frontdesk-service V24 device history snapshot'
            Tests = 'guest-service + frontdesk-service tests historically passed'
            Notes = 'Historical snapshot support exists; complete ServiceOrder migration remains dependency-sensitive.'
        }
    }

    $Builder = [System.Text.StringBuilder]::new()

    [void]$Builder.AppendLine('# CAMRA -> FIXI PROGRESS')
    [void]$Builder.AppendLine('')
    [void]$Builder.AppendLine("Initial baseline generated: $IsoNow")
    [void]$Builder.AppendLine('')
    [void]$Builder.AppendLine('Source of truth: real repository state + existing execution evidence.')
    [void]$Builder.AppendLine('')
    [void]$Builder.AppendLine('Legend: `[ ] PENDING` · `[~] IN PROGRESS/PARTIAL` · `[x] DONE` · `[!] BLOCKED`')
    [void]$Builder.AppendLine('')
    [void]$Builder.AppendLine('| Workpack | Name | Status | Date | Files / migrations | Tests executed / evidence | Notes / blockers |')
    [void]$Builder.AppendLine('|---|---|---|---|---|---|---|')

    foreach ($RawLine in ($WorkpackCatalog -split "`n")) {
        $Line = $RawLine.Trim()
        if (-not $Line) {
            continue
        }

        $Parts = $Line.Split('|', 2)
        $Id = $Parts[0].Trim()
        $Name = $Parts[1].Trim()

        if ($Initial.ContainsKey($Id)) {
            $Entry = $Initial[$Id]
            $Status = $Entry.Status
            $Date = $IsoNow
            $Files = Sanitize-ProgressCell $Entry.Files
            $Tests = Sanitize-ProgressCell $Entry.Tests
            $Notes = Sanitize-ProgressCell $Entry.Notes
        }
        else {
            $Status = '[ ]'
            $Date = '-'
            $Files = '-'
            $Tests = '-'
            $Notes = 'Pending repository-level comparison when dependencies make this workpack executable.'
        }

        [void]$Builder.AppendLine(
            "| $Id | $(Sanitize-ProgressCell $Name) | $Status | $Date | $Files | $Tests | $Notes |"
        )
    }

    [void]$Builder.AppendLine('')
    [void]$Builder.AppendLine('## Execution log')
    [void]$Builder.AppendLine('')
    [void]$Builder.AppendLine("- $IsoNow — A02 started from verified partial Branch implementation.")

    [System.IO.File]::WriteAllText(
        $ProgressFile,
        $Builder.ToString().Replace("`r`n", "`n"),
        $Utf8NoBom
    )

    Write-Host 'CREATE: CAMRA_TO_FIXI_PROGRESS.md'
}
else {
    Write-Host 'KEEP: CAMRA_TO_FIXI_PROGRESS.md ya existe; no se reinicia el progreso.'
}

function Update-ProgressRow {
    param(
        [Parameter(Mandatory)]
        [string] $Id,

        [Parameter(Mandatory)]
        [string] $Status,

        [Parameter(Mandatory)]
        [string] $Files,

        [Parameter(Mandatory)]
        [string] $Tests,

        [Parameter(Mandatory)]
        [string] $Notes
    )

    if (-not (Test-Path $ProgressFile)) {
        throw 'No existe CAMRA_TO_FIXI_PROGRESS.md'
    }

    $Lines = [System.Collections.Generic.List[string]]::new()

    foreach ($Line in [System.IO.File]::ReadAllLines($ProgressFile)) {
        [void]$Lines.Add($Line)
    }

    $Found = $false
    $Now = Get-Date -Format 'yyyy-MM-ddTHH:mm:ssK'

    for ($i = 0; $i -lt $Lines.Count; $i++) {
        if ($Lines[$i].StartsWith("| $Id |")) {
            $NameParts = $Lines[$i].Split('|')
            $Name = if ($NameParts.Count -gt 2) {
                $NameParts[2].Trim()
            }
            else {
                $Id
            }

            $Lines[$i] = (
                "| $Id | $(Sanitize-ProgressCell $Name) | $Status | $Now | " +
                "$(Sanitize-ProgressCell $Files) | " +
                "$(Sanitize-ProgressCell $Tests) | " +
                "$(Sanitize-ProgressCell $Notes) |"
            )

            $Found = $true
            break
        }
    }

    if (-not $Found) {
        throw "No se encontró el workpack $Id en CAMRA_TO_FIXI_PROGRESS.md"
    }

    [System.IO.File]::WriteAllLines($ProgressFile, $Lines, $Utf8NoBom)
}

Update-ProgressRow `
    -Id 'A02' `
    -Status '[~]' `
    -Files 'TenantBranch; TenantBranchRepository; BranchAccessService; BranchController' `
    -Tests 'workpack currently executing' `
    -Notes 'Completing edit, soft-deactivation and inactive-history query without replacing V10.'

try {

    # ============================================================
    # 2. DTO DE ACTUALIZACIÓN
    # ============================================================
    $UpdateBranchDto = @'
package com.hotelpms.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for renaming a branch inside the authenticated tenant.
 * Tenant and branch ownership are resolved server-side.
 *
 * @param name new branch display name
 */
public record UpdateBranchRequest(
        @NotBlank @Size(max = 120) String name) {
}
'@

    Ensure-FileExact `
        -Path 'auth-service/src/main/java/com/hotelpms/auth/dto/UpdateBranchRequest.java' `
        -Content $UpdateBranchDto

    # ============================================================
    # 3. REPOSITORY — consulta administrativa incluyendo inactivas
    # ============================================================
    $RepositoryOld = @'
    List<TenantBranch> findAllByHotelIdAndActiveTrueOrderByNameAsc(UUID hotelId);

    /**
     * Finds a branch by id and tenant.
'@

    $RepositoryNew = @'
    List<TenantBranch> findAllByHotelIdAndActiveTrueOrderByNameAsc(UUID hotelId);

    /**
     * Lists every branch for a tenant, including inactive historical records.
     *
     * @param hotelId the tenant UUID
     * @return all branches belonging to that tenant
     */
    List<TenantBranch> findAllByHotelIdOrderByNameAsc(UUID hotelId);

    /**
     * Finds a branch by id and tenant.
'@

    Replace-ExactOnce `
        -Path 'auth-service/src/main/java/com/hotelpms/auth/repository/TenantBranchRepository.java' `
        -Old $RepositoryOld `
        -New $RepositoryNew `
        -AlreadyAppliedMarker 'findAllByHotelIdOrderByNameAsc'

    # ============================================================
    # 4. SERVICE CONTRACT
    # ============================================================
    $ServiceListOld = @'
    List<TenantBranchResponse> listBranches(UUID hotelId);

    /**
     * Creates a branch and immediately gives the requesting user membership.
'@

    $ServiceListNew = @'
    List<TenantBranchResponse> listBranches(UUID hotelId);

    /**
     * Lists tenant branches and optionally includes inactive historical records.
     *
     * @param hotelId        the tenant UUID
     * @param includeInactive whether inactive branches should be returned
     * @return branches belonging to the tenant
     */
    List<TenantBranchResponse> listBranches(UUID hotelId, boolean includeInactive);

    /**
     * Creates a branch and immediately gives the requesting user membership.
'@

    Replace-ExactOnce `
        -Path 'auth-service/src/main/java/com/hotelpms/auth/service/BranchAccessService.java' `
        -Old $ServiceListOld `
        -New $ServiceListNew `
        -AlreadyAppliedMarker 'listBranches(UUID hotelId, boolean includeInactive)'

    $ServiceLifecycleOld = @'
    TenantBranchResponse createBranch(UUID hotelId, String name, String requestingUsername);

    /**
     * Validates that a branch exists and belongs to the given tenant.
'@

    $ServiceLifecycleNew = @'
    TenantBranchResponse createBranch(UUID hotelId, String name, String requestingUsername);

    /**
     * Renames an active branch inside the authenticated tenant.
     *
     * @param hotelId      tenant UUID
     * @param branchId     branch UUID
     * @param name         new branch display name
     * @param actorUsername authenticated actor for audit logging
     * @return updated branch
     */
    TenantBranchResponse updateBranch(UUID hotelId, UUID branchId, String name, String actorUsername);

    /**
     * Soft-deactivates a branch while retaining its row and memberships.
     *
     * @param hotelId      tenant UUID
     * @param branchId     branch UUID
     * @param actorUsername authenticated actor for audit logging
     * @return deactivated branch
     */
    TenantBranchResponse deactivateBranch(UUID hotelId, UUID branchId, String actorUsername);

    /**
     * Validates that a branch exists and belongs to the given tenant.
'@

    Replace-ExactOnce `
        -Path 'auth-service/src/main/java/com/hotelpms/auth/service/BranchAccessService.java' `
        -Old $ServiceLifecycleOld `
        -New $ServiceLifecycleNew `
        -AlreadyAppliedMarker 'deactivateBranch(UUID hotelId, UUID branchId, String actorUsername)'

    # ============================================================
    # 5. SERVICE IMPLEMENTATION
    # ============================================================
    $ImplListOld = @'
    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<TenantBranchResponse> listBranches(final UUID hotelId) {
        return branchRepository.findAllByHotelIdAndActiveTrueOrderByNameAsc(hotelId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public TenantBranchResponse createBranch
'@

    $ImplListNew = @'
    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<TenantBranchResponse> listBranches(final UUID hotelId) {
        return listBranches(hotelId, false);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<TenantBranchResponse> listBranches(final UUID hotelId, final boolean includeInactive) {
        final List<TenantBranch> branches = includeInactive
                ? branchRepository.findAllByHotelIdOrderByNameAsc(hotelId)
                : branchRepository.findAllByHotelIdAndActiveTrueOrderByNameAsc(hotelId);

        return branches.stream()
                .map(this::toResponse)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public TenantBranchResponse createBranch
'@

    Replace-ExactOnce `
        -Path 'auth-service/src/main/java/com/hotelpms/auth/service/BranchAccessServiceImpl.java' `
        -Old $ImplListOld `
        -New $ImplListNew `
        -AlreadyAppliedMarker 'listBranches(final UUID hotelId, final boolean includeInactive)'

    $ImplLifecycleAnchor = @'
        return toResponse(savedBranch);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public TenantBranch getBranchForTenant
'@

    $ImplLifecycleReplacement = @'
        return toResponse(savedBranch);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public TenantBranchResponse updateBranch(final UUID hotelId, final UUID branchId,
            final String name, final String actorUsername) {
        final TenantBranch branch = branchRepository.findByIdAndHotelId(branchId, hotelId)
                .orElseThrow(() -> new NotFoundException(BRANCH_NOT_FOUND));

        if (!branch.isActive()) {
            throw new NotFoundException(BRANCH_NOT_FOUND);
        }

        final String trimmedName = name.trim();

        if (!branch.getName().equalsIgnoreCase(trimmedName)
                && branchRepository.existsByHotelIdAndNameIgnoreCase(hotelId, trimmedName)) {
            throw new DuplicateResourceException("BRANCH_NAME_EXISTS");
        }

        branch.setName(trimmedName);
        final TenantBranch savedBranch = branchRepository.save(branch);

        log.info("[AUTH] BRANCH_UPDATED | branchId={} | hotelId={} | name={} | by={}",
                savedBranch.getId(), hotelId, trimmedName, actorUsername);

        return toResponse(savedBranch);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public TenantBranchResponse deactivateBranch(final UUID hotelId, final UUID branchId,
            final String actorUsername) {
        final TenantBranch branch = branchRepository.findByIdAndHotelId(branchId, hotelId)
                .orElseThrow(() -> new NotFoundException(BRANCH_NOT_FOUND));

        if (!branch.isActive()) {
            throw new NotFoundException(BRANCH_NOT_FOUND);
        }

        branch.setActive(false);
        final TenantBranch savedBranch = branchRepository.save(branch);

        log.info("[AUTH] BRANCH_DEACTIVATED | branchId={} | hotelId={} | by={}",
                savedBranch.getId(), hotelId, actorUsername);

        return toResponse(savedBranch);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public TenantBranch getBranchForTenant
'@

    Replace-ExactOnce `
        -Path 'auth-service/src/main/java/com/hotelpms/auth/service/BranchAccessServiceImpl.java' `
        -Old $ImplLifecycleAnchor `
        -New $ImplLifecycleReplacement `
        -AlreadyAppliedMarker 'public TenantBranchResponse deactivateBranch'

    # ============================================================
    # 6. CONTROLLER
    # ============================================================
    $ControllerDtoImportOld = @'
import com.hotelpms.auth.dto.CreateBranchRequest;
import com.hotelpms.auth.dto.TenantBranchResponse;
'@

    $ControllerDtoImportNew = @'
import com.hotelpms.auth.dto.CreateBranchRequest;
import com.hotelpms.auth.dto.TenantBranchResponse;
import com.hotelpms.auth.dto.UpdateBranchRequest;
'@

    Replace-ExactOnce `
        -Path 'auth-service/src/main/java/com/hotelpms/auth/controller/BranchController.java' `
        -Old $ControllerDtoImportOld `
        -New $ControllerDtoImportNew `
        -AlreadyAppliedMarker 'import com.hotelpms.auth.dto.UpdateBranchRequest;'

    $ControllerWebImportOld = @'
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
'@

    $ControllerWebImportNew = @'
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
'@

    Replace-ExactOnce `
        -Path 'auth-service/src/main/java/com/hotelpms/auth/controller/BranchController.java' `
        -Old $ControllerWebImportOld `
        -New $ControllerWebImportNew `
        -AlreadyAppliedMarker 'import org.springframework.web.bind.annotation.RequestParam;'

    $ControllerListOld = @'
    @GetMapping
    public ResponseEntity<List<TenantBranchResponse>> listBranches(
            @NonNull @RequestHeader(HEADER_HOTEL) final UUID hotelId,
            @NonNull final Authentication auth) {
        capabilityService.requireCapability(auth.getName(), hotelId, Capability.BRANCHES_READ);
        return ResponseEntity.ok(branchAccessService.listBranches(hotelId));
    }
'@

    $ControllerListNew = @'
    @GetMapping
    public ResponseEntity<List<TenantBranchResponse>> listBranches(
            @NonNull @RequestHeader(HEADER_HOTEL) final UUID hotelId,
            @RequestParam(defaultValue = "false") final boolean includeInactive,
            @NonNull final Authentication auth) {
        capabilityService.requireCapability(auth.getName(), hotelId, Capability.BRANCHES_READ);

        if (includeInactive) {
            return ResponseEntity.ok(branchAccessService.listBranches(hotelId, true));
        }

        return ResponseEntity.ok(branchAccessService.listBranches(hotelId));
    }
'@

    Replace-ExactOnce `
        -Path 'auth-service/src/main/java/com/hotelpms/auth/controller/BranchController.java' `
        -Old $ControllerListOld `
        -New $ControllerListNew `
        -AlreadyAppliedMarker '@RequestParam(defaultValue = "false") final boolean includeInactive'

    $ControllerLifecycleAnchor = @'
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(branchAccessService.createBranch(hotelId, request.name(), auth.getName()));
    }

    /**
     * Returns a single branch, validating it belongs to the authenticated tenant.
'@

    $ControllerLifecycleReplacement = @'
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(branchAccessService.createBranch(hotelId, request.name(), auth.getName()));
    }

    /**
     * Renames an active branch inside the authenticated tenant.
     *
     * @param hotelId  tenant UUID from the authenticated gateway context
     * @param branchId branch UUID
     * @param request  validated new name
     * @param auth     authenticated caller
     * @return updated branch
     */
    @PatchMapping("/{branchId}")
    public ResponseEntity<TenantBranchResponse> updateBranch(
            @NonNull @RequestHeader(HEADER_HOTEL) final UUID hotelId,
            @NonNull @PathVariable final UUID branchId,
            @NonNull @Valid @RequestBody final UpdateBranchRequest request,
            @NonNull final Authentication auth) {
        capabilityService.requireCapability(auth.getName(), hotelId, Capability.BRANCHES_MANAGE);
        return ResponseEntity.ok(
                branchAccessService.updateBranch(hotelId, branchId, request.name(), auth.getName()));
    }

    /**
     * Soft-deactivates a branch without deleting its history.
     *
     * @param hotelId  tenant UUID from the authenticated gateway context
     * @param branchId branch UUID
     * @param auth     authenticated caller
     * @return deactivated branch
     */
    @PatchMapping("/{branchId}/deactivate")
    public ResponseEntity<TenantBranchResponse> deactivateBranch(
            @NonNull @RequestHeader(HEADER_HOTEL) final UUID hotelId,
            @NonNull @PathVariable final UUID branchId,
            @NonNull final Authentication auth) {
        capabilityService.requireCapability(auth.getName(), hotelId, Capability.BRANCHES_MANAGE);
        return ResponseEntity.ok(
                branchAccessService.deactivateBranch(hotelId, branchId, auth.getName()));
    }

    /**
     * Returns a single branch, validating it belongs to the authenticated tenant.
'@

    Replace-ExactOnce `
        -Path 'auth-service/src/main/java/com/hotelpms/auth/controller/BranchController.java' `
        -Old $ControllerLifecycleAnchor `
        -New $ControllerLifecycleReplacement `
        -AlreadyAppliedMarker '@PatchMapping("/{branchId}/deactivate")'

    # ============================================================
    # 7. TESTS — SERVICE
    # ============================================================
    $LifecycleServiceTest = @'
package com.hotelpms.auth.service;

import com.hotelpms.auth.domain.TenantBranch;
import com.hotelpms.auth.dto.TenantBranchResponse;
import com.hotelpms.auth.exception.DuplicateResourceException;
import com.hotelpms.auth.exception.NotFoundException;
import com.hotelpms.auth.repository.TenantBranchRepository;
import com.hotelpms.auth.repository.UserAccountRepository;
import com.hotelpms.auth.repository.UserBranchMembershipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class BranchLifecycleServiceTest {

    private static final UUID HOTEL_A =
            UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID HOTEL_B =
            UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID BRANCH_ID =
            UUID.fromString("00000000-0000-0000-0000-00000000000a");
    private static final UUID INACTIVE_BRANCH_ID =
            UUID.fromString("00000000-0000-0000-0000-00000000000b");
    private static final String MAIN = "Main";
    private static final String NORTH = "North";
    private static final String ACTOR = "owner1";

    @Mock
    private TenantBranchRepository branchRepository;

    @Mock
    private UserBranchMembershipRepository membershipRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private BranchAccessServiceImpl service;

    private TenantBranch branch;

    @BeforeEach
    void setUp() {
        branch = TenantBranch.builder()
                .id(BRANCH_ID)
                .hotelId(HOTEL_A)
                .name(MAIN)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void updateBranchRenamesOnlyInsideTenant() {
        when(branchRepository.findByIdAndHotelId(BRANCH_ID, HOTEL_A))
                .thenReturn(Optional.of(branch));
        when(branchRepository.existsByHotelIdAndNameIgnoreCase(HOTEL_A, NORTH))
                .thenReturn(false);
        when(branchRepository.save(any(TenantBranch.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        final TenantBranchResponse result =
                service.updateBranch(HOTEL_A, BRANCH_ID, " North ", ACTOR);

        assertThat(result.name()).isEqualTo(NORTH);
        assertThat(result.hotelId()).isEqualTo(HOTEL_A);
        assertThat(result.active()).isTrue();
        verify(branchRepository).save(branch);
    }

    @Test
    void updateBranchRejectsForeignTenantBranch() {
        when(branchRepository.findByIdAndHotelId(BRANCH_ID, HOTEL_B))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.updateBranch(HOTEL_B, BRANCH_ID, NORTH, ACTOR));

        verify(branchRepository, never()).save(any(TenantBranch.class));
    }

    @Test
    void updateBranchRejectsDuplicateNameInTenant() {
        when(branchRepository.findByIdAndHotelId(BRANCH_ID, HOTEL_A))
                .thenReturn(Optional.of(branch));
        when(branchRepository.existsByHotelIdAndNameIgnoreCase(HOTEL_A, NORTH))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> service.updateBranch(HOTEL_A, BRANCH_ID, NORTH, ACTOR));

        verify(branchRepository, never()).save(any(TenantBranch.class));
    }

    @Test
    void deactivateBranchPreservesRowAndMarksInactive() {
        when(branchRepository.findByIdAndHotelId(BRANCH_ID, HOTEL_A))
                .thenReturn(Optional.of(branch));
        when(branchRepository.save(any(TenantBranch.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        final TenantBranchResponse result =
                service.deactivateBranch(HOTEL_A, BRANCH_ID, ACTOR);

        assertThat(result.active()).isFalse();
        assertThat(branch.isActive()).isFalse();
        verify(branchRepository).save(branch);
        verify(branchRepository, never()).delete(any(TenantBranch.class));
    }

    @Test
    void listBranchesCanIncludeInactiveHistory() {
        final TenantBranch inactive = TenantBranch.builder()
                .id(INACTIVE_BRANCH_ID)
                .hotelId(HOTEL_A)
                .name(NORTH)
                .active(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(branchRepository.findAllByHotelIdOrderByNameAsc(HOTEL_A))
                .thenReturn(List.of(branch, inactive));

        final List<TenantBranchResponse> result =
                service.listBranches(HOTEL_A, true);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(TenantBranchResponse::active)
                .containsExactly(true, false);
    }
}
'@

    Ensure-FileExact `
        -Path 'auth-service/src/test/java/com/hotelpms/auth/service/BranchLifecycleServiceTest.java' `
        -Content $LifecycleServiceTest

    # ============================================================
    # 8. TESTS — CONTROLLER
    # ============================================================
    $LifecycleControllerTest = @'
package com.hotelpms.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hotelpms.auth.dto.TenantBranchResponse;
import com.hotelpms.auth.dto.UpdateBranchRequest;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class BranchLifecycleControllerTest {

    private static final String BASE_URL = "/api/v1/auth/branches";
    private static final String HEADER_HOTEL = "X-Auth-Hotel";
    private static final String USERNAME = "owner1";
    private static final String NORTH = "North";
    private static final UUID HOTEL_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID BRANCH_ID =
            UUID.fromString("00000000-0000-0000-0000-00000000000a");

    @Mock
    private BranchAccessService branchAccessService;

    @Mock
    private CapabilityService capabilityService;

    @InjectMocks
    private BranchController controller;

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

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void updateBranchRequiresManageCapabilityAndReturnsUpdatedBranch() throws Exception {
        final TenantBranchResponse response = new TenantBranchResponse(
                BRANCH_ID,
                HOTEL_ID,
                NORTH,
                true,
                LocalDateTime.now());

        when(branchAccessService.updateBranch(
                eq(HOTEL_ID),
                eq(BRANCH_ID),
                eq(NORTH),
                eq(USERNAME)))
                .thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/{branchId}", BRANCH_ID)
                        .header(HEADER_HOTEL, HOTEL_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateBranchRequest(NORTH)))
                        .with(request -> {
                            request.setUserPrincipal(auth);
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(NORTH))
                .andExpect(jsonPath("$.active").value(true));

        verify(capabilityService)
                .requireCapability(
                        USERNAME,
                        HOTEL_ID,
                        Capability.BRANCHES_MANAGE);
    }

    @Test
    void deactivateBranchUsesSoftDeactivationEndpoint() throws Exception {
        final TenantBranchResponse response = new TenantBranchResponse(
                BRANCH_ID,
                HOTEL_ID,
                NORTH,
                false,
                LocalDateTime.now());

        when(branchAccessService.deactivateBranch(
                HOTEL_ID,
                BRANCH_ID,
                USERNAME))
                .thenReturn(response);

        mockMvc.perform(patch(
                        BASE_URL + "/{branchId}/deactivate",
                        BRANCH_ID)
                        .header(HEADER_HOTEL, HOTEL_ID.toString())
                        .with(request -> {
                            request.setUserPrincipal(auth);
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        verify(capabilityService)
                .requireCapability(
                        USERNAME,
                        HOTEL_ID,
                        Capability.BRANCHES_MANAGE);
    }

    @Test
    void listBranchesCanRequestInactiveHistory() throws Exception {
        final TenantBranchResponse response = new TenantBranchResponse(
                BRANCH_ID,
                HOTEL_ID,
                NORTH,
                false,
                LocalDateTime.now());

        when(branchAccessService.listBranches(HOTEL_ID, true))
                .thenReturn(List.of(response));

        mockMvc.perform(get(BASE_URL)
                        .param("includeInactive", "true")
                        .header(HEADER_HOTEL, HOTEL_ID.toString())
                        .with(request -> {
                            request.setUserPrincipal(auth);
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].active").value(false));

        verify(capabilityService)
                .requireCapability(
                        USERNAME,
                        HOTEL_ID,
                        Capability.BRANCHES_READ);
    }
}
'@

    Ensure-FileExact `
        -Path 'auth-service/src/test/java/com/hotelpms/auth/controller/BranchLifecycleControllerTest.java' `
        -Content $LifecycleControllerTest

    # ============================================================
    # 9. VERIFICACIONES ESTRUCTURALES
    # ============================================================
    Write-Host ''
    Write-Host '=== VALIDACIÓN ESTRUCTURAL ==='

    $ForbiddenMigrationChanges = git diff --name-only -- `
        'auth-service/src/main/resources/db/migration'

    if ($LASTEXITCODE -ne 0) {
        throw 'No fue posible validar migraciones.'
    }

    if ($ForbiddenMigrationChanges) {
        throw @"
A02 no requiere una nueva migración y no debe modificar migraciones históricas.
Se detectaron cambios:
$ForbiddenMigrationChanges
"@
    }

    $RequiredMarkers = @(
        @{
            File = 'auth-service/src/main/java/com/hotelpms/auth/controller/BranchController.java'
            Marker = '@PatchMapping("/{branchId}/deactivate")'
        },
        @{
            File = 'auth-service/src/main/java/com/hotelpms/auth/service/BranchAccessServiceImpl.java'
            Marker = 'public TenantBranchResponse updateBranch'
        },
        @{
            File = 'auth-service/src/main/java/com/hotelpms/auth/service/BranchAccessServiceImpl.java'
            Marker = 'public TenantBranchResponse deactivateBranch'
        },
        @{
            File = 'auth-service/src/main/java/com/hotelpms/auth/repository/TenantBranchRepository.java'
            Marker = 'findAllByHotelIdOrderByNameAsc'
        }
    )

    foreach ($Check in $RequiredMarkers) {
        $Text = Get-TextLf $Check.File

        if (-not $Text.Contains($Check.Marker)) {
            throw "Validación falló: '$($Check.Marker)' no existe en $($Check.File)"
        }
    }

    # ============================================================
    # 10. BUILD / TESTS DEL MÓDULO
    # ============================================================
    Write-Host ''
    Write-Host '=== AUTH-SERVICE TEST ==='

    & .\gradlew.bat `
        :auth-service:test `
        --no-daemon `
        --console=plain

    if ($LASTEXITCODE -ne 0) {
        throw "Falló :auth-service:test con exit code $LASTEXITCODE"
    }

    Write-Host ''
    Write-Host '=== AUTH-SERVICE QUALITY GATES ==='

    & .\gradlew.bat `
        :auth-service:checkstyleMain `
        :auth-service:checkstyleTest `
        :auth-service:pmdMain `
        :auth-service:pmdTest `
        --no-daemon `
        --console=plain

    if ($LASTEXITCODE -ne 0) {
        throw "Fallaron quality gates de auth-service con exit code $LASTEXITCODE"
    }

    Write-Host ''
    Write-Host '=== GIT DIFF CHECK ==='

    git diff --check

    if ($LASTEXITCODE -ne 0) {
        throw 'git diff --check falló.'
    }

    # ============================================================
    # 11. A02 SOLO SE MARCA DONE DESPUÉS DE VALIDAR
    # ============================================================
    Update-ProgressRow `
        -Id 'A02' `
        -Status '[x]' `
        -Files 'UpdateBranchRequest; TenantBranchRepository; BranchAccessService; BranchAccessServiceImpl; BranchController; BranchLifecycleServiceTest; BranchLifecycleControllerTest; no migration required' `
        -Tests ':auth-service:test; checkstyleMain; checkstyleTest; pmdMain; pmdTest; git diff --check' `
        -Notes 'Create/read/edit/soft-deactivate complete. Inactive branches remain queryable with includeInactive=true; tenant scoping and BRANCHES_MANAGE enforcement preserved.'

    $ExecutionLine = "- $(Get-Date -Format 'yyyy-MM-ddTHH:mm:ssK') — A02 validated: branch rename + soft deactivation + inactive history query."

    [System.IO.File]::AppendAllText(
        $ProgressFile,
        $ExecutionLine + "`n",
        $Utf8NoBom
    )

    Write-Host ''
    Write-Host '=== DIFF FINAL ==='
    git diff --stat
    git status --short

    Write-Host ''
    Write-Host '============================================================'
    Write-Host 'FIXI_WORKPACK_RESULT'
    Write-Host 'RESULTADO: VALIDADO'
    Write-Host "COMPLETADO: $Workpack — $WorkpackName"
    Write-Host 'MIGRATIONS: ninguna nueva; V10 preservada'
    Write-Host 'TESTS: auth-service test + Checkstyle + PMD + git diff --check'
    Write-Host 'SIGUIENTE CANDIDATO: A03 — EMPLEADOS Y MEMBRESÍAS DE SUCURSAL'
    Write-Host '============================================================'
}
catch {
    $Failure = $_.Exception.Message

    try {
        Update-ProgressRow `
            -Id 'A02' `
            -Status '[~]' `
            -Files 'A02 changes prepared; inspect git diff and checkpoint before repair' `
            -Tests 'last execution failed before complete validation' `
            -Notes "REQUIERE REPARACIÓN: $Failure ; checkpoint: $BackupDir"

        $FailureLine = "- $(Get-Date -Format 'yyyy-MM-ddTHH:mm:ssK') — A02 execution failed; repair required: $(Sanitize-ProgressCell $Failure)"
        [System.IO.File]::AppendAllText(
            $ProgressFile,
            $FailureLine + "`n",
            $Utf8NoBom
        )
    }
    catch {
        Write-Warning "No fue posible actualizar el progress file tras el fallo: $($_.Exception.Message)"
    }

    Write-Host ''
    Write-Host '============================================================'
    Write-Host 'FIXI_WORKPACK_RESULT'
    Write-Host 'RESULTADO: REQUIERE REPARACIÓN'
    Write-Host "WORKPACK: $Workpack — $WorkpackName"
    Write-Host "ERROR: $Failure"
    Write-Host "CHECKPOINT: $BackupDir"
    Write-Host 'Los cambios NO se borraron automáticamente para permitir diagnóstico.'
    Write-Host '============================================================'

    exit 1
}
