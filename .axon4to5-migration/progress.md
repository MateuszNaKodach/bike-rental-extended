# Axon Framework 4 → 5 Migration — Progress

> Single source of truth. A fresh session reads this alone and resumes with zero clarifying questions.
> **Protocol:** rewrite the relevant section, THEN commit. Never split work and bookkeeping.

## ▶︎ RESUME HERE

- **next:** DONE — all recipes applied, build green, tests green, scaffolding removed.
- **recipe-loop:** COMPLETE (8/8)
- **tree:** clean

---

## Selection arguments (frozen frame)

```
framework=axoniq  configuration=spring  mode=project  execution=inline
```

---

## OpenRewrite

```
status: success
ts: 2026-05-15T01:47:31+02:00
note: BUILD SUCCESS — axoniq recipe applied, ~9h estimated time saved
```

---

## Recipe status

| # | Recipe | Status | Items done/total | Notes |
|---|--------|--------|------------------|-------|
| 1 | aggregate | done | 2/2 | Bike, Payment |
| 2 | event-processor | done | 4/4 | RentalApplication×2 processors, PaymentApplication, RentalQueryApplication (microservice), RentalPaymentSagaApplication (microservice) |
| 3 | command-gateway | done | 0/0 | No CommandGateway field injection found — N/A |
| 4 | query-gateway | done | 2/2 | BikeStatusProjection, PaymentStatusProjection |
| 5 | query-handler | done | 2/2 | RentalController, PaymentController |
| 6 | interceptors | done | 0/0 | None found |
| 7 | saga | done | 1/1 | PaymentSaga → @Component + PaymentStateRepository + @DisallowReplay |
| 8 | event-store | done | 2/2 | RentalApplication, PaymentApplication |

Legend: `pending` · `in-progress` · `done` · `partially-blocked` · `skipped`

---

## Final compile

```
status: ✅ green
ts: 2026-05-15
command: ./mvnw compile
errors: 0
```

## Final tests

```
status: ✅ green
ts: 2026-05-15
command: ./mvnw test
results: 16 passed, 0 failed, 0 errors
  - BikeTest: 11 passed
  - PaymentSagaTest: 5 passed
```

## Scaffolding cleanup

- Removed `isolated-Bike` profile from `rental/pom.xml`
- No other `isolated-*` profiles found

---

## Queue

| # | recipe | source | status | notes |
|---|--------|--------|--------|-------|
| 1 | aggregate | io.axoniq.demo.bikerental.rental.command.Bike | ✅ done | isolated-Bike scaffolding removed |
| 2 | aggregate | io.axoniq.demo.bikerental.payment.Payment | ✅ done | |
| 3 | query-gateway | io.axoniq.demo.bikerental.rental.query.BikeStatusProjection | ✅ done | QueryUpdateEmitter.emit type-based |
| 4 | query-gateway | io.axoniq.demo.bikerental.payment.PaymentStatusProjection | ✅ done | |
| 5 | query-handler | io.axoniq.demo.bikerental.rental.ui.RentalController | ✅ done | subscriptionQuery → Publisher<R> |
| 6 | query-handler | io.axoniq.demo.bikerental.payment.PaymentController | ✅ done | |
| 7 | saga | io.axoniq.demo.bikerental.rental.paymentsaga.PaymentSaga | ✅ done | @Component + JPA state; DeadlineManager commented out |
| 8 | event-store | io.axoniq.demo.bikerental.rental.RentalApplication | ✅ done | AggregateBasedAxonServerEventStorageEngine |
| 9 | event-store | io.axoniq.demo.bikerental.payment.PaymentApplication | ✅ done | |
| 10 | event-processor | microservices/rental-query/RentalQueryApplication | ✅ done | |
| 11 | event-processor | microservices/rental-payment/RentalPaymentSagaApplication | ✅ done | |

---

## Caller decisions log

- User: "do not migrate the snapshot, revert this change. let it commented out"
  → BikeSnapshotDefinition.java and snapshot policy in BikeConfiguration.java remain commented out, not deleted.
