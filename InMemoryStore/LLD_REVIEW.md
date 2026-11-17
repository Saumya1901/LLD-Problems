# In-Memory Key-Value Store: SDE-2 Review

## 1. Requirements and assumptions

Reviewed `QUESTION.md`, `src/StoringService.java`, `src/ValueObject.java`, and `src/Main.java`. Line references below refer to the existing source files.

The documented contract explicitly requires insert **or update**, deletion, single-key retrieval, optional client-supplied TTL, and concurrent access. Entries written without TTL must remain until deleted or overwritten. Capacity limits and LRU are out of scope. The current request authorizes this review despite the interview process described in `QUESTION.md`.

Clarify these remaining ambiguities:

- What is the TTL unit? The implementation interprets the integer as milliseconds, but the problem does not specify units.
- Is an entry expired exactly at its deadline, or only after it? Is zero TTL allowed?
- Are negative TTLs invalid, or is `-1` an explicitly supported public sentinel? What duration range must be supported?
- What should missing or expired reads return: null, an optional result, or an exception? These states should have a deliberate contract.
- Are null keys, null payloads, and empty strings accepted? If rejected, what exception is expected?
- What does `delete` return, especially for an expired entry? Its removal obligation is already explicit.
- Should a TTL write reset the deadline of an existing key? Does an ordinary overwrite remove a prior TTL? The latter is strongly implied by the documented no-TTL requirement.
- Is expiration based on elapsed duration regardless of wall-clock adjustments? When does TTL begin: method invocation or actual insertion?
- Does expiry require physical memory reclamation, or only logical invisibility? What reclamation delay is acceptable?
- What atomicity is promised for overlapping operations? Can a read that observes a live entry return after its deadline? A natural baseline is a result consistent with some instant during the operation.
- Is `putByTtl` an acceptable name for the documented TTL overload? No supplied interface makes that naming difference a compilation defect.

## 2. Correctness issues

Compilation verification: `javac -Xlint:all -d <temporary-directory> src/*.java` succeeded on OpenJDK 21. No compilation errors or invalid Java API signatures were found. Valid API calls can still implement the wrong behavior.

### C1. Ordinary writes do not update existing entries

- **Severity:** High
- **Affected method and line:** `StoringService.put`, line 24.
- **Description:** `computeIfAbsent` preserves an existing mapping. This contradicts the explicit insert-or-update requirement. It also prevents a no-TTL overwrite from replacing a TTL entry.
- **Scenario:** `put("a", "one")`, then `put("a", "two")`; the original object and payload remain. Write a short-lived TTL entry, wait until expired, then call `put` with the same key: subsequent reads still return null because the expired mapping remains present.
- **Concept:** Presence in a map is distinct from logical TTL validity; insert-if-absent semantics differ from update semantics.

### C2. TTL writes do not update values or deadlines

- **Severity:** High
- **Affected method and line:** `StoringService.putByTtl`, line 41.
- **Description:** A TTL write is ignored whenever the key is already mapped, including expired keys. It cannot change a payload, attach TTL to a permanent entry, or refresh a previous TTL.
- **Scenario:** `put("a", "one")`, followed by `putByTtl("a", "two", 10)`; the original permanent entry remains permanently live. Rewriting an expired TTL key also leaves it inaccessible.
- **Concept:** Correct use of an atomic map API requires matching its semantics to the application contract.

### C3. Delete never removes anything

- **Severity:** High
- **Affected method and lines:** `StoringService.delete`, lines 27–35, especially 28–32.
- **Description:** The method checks presence and expiry and may return an object, but never changes the map. This violates deletion regardless of the return-value policy.
- **Scenario:** `put("a", "one")`, `delete("a")`, `get("a")`; the entry is still returned. Deleting an expired entry also leaves its mapping allocated.
- **Concept:** Query operations do not perform state mutation; a method's observable postcondition must match its contract.

### C4. Missing reads dereference null

- **Severity:** Medium
- **Affected method and lines:** `StoringService.get`, lines 15–16.
- **Description:** An absent non-null key produces null from the map, immediately followed by a method call on that null reference. This is an uncontrolled missing-key path. Its classification as an API contract violation depends on the agreed absence policy; the implementation otherwise uses null for unavailable expired entries.
- **Scenario:** `new StoringService().get("unknown")` throws `NullPointerException`. This was reproduced.
- **Concept:** Null result handling and separation of absence from invalid arguments.

