# Axon Framework 4 → 5 Migration — Learnings

Append-only. One dated entry per surprise, manual fix, or non-obvious decision.
Read on demand — `progress.md` is the source of truth for state.

```
## YYYY-MM-DD — <one-line headline>
**Context:** where in the migration this came up.
**Surprise:** what was unexpected.
**Resolution:** what was done. Link to commit `<sha>` if applicable.
```

---

## 2026-05-15 — Axon Server connector lives under `io.axoniq.framework`, not `org.axonframework`

**Context:** event-store recipe — adding `AggregateBasedAxonServerEventStorageEngine` bean to `RentalApplication` and `PaymentApplication`.

**Surprise:** The first import attempt used `org.axonframework.axonserver.connector.api.AxonServerConnectionManager` — which no longer exists in AF5. The connector artifact (`io.axoniq.framework:axon-server-connector`) moved its packages to the `io.axoniq.framework.*` namespace. Compile failed with "package does not exist".

**Resolution:** Correct imports are:
- `io.axoniq.framework.axonserver.connector.api.AxonServerConnectionManager`
- `io.axoniq.framework.axonserver.connector.event.AggregateBasedAxonServerEventStorageEngine`

Applies to every project that uses Axon Server as the event store. Grep for `org.axonframework.axonserver` in post-OpenRewrite sources — those imports will NOT have been fixed by the recipe.

---

## 2026-05-15 — New classes in a sibling Maven module require `mvn install` before downstream compile sees them

**Context:** query-gateway recipe — new `@Query`-annotated Kotlin records created in `core-api/src/main/java` (queries.kt files).

**Surprise:** After creating the new Kotlin source files in `core-api`, `./mvnw compile` from the root failed with "cannot find symbol" for those classes in downstream modules (`rental`, `payment`). Root-level `compile` does not install sibling module artifacts to the local repo — it recompiles modules but does not update `~/.m2`.

**Resolution:** Run `./mvnw -f core-api/pom.xml install -q` after adding sources to a sibling module that other modules depend on via `<dependency>`. Re-run root `compile` after that succeeds.

---

## 2026-05-15 — `subscriptionQuery` in AF5 returns `Publisher<R>` (unified stream), not `SubscriptionQueryResponse<I,U>`

**Context:** query-gateway recipe — migrating `RentalController` subscription query calls.

**Surprise:** The query-gateway RECIPE.md (Step 4) documents `SubscriptionQueryResponse<R, U>` with `initialResult()` and `updates()` accessors. The actual AF5 `QueryGateway` in this project's dependency returns a `Publisher<R>` from `subscriptionQuery(payload, R.class)` — a single unified stream of initial result(s) followed by updates. There is no `SubscriptionQueryResponse<R,U>` class. `javap` on the actual jar confirmed the signature.

**Resolution:** Use `Flux.from(queryGateway.subscriptionQuery(payload, R.class))` directly. Apply `.next()` to get only the first item (initial result). The recipe's Step 4 documentation targets a different AF5 snapshot/version; verify with `javap` on the actual jar when encountering subscription queries. The documented `SubscriptionQueryResponse` API may apply to a different version or a different build of the connector.

---

## 2026-05-15 — `initialToken(StreamableMessageSource::createHeadToken)` does not exist on `EventProcessorDefinition` fluent builder

**Context:** event-processor recipe — migrating `ConfigurationEnhancer` beans to `EventProcessorDefinition.pooledStreaming(...)` in multiple Application classes.

**Surprise:** AF4 code called `.initialToken(StreamableMessageSource::createHeadToken)` in the builder to start from the head of the stream (skip history). The `EventProcessorDefinition` fluent builder in AF5 has no `initialToken(...)` method. Compile error.

**Resolution:** Dropped the call and annotated with `// TODO(af5): initialToken(...) removed — processor will replay from beginning on first start`. The processor will do a full replay on first run. If head-token semantics are needed, the equivalent in AF5 must be set via `TrackingTokenSource.latestToken()` but that requires a `ProcessingContext` — not straightforward to wire at startup time without additional support.

---

## 2026-05-15 — `@EntityScan(basePackageClasses = {SagaEntry.class})` fails to compile after saga removal

**Context:** saga recipe — updating `@EntityScan` in `RentalApplication` (and microservices `RentalPaymentSagaApplication`).

