# Logger Rate Limiter — Interview Audit

Date: 2026-10-17  
Language: Java  
Level: SDE-2 machine-coding interview  
Mode: INTERVIEWER / H0 (no hints)  
Status: Latest revision reviewed; passes clarified requirements. Interview not yet ended.

## 1. Problem statement

Design and implement an in-memory logger that determines whether a message should be printed.

### Required API

```java
public class Logger {
    public boolean shouldPrintMessage(int timestamp, String message) {
        // Your implementation
    }
}
```

### Functional requirements and constraints

1. A message can be printed only if the same message has not been printed within the preceding 10 seconds.
2. Different messages are treated independently.
3. Only successfully printed messages affect future rate-limiting decisions.
4. Timestamps arrive in non-decreasing order.
5. At exactly 10 seconds after the last successful print, the message becomes eligible again.
6. Assume a single-threaded environment.
7. Everything is stored in memory.
8. Return `true` when the message is eligible to be printed and `false` otherwise.

Initially, timestamp bounds and null-message behavior were unspecified. In Event 11, the timestamp contract was clarified to `0 <= timestamp <= Integer.MAX_VALUE`. Null-message behavior remains unspecified.

### Example

```java
Logger logger = new Logger();

logger.shouldPrintMessage(1, "foo");   // true
logger.shouldPrintMessage(2, "bar");   // true
logger.shouldPrintMessage(3, "foo");   // false
logger.shouldPrintMessage(11, "foo");  // true
logger.shouldPrintMessage(12, "bar");  // true
```

## 2. Interview rules

- No solutions, pseudocode, hints, implementation suggestions, or recommended data structures or algorithms unless explicitly requested.
- Do not confirm approach correctness while the candidate is reasoning.
- The candidate writes the entire implementation independently in Java.
- Answer requirement clarifications without revealing a solution.
- Do not repeatedly interrupt with theoretical questions.
- Do not introduce concurrency, distributed systems, or additional features unless requested as a follow-up.
- Java API documentation is permitted.
- Allow productive struggle without unsolicited help.
- Review the implementation only after the candidate says **“Ready for review.”**
- Do not modify implementation code without explicit permission.
- Maintain this audit throughout the session without interrupting coding to announce routine documentation updates.
- Preserve chronological reasoning and revisions; distinguish candidate reasoning from interviewer feedback and record mistakes accurately.
- Include no solutions or hints before they have been explicitly discussed.
- On **“End interview,”** finalize this audit, including unfinished work.

## 3. Chronological session record

### Event 1 — Candidate establishes interview

**Candidate request:** Conduct a Java SDE-2 Logger Rate Limiter machine-coding interview in H0 mode, with the requirements and review criteria recorded above. Start by presenting the problem and asking for an implementation without hints.

**Interviewer response:** Presented the API, requirements, and example. Asked the candidate to implement the complete class independently, allowed requirement clarifications and Java API documentation, and requested **“Ready for review”** when assessment is desired.

### Event 2 — Candidate requests permanent documentation

**Candidate question/request:** “can you create the question .md file” followed by mandatory instructions to maintain `INTERVIEW_AUDIT.md` with the problem, clarifications, design discussion, implementation history, interviewer questions, mistakes, testing, evaluation, and revision notes.

**Interviewer answer/action:** Agreed to create this file with the full question and history so far and maintain it throughout the session without modifying implementation code. Created this audit as the requested question and permanent interview record.

### Event 3 — Candidate expresses uncertainty about starting

**Candidate statement:** “like i dont know what to do right now”

**Interviewer response:** Asked the candidate to start by writing the `Logger` class and required method in a Java file and take time to think through its behavior. Maintained H0 mode and explained that a hint requires an explicit request. No algorithm, data structure, pseudocode, or correctness feedback was provided.

**Observation:** Candidate expressed uncertainty about how to begin. No technical or conceptual gap can yet be established from this statement alone.

### Event 4 — Candidate asks where to find the question

**Candidate question:** “i dont even know the quesiton dis it in interiew audit?”