### C5. The stored payload cannot be retrieved through the public API

- **Severity:** High
- **Affected methods and lines:** `StoringService.get`, lines 10 and 17; `ValueObject`, line 5 and public methods at lines 9–26.
- **Description:** `get` returns a `ValueObject`, whose private string has no public accessor or other payload-retrieval operation. Returning a wrapper is a design choice, but a caller cannot obtain the stored value using this API. Default `toString` does not expose it.
- **Scenario:** A caller inserts `("a", "hello")` and receives an object from `get("a")`, but cannot read `"hello"` without bypassing encapsulation.
- **Concept:** Public API usability and information hiding: internal metadata and payload access have different responsibilities.

### C6. Expired entries are retained indefinitely

- **Severity:** Medium, conditional on reclamation expectations
- **Affected methods and lines:** `StoringService.get`, lines 15–19; `delete`, lines 27–35; `ValueObject.isKeyExpired`, lines 23–26.
- **Description:** Expiration changes a flag but never releases a mapping or payload. Logical expiry works for an isolated positive-TTL insertion after its deadline, but storage grows with historical distinct keys. Physical cleanup is not explicitly required, so this is a resource limitation rather than an unconditional TTL contract violation. Failure to rewrite expired keys is already covered by C1/C2.
- **Scenario:** Insert a million distinct short-lived keys, wait, and access them all. They are logically expired but all remain strongly reachable from the map.
- **Concept:** Logical deletion versus memory reclamation and object reachability.

### C7. Deadline comparison and TTL input policies need agreement

- **Severity:** Medium, conditional on the agreed contract
- **Affected methods and lines:** `ValueObject.isKeyExpired`, line 23; TTL constructor, lines 15–18; `StoringService.putByTtl`, line 38.
- **Description:** The strict `>` comparison considers an entry live at elapsed time exactly equal to TTL. Zero TTL can therefore be live within its creation millisecond. `-1` becomes permanent; other negative TTLs normally expire on their first check. None of these policies is documented or validated.
- **Scenario:** At `createdAt + 100`, a TTL of 100 is still live. A TTL write with `-1` never expires. These observations become bugs only if the agreed boundary or input contract disagrees.
- **Concept:** Temporal boundary conditions, sentinel values, and input-domain contracts. The long subtraction promotes the integer TTL; there is no ordinary integer-addition overflow in this expression. If milliseconds are intended, positive `int` TTLs reach only about 24.9 days; whether that is sufficient is unspecified.

### C8. Wall-clock changes can alter effective lifetime

- **Severity:** Medium, conditional on elapsed-duration semantics
- **Affected methods and lines:** `ValueObject` constructors, lines 12 and 18; `isKeyExpired`, line 23.
- **Description:** Expiry uses wall-clock timestamps. A backward clock change can extend an unchecked entry's lifetime; a forward jump can expire it before the requested real elapsed duration. Once observed expired, the sticky flag prevents revival of that object.
- **Scenario:** Insert with a 1-second TTL, then move the clock back 1 minute before any expiry check. The entry can remain live for roughly an extra minute.
- **Concept:** Wall-clock time versus elapsed-time measurement. Java documents their distinct timing roles in [System](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/System.html).

## 3. Concurrency review

### Guarantees that are present

`ConcurrentHashMap` protects its own operations. `computeIfAbsent` atomically establishes an absent mapping; competing writers do not corrupt the table. However, preserving the first mapping is the wrong sequential behavior for the required updates. Map thread safety does not establish the correctness of the store's full operations.

Insertion through the map safely publishes a constructed `ValueObject` to a reader that obtains it. The payload, TTL, and timestamp have no subsequent writes in this implementation. Their lack of `final` is therefore not, by itself, a demonstrated visibility bug. This assumes ordinary safe sharing of the service itself. See the publication and atomic-operation guarantees in the [ConcurrentHashMap documentation](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ConcurrentHashMap.html).

### Compound operations and possible interleavings

- `delete` lines 28–31 perform a separate presence check and retrieval. This sequence is not atomic. **With only the current service methods, entries are never removed or replaced**, so a concurrent service call cannot currently make that retrieval null after a true presence check. Do not present that hypothetical race as an existing public-API failure.
- The package-visible mutable `hashMap` at line 7 permits same-package callers to remove entries or replace the map reference. A caller can remove a key between `delete`'s presence check and lookup, causing a null dereference. Reassigning that field concurrently also has no synchronization discipline. These are concrete encapsulation risks requiring access outside the service methods.
- If actual removal or replacement becomes part of the implementation, interactions among reads, deletes, writes, and expiry checks need a fresh atomicity review. Synchronizing an old entry object would not coordinate operations on a replacement object.
- There is no atomic successful deletion to assess today: C3 is already a sequential correctness failure.

