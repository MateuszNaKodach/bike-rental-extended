# Axon Framework 4 → 5 Migration — Progress

> Single source of truth. A fresh session reads this alone and resumes with zero clarifying questions.
> **Protocol:** rewrite the relevant section, THEN commit. Never split work and bookkeeping.

## ▶︎ RESUME HERE

- **next:** Discover aggregate candidates (recipe 1/8).
- **recipe-loop:** aggregate (1/8)
- **recipe:** aggregate
- **source:** —
- **verify:** —
- **tree:** clean
- **awaiting-caller:** no

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

| # | Recipe | Status | Items done/total | Last commit |
|---|--------|--------|------------------|-------------|
| 1 | aggregate | pending | 0/? | — |
| 2 | event-processor | pending | 0/? | — |
| 3 | command-gateway | pending | 0/? | — |
| 4 | query-gateway | pending | 0/? | — |
| 5 | query-handler | pending | 0/? | — |
| 6 | interceptors | pending | 0/? | — |
| 7 | saga | pending | 0/? | — |
| 8 | event-store | pending | 0/? | — |

Legend: `pending` · `in-progress` · `done` · `partially-blocked` · `skipped`

---

## Queue

| # | recipe | source | status | last-commit | notes |
|---|--------|--------|--------|-------------|-------|

---

## Caller decisions log

_none yet_