**Interviewer answer:** Confirmed that Section 1 contains the full question and linked this file. Restated the method signature, the 10-second eligibility requirement, the provided `foo` example, and independence of different messages. No implementation guidance was provided.

### Event 5 — Candidate asks about message identity

**Candidate question:** “is there an id that will be provided by client for identigying the message ?”

**Interviewer answer:** No separate ID is provided. The `message` string itself identifies the message: strings with the same text are treated as the same message, and different text is treated independently.

### Event 6 — Candidate asks about case sensitivity

**Candidate question:** “so can the message be in upper case and lower case are Apple and apple and APPLE treated same?”

**Interviewer answer:** Message matching is case-sensitive. `"Apple"`, `"apple"`, and `"APPLE"` are three different messages, each rate-limited independently.

### Event 7 — Candidate asks about punctuation, whitespace, and input source

**Candidate question:** “is smaller changes apple. and apple: treated different and are trailing spaces considered as new messages and will it come from client itself”

**Interviewer answer:** `"apple."` and `"apple:"` are different messages. Trailing spaces count, so `"apple"` and `"apple "` are different messages. The caller supplies the `message` string directly; treat it exactly as received, including capitalization, punctuation, and whitespace.

### Event 8 — Candidate finishes coding

**Candidate statement:** “i have done the coding”

**Interviewer response:** Asked the candidate to say “Ready for review” when assessment was desired. No implementation was reviewed at that point.

### Event 9 — Candidate authorizes review

**Candidate statement:** “Ready for review”

**Interviewer action:** Read `src/Logger.java` and `src/Main.java`, compiled both, ran the candidate example, and executed an independent temporary test harness. No implementation files were changed.

**Interviewer feedback:** Initial score 8/10. Core behavior is correct. One integer-overflow edge case exists if negative timestamps are allowed, since timestamp bounds were never specified. Encapsulation, naming, and candidate-written test coverage need improvement. Candidate revisions and response are pending; no corrected implementation was provided.

## 4. Clarification questions

- **Message identity:** The candidate asked whether the client supplies a message ID. The interviewer clarified that there is no separate ID; message identity is determined by the string's text (Event 5).
- **Case sensitivity:** Matching is case-sensitive; `"Apple"`, `"apple"`, and `"APPLE"` are distinct messages (Event 6).
- **Punctuation, whitespace, and input source:** Punctuation and trailing spaces are significant. The caller supplies the string directly, and matching uses the text exactly as received (Event 7).
- The documentation questions and answers are recorded in Events 2 and 4.

## 5. Design discussion

No verbal design proposal or complexity analysis was submitted before coding.

**Observed implementation (interviewer observation):** A `HashMap<String, Integer>` stores the most recent successful print timestamp for each message. Rejected calls leave it unchanged. Old entries are retained. This meets the stated memory requirements; bounded retention was not required.

## 6. Implementation history

### Initial submission — preserved verbatim

Source: `src/Logger.java`, as first reviewed.

```java
import java.util.HashMap;
import java.util.Map;

public class Logger {

    Map<String, Integer> rateLimit = new HashMap<>();

    public boolean shouldPrintMessage(int timestamp, String message) {
        if (rateLimit.containsKey(message)) {
            int putTimeStamp = rateLimit.get(message);
            if (timestamp - putTimeStamp >= 10) {
                rateLimit.put(message, timestamp);
                return true;

            }
            return false;
        }

        rateLimit.put(message, timestamp);

        return true;
    }


}
```

The candidate also supplied `src/Main.java` with the five example calls and printed their results. No revisions have been observed. The interviewer has not modified implementation code.

## 7. Interviewer questions and candidate responses

- **Initial prompt:** Implement the complete Java class independently and say **“Ready for review”** when ready for assessment.
- **Candidate response:** Completed the implementation and explicitly said “Ready for review.”
- No technical probing questions or follow-up discussion have occurred.

## 8. Mistakes and gaps

