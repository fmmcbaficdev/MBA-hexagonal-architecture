# Full Cycle — Hexagonal Architecture

Fork do [MBA-hexagonal-architecture](https://github.com/devfullcycle/MBA-hexagonal-architecture), a partir da branch `clean-arch`, com a feature de **cancelamento de evento**.

## Como subir

Java 17+ e Docker.

```bash
docker compose up -d
./gradlew :infrastructure:bootRun
```

A API sobe em `http://localhost:8080`. GraphiQL em `http://localhost:8080/graphiql`.

## Como testar

```bash
./gradlew test
```

No Windows, se o Gradle reclamar do JDK 25:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH
.\gradlew.bat test
```

## Onde a cascata de cancelamento acontece

`CancelEventUseCase` chama `Event.cancel()`, que registra o evento de domínio `EventCancelled` (`type = event.cancelled`). Ao persistir o agregado, `EventDatabaseRepository` grava esse fato na tabela `outbox`. O `OutboxRelay` publica o JSON na `QueueGateway`. O `ConsumerQueueGateway` reconhece `event.cancelled` e dispara `CancelEventTicketsUseCase`, que busca os ingressos por `ticketsByEventId` e chama `Ticket.cancel()`. O agregado de Evento não toca o de Ingresso.

## Contratos

- `POST /events/{id}/cancel` — cancela o evento (422 se inexistente ou já cancelado)
- `GET /events/{id}` — representação completa
- `GET /events/{id}` + header `X-Public: true` — `{id, status}`
- GraphQL: `cancelEvent(id)` e `eventOfId(id)`
