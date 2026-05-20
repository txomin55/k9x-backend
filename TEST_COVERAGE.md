# Test Coverage — Endpoints

Leyenda: ✅ test existe · ⚠️ implementado, sin tests · ❌ sin implementación

---

## Competitions (secured)

| Endpoint | Service Case | jOOQ Adapter Test |
|---|---|---|
| `CreateCompetition` | ✅ | ✅ |
| `FetchCompetitions` | ✅ | ✅ |
| `UpdateCompetition` | ✅ | ✅ |
| `RemoveCompetition` | ✅ | ✅ |

---

## Dogs (secured)

| Endpoint | Service Case | jOOQ Adapter Test |
|---|---|---|
| `CreateDog` | ✅ | ✅ |
| `GetDogList` | ✅ | ✅ |
| `UpdateDog` | ✅ | ✅ |
| `RemoveDog` | ✅ | ✅ |

---

## Judges (secured)

| Endpoint | Service Case | jOOQ Adapter Test |
|---|---|---|
| `CreateJudge` | ✅ | ✅ |
| `FetchJudges` | ✅ | ✅ |
| `UpdateJudge` | ✅ | ✅ |
| `RemoveJudge` | ✅ | ✅ |

---

## Stages (secured)

| Endpoint | Service Case | jOOQ Adapter Test |
|---|---|---|
| `CreateStage` | ✅ | ✅ |
| `UpdateStage` | ✅ | ✅ |
| `RemoveStage` | ✅ | ✅ |

---

## Stages (public)

| Endpoint | Service Case | jOOQ Adapter Test |
|---|---|---|
| `GetStage` | ❌ | ✅ |
| `GetStages` | ❌ | ❌ |

---

## Events (secured)

| Endpoint | Service Case | jOOQ Adapter Test |
|---|---|---|
| `CreateEvent` | ✅ | ✅ |
| `RemoveEvent` | ✅ | ✅ |
| `EnrollEvent` | ❌ | ❌ |
| `FetchAllByStagesEventData` | ❌ | ❌ |
| `UpdateObdxEventInfo` | ❌ | ❌ |
| `UpdateObdxScore` | ❌ | ❌ |

---

## Events (public)

| Endpoint | Service Case | jOOQ Adapter Test |
|---|---|---|
| `GetEventClassification` | ❌ | ❌ |

---

## Collections (secured)

| Endpoint | Service Case | jOOQ Adapter Test |
|---|---|---|
| `GetCollection` | ❌ | ❌ |
| `GetCollections` | ❌ | ❌ |

---

## Disciplines (secured)

| Endpoint | Service Case | Adapter Test |
|---|---|---|
| `GetDisciplines` | ✅ | ✅ |

---

## User (secured)

| Endpoint | Service Case | jOOQ Adapter Test |
|---|---|---|
| `GetUserData` | ❌ | ✅ |
| `Logout` | ❌ | ❌ |
| `RegisterPush` | ❌ | ❌ |

---

## Users (public)

| Endpoint | Service Case | jOOQ Adapter Test |
|---|---|---|
| `Login` | ✅ | ❌ |

---

## Resumen

| Módulo | Service Cases | jOOQ Adapters |
|---|---|---|
| Competitions | 4 / 4 | 4 / 4 |
| Dogs | 4 / 4 | 4 / 4 |
| Judges | 4 / 4 | 4 / 4 |
| Stages (secured) | 3 / 3 | 3 / 3 |
| Stages (public) | 0 / 2 | 1 / 2 |
| Events (secured) | 2 / 6 | 2 / 6 |
| Events (public) | 0 / 1 | 0 / 1 |
| Collections | 0 / 2 | 0 / 2 |
| Disciplines | 1 / 1 | 1 / 1 |
| User (secured) | 0 / 3 | 1 / 3 |
| Users (public) | 1 / 1 | 0 / 1 |
| **Total** | **19 / 31** | **20 / 31** |