- **Conditional correctness defect:** `timestamp - putTimeStamp` can overflow for non-decreasing timestamps spanning more than `Integer.MAX_VALUE` seconds. Reproducer: `shouldPrintMessage(Integer.MIN_VALUE, "x")` returns true, followed by `shouldPrintMessage(0, "x")` returning false despite sufficient elapsed time. Negative timestamps were not prohibited, but bounds were not clarified. This failure does not occur for nonnegative int timestamps; treat it as a bounds/robustness issue, not a failure of the ordinary examples.
- **Encapsulation:** `rateLimit` is package-visible and reassignable, allowing other code in the package to alter internal state.
- **Readability:** `rateLimit` and `putTimeStamp` could more clearly describe the stored values. Repeated blank lines are minor polish issues.
- **Testing gap:** Candidate supplied only the problem example; no boundary-focused assertions or additional test discussion were observed.
- No Java API difficulty was observed. No concurrency or bounded-memory feature is required.

## 9. Testing

**Candidate tests:** Five example calls in `Main`; candidate execution before review is unknown.

**Interviewer execution:** Compiled `Logger.java`, `Main.java`, and a temporary independent Java harness using `javac`. Compilation succeeded. Ran `Main`: output was `true, true, false, true, true`, as expected.

**Independent harness:** 22 checks passed, 1 failed.

Passed coverage:

- All five provided example results.
- First call at zero; repeated message at the same timestamp.
- Equal text in separately allocated String objects.
- Nine-second rejection and ten-second eligibility.
- Rejected calls do not reset the window; repeated successful windows.
- Independent case, punctuation, and trailing-space variants.
- Empty-string first and repeated calls.
- A nonnegative timestamp jump from zero to `Integer.MAX_VALUE`.
- Initial call at `Integer.MIN_VALUE`.

Failed check: subsequent call at zero after a successful print at `Integer.MIN_VALUE` incorrectly returned false.

Remaining untested: large-volume input, exhaustive timestamp combinations, and null-message behavior (unspecified). Out-of-order timestamps and concurrency are outside the stated contract. Temporary tests were outside the repository; implementation files were unchanged.

## 10. Initial evaluation

**Score: 8/10.** Review authorized by “Ready for review.” This assessment remains open for revisions.

- **Functional correctness:** Core logic correctly handles independent messages, successful-print-only updates, and the exact ten-second boundary. Full-int-range arithmetic has the conditional defect documented above.
- **Edge cases:** Executed ordinary edge cases pass; timestamp domain was not clarified. Candidate testing did not demonstrate boundary coverage.
- **Java quality:** Compiles cleanly; uses standard Java APIs correctly. Internal state should be better encapsulated.
- **Complexity:** Expected O(1) map operations per call, with O(U) entries for U distinct messages ever seen. String hashing/equality can take O(L) for message length L; retained key text also contributes to memory. Hash-map operations are not a universal worst-case O(1) guarantee. No candidate complexity explanation has been supplied yet.
- **Readability and maintainability:** Short, straightforward control flow; naming and formatting could improve.
- **SDE-2 readiness:** Passes the core coding portion of this narrowly scoped problem under nonnegative timestamps. A complete SDE-2 interview pass is not established from this simple task alone; stronger independent testing, encapsulation, and explicit input-bound reasoning would strengthen the submission.

No corrected implementation or solution was supplied. Candidate is invited to address the identified issues independently.

## 11. Revision notes and redo checklist

Interviewer-derived topics to revisit; candidate learnings have not yet been stated:

- Clarify input bounds before relying on arithmetic assumptions.
- Review Java integer overflow and elapsed-time comparisons.
- Protect internal state and use names that explain stored values.
- Build boundary tests rather than relying solely on the supplied example.

Concrete redo checklist:

- [ ] State timestamp-domain assumptions.
- [ ] Address the demonstrated arithmetic case if the full int domain is supported.
- [ ] Improve state encapsulation and naming.
- [ ] Add independent checks for identical timestamps, nine/ten-second boundaries, rejected-call behavior, and exact string identity.
- [ ] Explain time and space complexity, including retained historical messages.
- [ ] Re-run tests and explain any revisions.

## 12. Outstanding work

- Candidate response to review and any implementation revisions.
- Verification of revisions if submitted.
- Final audit completion when the candidate says “End interview.”

