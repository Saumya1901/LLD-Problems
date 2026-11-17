# In-Memory Key-Value Store with TTL

## Interview

- Role: SDE-2 backend engineering
- Language: Java
- Duration: approximately 20–25 minutes
- Difficulty: Easy to Medium

## Problem statement

Design and implement an in-memory key-value store supporting:

- `put(key, value)` — Insert or update a key-value pair.
- `get(key)` — Retrieve a value.
- `delete(key)` — Remove a key.
- `put(key, value, ttl)` — Insert or update a key-value pair that expires after a specified duration.
- Concurrent access from multiple threads.

Prioritize practical object-oriented design. Choose your own classes, interfaces, data structures, and implementation details.

## Clarifying questions and agreed requirements

1. **Is TTL set by the client?**
   Yes. The client supplies the duration when calling `put(key, value, ttl)`.

2. **What happens when no TTL is provided?**
   The entry never expires. It remains until explicitly deleted or overwritten.

3. **What is the maximum storage capacity of the store?**
   Assume the store fits in available memory. Capacity limits are outside the scope of this interview.

4. **If capacity is reached, should existing keys be evicted using an algorithm such as LRU?**
   No capacity-based eviction policy is required. LRU is outside scope.

5. **Is retrieving all stored data required?**
   No. `get(key)` retrieves only the value associated with a single key. Listing or retrieving all entries is outside scope.

6. **Can the implementation use Java instead of C++?**
   Yes. Java is the agreed implementation language.

## Candidate design

Use this space for your design and trade-offs.

## Candidate implementation

Write Java here or create source files in this repository.

```java

```

## Interview process

The interviewer will ask one question at a time and will provide hints only when requested. Your code will not be modified unless you explicitly ask. Evaluation begins only when you say **"End Interview."**