**Surprise:** After removing the `@Saga` infrastructure, `SagaEntry` no longer exists in AF5's classpath — `org.axonframework.modelling.saga.repository.jpa.SagaEntry` was removed with the Saga SPI. Any `@EntityScan(basePackageClasses = {..., SagaEntry.class})` causes a compile error.

**Resolution:** Replace `SagaEntry.class` with the new JPA state entity class (`PaymentState.class`). For microservices modules that don't directly depend on the module containing `PaymentState`, use `basePackages = "io.axoniq..."` (string-based) instead of a class reference to avoid cross-module compile dependency.

---

## 2026-05-15 — `AxonTestFixture(TargetClass.class)` constructor does not exist in AF5 for non-aggregate test subjects

**Context:** saga recipe — existing `PaymentSagaTest` used `new AxonTestFixture(PaymentSaga.class)`.

**Surprise:** AF5's `AxonTestFixture` is designed for aggregate testing only. The constructor `AxonTestFixture(Class<T>)` applied to a non-aggregate saga class doesn't exist. The compile fails: "cannot find symbol — constructor AxonTestFixture(Class<PaymentSaga>)".

**Resolution:** Rewrite saga tests as plain unit tests using Mockito. Mock `PaymentStateRepository` and `CommandDispatcher`, construct the `PaymentSaga` directly, invoke handlers directly, and verify with `ArgumentCaptor` / `verify(...)`. This is idiomatic for the AF5 saga-as-component pattern. No fixture DSL is available for `@Component @DisallowReplay` event handlers.

---

## 2026-05-15 — Microservices application classes are NOT touched by OpenRewrite — manual migration required

**Context:** Debugging loop — full compile after all recipes applied to main modules.

**Surprise:** After the main `rental/` and `payment/` modules compiled cleanly, the build still failed in `microservices/rental-query/` and `microservices/rental-payment/`. Their Application classes still contained AF4 patterns: `ConfigurationEnhancer`, `SagaEntry`, `DeadlineManager`, `ConfigurationScopeAwareProvider`. OpenRewrite did not process these files.

**Resolution:** Manually apply event-processor + saga recipe patterns to each microservices Application class. In this project: `RentalQueryApplication` needed `EventProcessorDefinition` bean; `RentalPaymentSagaApplication` needed `EventProcessorDefinition` beans, `AggregateBasedAxonServerEventStorageEngine` bean, `SagaEntry` → package-based `@EntityScan`, and removal of `DeadlineManager` + `ConfigurationEnhancer` beans.

**Pattern:** After main-module compile goes green, always run a full `./mvnw compile` (not just the main modules) to catch microservices application classes.

---

## 2026-05-15 — `QueryUpdateEmitter.emit(String.class, predicate, value)` → must use typed query class, not `String.class`

**Context:** query-handler recipe — migrating `BikeStatusProjection` and `PaymentStatusProjection`.

**Surprise:** AF4 code called `queryUpdateEmitter.emit(String.class, event.getId()::equals, value)` where `String.class` was the "query type" marker used for string-keyed named queries. In AF5, `emit` is typed to the query payload class (`emit(FindBikeByIdQuery.class, predicate, update)`) — the predicate receives the actual query payload object.

**Resolution:** Replace `String.class` with the new `@Query`-annotated payload record class (`FindBikeByIdQuery.class`, `GetPaymentIdQuery.class`, etc.). Update the predicate lambda to use the typed parameter (`q -> q.getBikeId().equals(bikeId)` instead of `id::equals`). This requires the payload classes to already exist before migrating the projection — query-gateway recipe (which creates the `@Query` records) must run before query-handler recipe.

---

## 2026-05-15 — `CommandDispatcher` (method param) vs `CommandGateway` (field) — `PaymentSaga` needs both roles

**Context:** saga recipe — `PaymentSaga` dispatches commands from `@EventHandler` methods AND had a `@Scheduled`/deadline retry path.

**Surprise:** The saga recipe toolbox says: use `CommandDispatcher` as a method parameter for in-handler dispatch; use a constructor-injected `CommandGateway` field for `@Scheduled` pollers (which are not event handlers). The two coexist in the same class and serve different call sites.

**Resolution:** `@EventHandler` methods declare `CommandDispatcher commandDispatcher` as a parameter (AF5 resolves it). The `@Scheduled` poller method (added later to replace deadline logic) must use a `CommandGateway` field injected via constructor. Both in the same class is correct and expected. The deadline-replacement code was left as a comment for this project since no `@Scheduled` was introduced.