## 13. Second review — chronological continuation

### Event 10 — Candidate requests another review

**Candidate statement:** “READY FOR REVIEW”

**Observed changes:** Renamed `rateLimit` to `rateLimitMap`, added `if (timestamp < 0) return false;` with a comment asserting negative timestamps cannot occur, and added time/space complexity comments. `Main.java` still contains only the original example. No candidate explanation of the revision was submitted.

### Revised implementation — preserved verbatim

```java
import java.util.HashMap;
import java.util.Map;

public class Logger {

    Map<String, Integer> rateLimitMap = new HashMap<>();

    public boolean shouldPrintMessage(int timestamp, String message) {
        if (timestamp < 0) return false;//cannot have negetrive timestamp
        if (rateLimitMap.containsKey(message)) {
            int putTimeStamp = rateLimitMap.get(message);
            if (timestamp - putTimeStamp >= 10) {
                rateLimitMap.put(message, timestamp);
                return true;

            }
            return false;
        }

        rateLimitMap.put(message, timestamp);

        return true;
    }
/*TC: O(1)
* SC : O(N) MAXKEYS PUT */

}
```

### Review evidence

Compilation succeeded. A temporary independent harness ran 25 checks: 23 passed and 2 failed. Ordinary nonnegative scenarios, same-timestamp duplicates, nine/ten-second boundaries, rejection behavior, exact-text distinctions, empty strings, and the zero-to-maximum-int gap passed.

Failed checks:

- A first call at timestamp `-1` returned false; expected true if the full int domain is supported.
- A first call at `Integer.MIN_VALUE` returned false; expected true under the same interpretation.

Calls at positive timestamps after those rejected negative calls returned true, but this does not validate recording successful negative prints: those first prints were rejected. No implementation code was modified by the interviewer.

### Interviewer feedback and updated evaluation

**Score: 8/10 for the core problem under a nonnegative timestamp assumption.** The unresolved domain issue is conditional because timestamp bounds were never specified. The earlier review should have framed establishing those bounds as a clarification, rather than implying that negative timestamps must necessarily be accepted. Rejecting them introduces a new rule that the candidate has not established with the interviewer.

The new guard avoids the earlier subtraction case by refusing negative inputs, but changes observable behavior for an unspecified input domain. Candidate should state/clarify the timestamp assumption; full-int-domain support requires the demonstrated cases to work without silently excluding them. No implementation fix was provided.

Encapsulation remains unresolved: `rateLimitMap` is still package-visible and reassignable. Naming improved somewhat, but `putTimeStamp` remains unclear. Complexity comments show candidate awareness of constant map operations and linear retained keys; O(1) should be described as expected map-operation cost, with string hashing/equality costs considered. Candidate boundary-test evidence remains absent.

**SDE-2 assessment:** Core implementation remains a pass for nonnegative timestamps. Testing, encapsulation, and explicit contract reasoning remain weaker areas. No evidence supports upgrading the score yet.

**Interviewer clarification requested:** Are you assuming timestamps are nonnegative? Candidate response pending. For this interview, nonnegative timestamps may be adopted once that assumption is explicitly established; negative-input handling would then be outside the required contract.

### Remaining work after second review

- Establish the timestamp-domain assumption explicitly.
- Revisit encapsulation and candidate-written boundary checks.
- Submit any desired revisions for review.
- Finalize the audit on “End interview.”

### Event 11 — Timestamp domain established

**Candidate question:** “shoudlnt they be ?” in response to whether timestamps are assumed nonnegative.

**Interviewer answer:** Nonnegative timestamps are a reasonable assumption for elapsed seconds. The interviewer acknowledged leaving the range unspecified and explicitly established `0 <= timestamp <= Integer.MAX_VALUE` for this interview. Under non-decreasing timestamps in that range, subtraction is safe; the earlier overflow concern does not apply. Negative inputs are outside the contract, and the candidate's guard is optional.