### Synchronization and time

`isKeyExpired` is synchronized on the individual `ValueObject`. All accesses to its expiry flag are inside that method, and the flag is also an `AtomicBoolean`. These mechanisms overlap for flag visibility. This is unnecessary complexity, not evidence of a data race. The monitor also serializes expiry checks on a hot key; `get` itself is not synchronized despite its comments. No lock-order cycle is apparent in the current source.

The comments at `StoringService` lines 11–14 suggest that pausing after a timestamp requires synchronization. A monitor cannot stop time or prevent a thread from pausing after the expiry check. A read can observe a live entry, pause, and return after its deadline. Under a contract allowing observation at an instant during the call, this is acceptable. A guarantee about freshness at response time must be specified separately. A returned object is also not a promise of perpetual validity.

### Code quality, design, and complexity

These are optional improvements or limitations unless tied to a correctness issue above:

- Keeping entry metadata in a separate class is a reasonable starting decomposition. The unusable payload API remains C5.
- Package-visible, reassignable storage weakens ownership. Mutable declarations for otherwise stable entry data make the intended invariants harder to identify; no external setter exists today.
- Unused imports `HashMap` and `Optional` at `StoringService` lines 1–2, unused field `putTime` at line 8, and unused local `putTime` at line 39 add noise. The local shadows the field and does not determine TTL start; the constructor timestamp does.
- `Main` lines 1–15 are IDE sample code and exercise none of the store. No tests were found. Naming, formatting, and speculative comments reduce clarity but are lower priority than functional failures.
- Expected hash-table work for each operation is O(1), with constant-time expiry arithmetic; `delete` currently makes two lookups. These are expected costs, not unconditional worst-case latency guarantees. Hash collisions, resizing, contention, and monitor waiting can increase latency.
- Space is O(N + S), where N is the number of retained distinct keys and S is their retained key/payload size. Expired mappings count toward N. Each entry also allocates an atomic flag object. Temporary call-stack space per operation is O(1).
- No additional framework, generic key support, persistence, capacity policy, scheduler, or elaborate class hierarchy is required for the stated interview scope.

## 4. Edge-case test scenarios

Tests below describe observable requirements, not corrected implementation code. Source files were not changed. A temporary external probe reproduced C1–C4, retained expired mappings, acceptance of a null payload inside a wrapper, and rejection of a null key. Boundary and concurrency cases below are proposed tests, not claims of executed verification.

### Tests with established expectations

| Inputs / sequence | Expected behavior | Current observation or concern |
| --- | --- | --- |
| `put("a", "one")`, then `get("a")` | Client can retrieve payload `"one"`. | Wrapper exists, but payload cannot be read through the public API. |
| `put("a", "one")`, `put("a", "two")`, read | Payload is `"two"`. | Original mapping survives. |
| Insert a permanent entry, allow time to pass, read | Entry remains live without overwrite or delete. | Expiry sentinel supports this. |
| Insert live entry, delete, then inspect availability | Entry is absent; return representation depends on absence policy. | Entry remains live. |
| Insert two distinct keys, delete one | Deleted key is absent; other key is unaffected. | Deleted key remains. |
| TTL insert with positive duration, read well before deadline | Original payload is available. | Logical check supports this; payload API prevents normal value assertion. |
| TTL insert, read well after deadline | Payload is unavailable. | Returns null while retaining mapping. |
| Expire a TTL key, then ordinary write to same key | New payload is available without TTL. | Expired object survives. |
| Expire a TTL key, then positive-TTL write to same key | New payload is available within its new duration. | Expired object survives. |
| Permanent entry, then TTL overwrite | New payload replaces old; entry expires after the new duration. | Write is ignored. |
| Live TTL entry, then ordinary overwrite | New payload replaces old and does not expire from the old TTL. | Write is ignored. |
| TTL write to existing key with a different payload | New payload replaces old. | Write is ignored; deadline-reset detail still needs agreement. |
| Delete key twice, including an initially absent key | Key remains absent; return policy unspecified. | Initially absent delete returns null; present key is never removed. |
| Two independent store instances using the same key | Their entries remain independent. | Storage is per instance. |
| Concurrent writes to distinct keys, followed by reads after writers finish | Every inserted key is retrievable. | Needs concurrent verification. |
| Write A completes, then write B to same key starts and finishes | Final payload is B, including across threads. | Existing mapping prevents B. |
| Concurrent writes to an absent key, all operations finish | One complete submitted value is stored, with no corrupted entry. Winner depends on ordering. | Atomic insertion helps; no payload accessor for normal assertion. |

