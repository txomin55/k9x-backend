# Conventions & Architecture Guide

## Hexagonal Architecture — Layer Overview

```
domain  ←  application  ←  infrastructure  ←  loader
```

| Module | Gradle artifact | Responsibility |
|---|---|---|
| `k9x-backend-domain` | domain | Aggregates, domain exceptions, enums |
| `k9x-backend-application` | application | Ports, service cases, commands, DTOs, payloads |
| `k9x-backend-infrastructure` | infrastructure | jOOQ adapters, REST endpoints, Spring configuration stubs |
| `k9x-backend-loader` | loader | Spring Boot entry point, `@Bean` wiring |

Dependencies only flow inward. Infrastructure depends on application; application depends on domain.

---

## Domain Layer (`k9x-backend-domain`)

### Aggregates

Each aggregate is a `record` under `com.k9x.domain.aggregates.<entity>`:

```java
public record Competition(String id, String name, String creator,
                          Long createdAt, Long lastUpdate, Long deletedAt) {}
```

Soft deletes: `deletedAt` is `Long`, `null` means the record is active.

### Enums

Domain enums live in the aggregate package:

```java
// com.k9x.domain.aggregates.competitions.CompetitionStatus
public enum CompetitionStatus { ACTIVE }
```

Status fields that are **computed** (not stored in DB) are resolved in the service case layer, not in the adapter.

### Exceptions

```
com.k9x.domain.exceptions.UnauthorizedResourceException
```

---

## Application Layer (`k9x-backend-application`)

### Package Structure (per entity)

```
com.k9x.application.<entity>/
  exceptions/         — XxxNotFoundException, XxxAlreadyDeletedException
  port/
    GetXxxPersistencePort.java
    CreateXxxPersistencePort.java
    UpdateXxxPersistencePort.java
    DeleteXxxPersistencePort.java
    payload/
      UpdateXxxPersistencePayload.java   ← persistence write payload
  use_case/
    GetXxxServiceCase.java
    CreateXxxServiceCase.java
    UpdateXxxServiceCase.java
    DeleteXxxServiceCase.java
    command/
      UpdateXxxCommand.java              ← inbound mutation command
    dto/
      XxxDTO.java                        ← read model / return type
```

> **Rule**: commands go in `use_case.command`, DTOs go in `use_case.dto`, persistence payloads go in `port.payload`. Never put these directly in the `use_case` or `port` package root.

### Ports

Ports are plain Java interfaces. Update ports accept a payload object, never individual fields:

```java
public interface UpdateCompetitionPersistencePort {
    void updateCompetition(String id, UpdateCompetitionPersistencePayload payload);
}
```

### Persistence Payloads (`port.payload`)

Each payload is a `record` with a `static from(...)` factory that takes the command (and extra dependencies like geo coordinates) and stamps `DateUtils.nowUtcMillis()` as `lastUpdate`. The service case never constructs the payload inline — it always calls the factory:

```java
// port/payload/UpdateCompetitionPersistencePayload.java
public record UpdateCompetitionPersistencePayload(String name, String description,
        String country, String address, Double coordAlt, Double coordLong, long lastUpdate) {

    public static UpdateCompetitionPersistencePayload from(UpdateCompetitionCommand command, Coordinates coordinates) {
        return new UpdateCompetitionPersistencePayload(
                command.name(), command.description(), command.country(), command.address(),
                coordinates.coordAlt(), coordinates.coordLong(), DateUtils.nowUtcMillis());
    }
}
```

Service case call site:
```java
updateCompetitionPersistencePort.updateCompetition(id, UpdateCompetitionPersistencePayload.from(command, coordinates));
```

### Service Cases

Validation order:
1. `assertOrganizer(organizer)` — always first when the operation is organizer-only
2. Fetch the aggregate from the persistence port
3. `assertXxxValidations(entity, userId)` — null check → deleted check → creator check
4. (if needed) Fetch related aggregate and assert it too
5. Call the port with `XxxPersistencePayload.from(...)`

```java
public void updateCompetition(String id, UpdateCompetitionCommand command, String userId, boolean organizer) {
    assertOrganizer(organizer);
    Competition competition = getCompetitionPersistencePort.getCompetition(id);
    assertCompetitionValidations(competition, userId);
    Coordinates coordinates = geoCoordinatesPort.getCoordinates(command.address());
    updateCompetitionPersistencePort.updateCompetition(id, UpdateCompetitionPersistencePayload.from(command, coordinates));
}
```

### Read DTOs (`use_case.dto`)

Returned by `GetXxx` service cases. Fields that are computed (e.g. `status`) are set in the service case, not by the adapter:

```java
// GetCompetitionListServiceCase maps each competition and sets status = CompetitionStatus.ACTIVE.name()
```