**Assessment correction:** No functional correctness defect remains demonstrated within the clarified contract. The prior negative-input failures remain preserved as historical test results, but are now outside the required domain and must not be counted as correctness deductions. The core problem passes. Score remains 8/10 based on the remaining encapsulation and candidate testing gaps, rather than overflow. The timestamp-domain clarification is resolved; previous outstanding-work entries about establishing it are superseded by this event.

### Event 12 — Candidate requests confirmation of remaining issues

**Candidate statement:** “anything else i feel i made the correction i dont see any issue”

**Observed revision:** `rateLimitMap` is now private, resolving the visibility concern. The guard changed from `timestamp < 0` to `timestamp <= 0`. No other functional revisions were observed; Main still contains the original example.

**Current snapshot:**

```java
import java.util.HashMap;
import java.util.Map;

public class Logger {

    private Map<String, Integer> rateLimitMap = new HashMap<>();

    public boolean shouldPrintMessage(int timestamp, String message) {
        if (timestamp <= 0) return false;//cannot have negetrive timestamp
        if (rateLimitMap.containsKey(message)) {
            int putTimeStamp = rateLimitMap.get(message);
            if (timestamp - putTimeStamp >= 10) {
                rateLimitMap.put(message, timestamp);
                return true;

            }
            return false;
        }

        rateLimitMap.put(message, timestamp);

        return true;
    }
/*TC: O(1)
* SC : O(N) MAXKEYS PUT */

}
```

**Executed regression check:** Compiled the current Logger and a temporary harness. Compilation succeeded. `new Logger().shouldPrintMessage(0, "foo")` returned false; expected true under the explicitly clarified nonnegative timestamp contract. The new guard excludes zero, introducing a functional regression. No implementation code was modified.

**Interviewer feedback:** Private visibility fixes the prior encapsulation issue. One required correction remains: first messages at zero must be eligible. Naming and formatting are optional polish; no additional feature is required. Candidate should fix this independently and request review. Previous passing correctness assessment applies to the earlier `< 0` version, not this new `<= 0` revision. Score reassessment is pending this correction; the previous 8/10 is historical.

### Event 13 — Candidate fixes zero-timestamp regression

**Candidate statement:** “sorry updated”

**Observed revision:** Guard changed from `timestamp <= 0` to `timestamp < 0`. Private map visibility is retained.

**Latest implementation — preserved verbatim:**

```java
import java.util.HashMap;
import java.util.Map;

public class Logger {

    private Map<String, Integer> rateLimitMap = new HashMap<>();

    public boolean shouldPrintMessage(int timestamp, String message) {
        if (timestamp <0) return false;//cannot have negetrive timestamp
        if (rateLimitMap.containsKey(message)) {
            int putTimeStamp = rateLimitMap.get(message);
            if (timestamp - putTimeStamp >= 10) {
                rateLimitMap.put(message, timestamp);
                return true;

            }
            return false;
        }

        rateLimitMap.put(message, timestamp);

        return true;
    }
/*TC: O(1)
* SC : O(N) MAXKEYS PUT */

}
```

**Validation:** Compiled Logger, Main, and an independent temporary Java harness successfully. All 24 checks passed: provided example, first call at zero, same-timestamp rejection, separately allocated equal strings, nine/ten-second boundaries, rejected calls leaving the window unchanged, repeated windows, case/punctuation/whitespace distinctions, empty strings, a zero-to-maximum timestamp jump, and nine/ten-second boundaries near Integer.MAX_VALUE. Implementation files were not changed by the interviewer.

**Updated evaluation: 8.5/10.** No demonstrated functional correctness issue remains under the clarified nonnegative timestamp contract. Private state resolves the visibility concern. Expected O(1) map operations per call and O(U) retained entries remain appropriate, with string costs as previously noted. Core implementation passes this problem's SDE-2 coding bar. Candidate-written tests still cover only the provided example; broader candidate boundary testing and clearer naming/formatting remain improvement areas. No further implementation change is required to satisfy the functional requirements. This task alone does not establish overall SDE-2 readiness.

**Remaining work:** No known required code fixes. Independent boundary testing is a revision topic. Await any candidate discussion or “End interview” for final audit completion. Earlier open correctness and encapsulation findings are resolved by this revision; their history is preserved above.
