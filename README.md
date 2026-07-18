# cloud-itonami-isco-8122

Open Occupation Blueprint for **ISCO-08 8122**: Metal Finishing, Plating
and Coating Machine Operators.

This repository designs a forkable OSS business for a metal-finishing/
plating/coating plant scheduling and logistics coordination practice: a
plant scheduling and supply-coordination robot manages crew/task records
under a governor-gated actor, so a metal finishing, plating and coating
crew keeps its own operating records instead of renting a closed
workforce-management SaaS.

**Maturity: `:implemented`.** `src/platingcoord/` implements the
`PlatingCoordActor` as a `langgraph.graph/state-graph`
(`platingcoord.actor`) wired to a `Plating Plant Scheduling Coordination
Advisor` (`platingcoord.advisor`) and an independent `PlatingCoordGovernor`
(`platingcoord.governor`), following the itonami actor pattern
(ADR-2607121000): `:intake -> :advise -> :govern -> :decide -+-> :commit
(:ok? true) +-> :request-approval (:escalate? true, human-in-the-loop
interrupt) +-> :hold (:hard? true)`. HARD invariants (always hold, never
overridable): plater provenance, facility provenance, no-actuation
(`:effect` must be `:propose`), a closed op-allowlist (`:log-work-record`,
`:schedule-crew-operation`, `:flag-safety-concern`,
`:coordinate-supply-order` — nothing else may ever be proposed), and a
permanent, unconditional block on any proposal that would directly finalize
a plating-operation-execution decision (e.g. deciding to proceed with a
specific electroplating run) or a chemical-safety-clearance decision (e.g.
declaring a plating bath or plated batch safe for handling), or that would
override a plant safety officer's judgment. Always-escalate paths (human
sign-off regardless of confidence, mapping this repo's Trust Controls in
[`docs/business-model.md`](docs/business-model.md)): `:flag-safety-concern`
(always) and `:coordinate-supply-order` above the registered cost
threshold.

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot performs
the physical domain work**. Here a plant scheduling/logistics coordination
robot performs crew scheduling, production-run/inventory/progress-record
logging and plating-chemicals/raw-material supply-order coordination for a
metal finishing, plating and coating crew, under an actor that proposes
actions and an independent **Plating Plant Scheduling Coordination
Governor** that gates them. The governor never dispatches hardware itself,
never operates plating equipment on the plant floor, and never finalizes a
plating-operation-execution decision or a chemical-safety-clearance
decision, and never overrides a plant safety officer's judgment;
`:high`/`:safety-critical` actions (such as a flagged chemical-exposure/
ventilation/electrical-hazard/equipment-condition concern, or an
above-threshold supply order) require human sign-off. **This actor
coordinates PLANT SCHEDULING/LOGISTICS ONLY — it never operates plating
equipment itself, and it never makes a chemical-safety-clearance decision
itself.**

Metal Finishing, Plating and Coating Machine Operators run electroplating
lines using chemical baths (historically including cyanide-based and
chromium-based solutions), alongside electrical hazard from electroplating
current. This is a real chemical-exposure and electrical-hazard domain;
this actor never operates that equipment and never clears it as safe — it
only schedules and logs around it, and always routes chemical-exposure/
safety concerns to a human plant safety officer.

## Core Contract

```text
crew roster + facility registration + safety-reporting policy
        |
        v
Plating Plant Scheduling Coordination Advisor -> PlatingCoordGovernor -> log/schedule/coordinate, or human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated advice can dispatch a robot action the governor refuses,
finalize a plating-operation-execution decision, finalize a
chemical-safety-clearance decision, override a plant safety officer's
judgment, suppress an operating record, or disclose sensitive data without
governor approval and audit evidence.

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISCO-08 `8122`). Required capabilities:

- :robotics
- :identity
- :audit-ledger

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## License

AGPL-3.0-or-later.