For TTL tests, explicitly choose a unit and use large margins around deadlines; a small sleep cannot prove exact-boundary correctness. Thread scheduling can delay a read beyond the intended window.

### Tests whose expected policy must first be specified

- Missing and expired reads: both outcomes should follow the agreed absence API; currently one throws and the other returns null.
- Null key for every operation: currently rejected by the map. Null payload for both write paths: currently accepted inside a non-null wrapper. The map's null-value restriction does not reject the wrapper's null field.
- Empty key and empty payload: currently accepted; decide whether that is intended.
- TTL `0`, `-1`, `-2`, `Integer.MIN_VALUE`, and `Integer.MAX_VALUE`: establish validation, sentinel, range, and unit expectations first.
- Exact deadline: check just before, exactly at, and just after the deadline using controlled timing. Current comparison is live at equality.
- Repeated TTL overwrite: agree when the replacement's duration begins and whether the deadline resets.
- Read paused across a deadline and read overlapping deletion or overwrite: define allowed ordering before declaring a failure.
- Clock jumps in both directions: expected behavior depends on the timing contract.
- Expired-key retention after many distinct writes: assert memory reclamation only if it is required.
- Same-key read/write/delete contention with explicit operation ordering, plus checks for unexpected exceptions: requires correct sequential behavior and an agreed concurrent contract to interpret results.

## 5. Interview questions

1. Trace `put("a", "one")`, `put("a", "two")`, and `get("a")` through your implementation. Which documented requirement does the result satisfy or violate?
2. What changes in the map when `delete` returns a non-null object? What observable postcondition should deletion have?
3. What happens on `get` for a never-inserted key, and how can a caller obtain the string from a successful read?
4. Which TTL unit does your expression implement, and what happens at elapsed time exactly equal to TTL?
5. What do TTL values zero, minus one, and minus two mean in your implementation? Which of those meanings are requirements?
6. What happens when you overwrite an expired entry, or replace a TTL entry using ordinary `put`?
7. How would a backward wall-clock adjustment affect an entry that has not yet been checked for expiry?
8. What does `computeIfAbsent` make atomic, and which store-level guarantees does that leave unaddressed?
9. Can the `containsKey`/`get` sequence currently fail through service methods alone? How does package-level map access change the possible interleavings?
10. What protection comes from `synchronized` and from `AtomicBoolean` here? Can either guarantee that a read returns before the deadline?
11. Why can a reader see constructor-initialized fields after obtaining an entry from the concurrent map, even though those fields are not final?
12. How would you justify the public value representation, retained-expiry memory cost, and synchronization cost within a 20–25 minute implementation exercise?

## 6. Interview readiness

**Implementation assessment:** This is a compilable starting point, but it is not yet a correct SDE-2 submission. Update and deletion fail explicit requirements even in a single thread. Retrieval cannot expose the payload, and absence handling is incomplete. Those issues carry more weight than formatting or sophisticated concurrency discussion. Positive-TTL insertion and logical expiry provide a useful starting path, but boundary policy and overwrite behavior need clear reasoning.

**Code quality assessment:** The small service/entry split is understandable, and choosing a concurrent map shows awareness of concurrent access. API usability, encapsulation, unused state, stale comments, and overlapping synchronization mechanisms weaken the design. More abstractions would not compensate for incorrect observable behavior.

**Ability to explain:** This review cannot determine your interview communication or reasoning ability. Your comments show awareness of time-related interleavings, but they do not establish that the chosen synchronization is necessary. An interviewer would look for accurate execution traces, clear requirement questions, separation of real races from hypothetical ones, and a sound account of map guarantees. Explaining these well is valuable, while passing the core behavioral cases remains necessary.

Verification was limited to compilation and a small sequential runtime probe in a temporary directory. No stress test, exact-deadline test, or clock-manipulation test was run. No source changes or corrected implementation are included.
