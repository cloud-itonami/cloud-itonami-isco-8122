(ns platingcoord.store
  "SSoT for the ISCO-08 8122 metal finishing, plating and coating
  machine operators plant scheduling/logistics coordination actor
  (itonami actor pattern, ADR-2607121000 / CLAUDE.md Actors section;
  README's 'Robotics premise' — a plant scheduling/logistics
  coordination robot performs crew scheduling, production-run/
  inventory/progress-record logging and plating-chemicals/raw-material
  supply-order coordination for a metal-finishing/plating/coating
  crew under this advisor/governor pair, which never dispatches
  hardware itself, never operates plating equipment itself, and never
  finalizes a plating-operation-execution decision or a chemical-
  safety-clearance decision, and never overrides a plant safety
  officer's judgment — those remain the plant safety officer's
  exclusive judgment). Modeled closely on cloud-itonami-isco-7535's
  tannerycoord.store.

  Domain:

    plater   — a registered metal finishing/plating/coating machine
               operator crew member (:plater-id, :name)
    facility — a registered plating facility/line {:facility-id :name
               :max-supply-cost number}. `:max-supply-cost` is an
               informational registered ceiling used only to decide
               whether a `:coordinate-supply-order` proposal escalates
               to human sign-off (the governor never blocks a
               within-threshold order outright; it only decides
               commit vs. escalate).
    record   — a committed operating record (a logged production-run/
               inventory/progress entry, a scheduled crew/shift
               operation, a flagged safety concern, or a coordinated
               plating-chemicals/raw-material supply order) — written
               ONLY via commit-record!. This actor coordinates plant
               scheduling/logistics ONLY — a `record` is a
               coordination artifact, never a plating-operation-
               execution act, never a chemical-safety-clearance
               decision, and never a plant safety officer's-judgment
               override.
    ledger   — append-only audit trail, commit or hold.")

(defprotocol Store
  (plater [s plater-id])
  (facility [s facility-id])
  (records-of [s plater-id])
  (ledger [s])
  (register-plater! [s plater])
  (register-facility! [s facility])
  (commit-record! [s record])
  (append-ledger! [s fact]))

(defrecord MemStore [a]
  Store
  (plater [_ plater-id] (get-in @a [:platers plater-id]))
  (facility [_ facility-id] (get-in @a [:facilities facility-id]))
  (records-of [_ plater-id] (filter #(= plater-id (:plater-id %)) (:records @a)))
  (ledger [_] (:ledger @a))
  (register-plater! [s p]
    (swap! a assoc-in [:platers (:plater-id p)] p) s)
  (register-facility! [s f]
    (swap! a assoc-in [:facilities (:facility-id f)] f) s)
  (commit-record! [s record]
    (swap! a update :records (fnil conj []) record) s)
  (append-ledger! [s fact]
    (swap! a update :ledger (fnil conj []) fact) s))

(defn mem-store
  ([] (mem-store {}))
  ([seed] (->MemStore (atom (merge {:platers {} :facilities {} :records [] :ledger []}
                                    seed)))))