The persistence port for list reads returns a `List<FetchXxxDTO>` with the computed field as `null`; the service case fills it in.

---

## Infrastructure Layer (`k9x-backend-infrastructure`)

### jOOQ Adapters

One adapter class per port, named `XxxJooqAdapter`. Located under `com.k9x.infrastructure.out.persistence.<entity>`.

- Use `DSLContext` injected via constructor.
- Soft-delete filter: `WHERE deleted_at IS NULL`.
- For list queries with a LEFT JOIN (e.g. competitions + stages): filter the main table in `WHERE`, the joined table's `deleted_at` in the `ON` clause (not `WHERE`) to avoid dropping parent rows that have no active children.
- Deduplication pattern for JOIN results:

```java
LinkedHashMap<String, FetchCompetitionDTO> map = new LinkedHashMap<>();
result.forEach(r -> {
    map.putIfAbsent(r.get(COMPETITIONS.ID), new FetchCompetitionDTO(..., new ArrayList<>(), null));
    if (r.get(STAGES.ID) != null) {
        map.get(r.get(COMPETITIONS.ID)).stages().add(new FetchStageDTO(...));
    }
});
```

- `NOT NULL` columns that have no corresponding input field (e.g. `country` on create) are hardcoded at the adapter level (e.g. `""`), not propagated through the whole stack.

### REST Endpoints

One class per OAS delegate, located under `com.k9x.infrastructure.in.rest.endpoints.secured.<entity>`.

Endpoints receive `UserInfoDTO` (injected via constructor from the loader bean) and delegate entirely to the service case:

```java
public class UpdateCompetition implements SecuredCompetitionsUpdateApiDelegate {
    private final UpdateCompetitionServiceCase updateCompetitionServiceCase;
    private final UserInfoDTO userDetails;

    @Override
    public ResponseEntity<Void> updateCompetitionSecured(String id, UpdateCompetitionBodyDTO body) {
        updateCompetitionServiceCase.updateCompetition(
                id,
                new UpdateCompetitionCommand(body.getName(), body.getDescription(), body.getCountry(), body.getAddress()),
                userDetails.getEmail(), userDetails.isOrganizer());
        return ResponseEntity.ok().build();
    }
}
```

---

## Loader (`k9x-backend-loader`)

All Spring `@Bean` declarations live here. One `@Configuration` class per concern:

```
com.k9x.configuration.secured.<entity>/
  SecuredXxxEndpointConfiguration.java   ← wires endpoint classes
  XxxUseCaseConfiguration.java           ← wires service cases
com.k9x.configuration.persistence.<entity>/
  XxxJooqAdapterConfiguration.java       ← wires jOOQ adapters
```

Endpoint beans receive their service case and `UserInfoDTO` beans as constructor arguments.

---

## Database Schema

- Flyway script: `k9x-backend-infrastructure/src/main/resources/db/schema/V1__create_mvp_db.sql`
- Soft deletes: every table has `deleted_at BIGINT` (nullable — `NULL` = active).
- Timestamps (`created_at`, `last_update`, `deleted_at`) are stored as epoch milliseconds (`BIGINT`).
- jOOQ classes are generated from the DDL at build time.
- **Important**: the jOOQ generated classes may contain columns (e.g. `created_at`) that were removed from the DDL in a later migration. Always check the DDL before setting fields in adapters — never trust the generated class alone.

---

## Testing Conventions

### Application unit tests (Mockito)

```java
@ExtendWith(MockitoExtension.class)
class UpdateXxxServiceCaseTest {
    @Mock XxxPersistencePort port;
    private UpdateXxxServiceCase serviceCase;

    @BeforeEach void setUp() { serviceCase = new UpdateXxxServiceCase(port); }

    @Test void throws_exception_when_user_is_not_organizer() { ... }
    @Test void throws_exception_when_xxx_not_found() { ... }
    @Test void throws_exception_when_xxx_is_deleted() { ... }
    @Test void throws_exception_when_user_is_not_creator() { ... }
    @Test void updates_xxx_when_all_validations_pass() {
        // verify(port).updateXxx(eq("id"), any());
    }
}
```

Use `any()` for the payload argument in the happy-path `verify` — field-level assertions go in a separate test with `ArgumentCaptor` if needed.

After implementing a new endpoint (service case, adapter, or tests), update `TEST_COVERAGE.md` at the repo root — mark the endpoint row and adjust the summary totals.

### Infrastructure unit tests (jOOQ MockDataProvider)

Use `MockConnection` / `MockDataProvider` — no real database, no Spring context. One test class per adapter.

---

## Utilities

- `DateUtils.nowUtcMillis()` — always use this for timestamps, never `System.currentTimeMillis()` directly.
- `UserInfoDTO` — carries `getEmail()` and `isOrganizer()` for the authenticated user; injected into every secured endpoint.
