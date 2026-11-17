
/*
* LLD INTERVIEW: IN-MEMORY TASK MANAGEMENT SYSTEM
*
* Duration: 45 minutes
* Difficulty: SDE-2
*
* PROBLEM STATEMENT
*
* Design and implement an in-memory Task Management System
* similar to a simplified Jira.
*
* Multiple registered users should be able to create,
* assign, update, and view tasks concurrently.
*
* FUNCTIONAL REQUIREMENTS
*
* 1. User Management
*    - Register a user with a name.
*    - Each user has a unique system-generated ID.
*    - Only registered users can create tasks.
*    - Tasks can only be assigned to registered users.
*    - User deletion is NOT required.
*
* 2. Task Creation
*    - A registered user can create a task.
*    - Each task has a unique system-generated ID.
*    - Each task contains:
*        - Task ID
*        - Title (mandatory, non-empty)
*        - Description (optional)
*        - Creator
*        - Assignee (optional)
*        - Status
*    - A newly created task starts in TODO.
*    - Reject creation if the creator or supplied
*      assignee is not registered.
*
* 3. Task Assignment
*    - A task can have zero or one assignee.
*    - A user can be assigned multiple tasks.
*    - Tasks can be assigned or reassigned.
*    - Unassigned tasks are allowed.
*
* 4. Task Status
*    Supported statuses:
*        TODO
*        IN_PROGRESS
*        DONE
*
*    - Users can update task status.
*    - Forward and backward transitions are allowed.
*    - Example: DONE -> IN_PROGRESS is valid.
*
* 5. Task Retrieval and Filtering
*    - All users can view all tasks.
*    - Retrieve a task by its ID.
*    - Filter tasks by:
*        - Assignee
*        - Status
*        - Creator
*    - All filters are optional.
*    - Multiple filters use AND logic.
*    - No filters should return all tasks.
*
* NON-FUNCTIONAL REQUIREMENTS
*
* 1. Entirely in-memory storage.
* 2. Multiple users may perform operations concurrently.
* 3. Operations must be thread-safe.
* 4. Avoid inconsistent task state during concurrent updates.
* 5. Validate invalid users, tasks, and input.
*
* OUT OF SCOPE
*
* - Database persistence
* - REST APIs / Controllers
* - Authentication and authorization
* - User deletion
* - Task deletion
* - Notifications
* - Distributed systems
*
* INTERVIEW EXPECTATIONS
*
* 1. Define entities and relationships.
* 2. Design public Java APIs.
* 3. Implement the core operations.
* 4. Handle validation and error cases.
* 5. Explain concurrency and thread safety.
* 6. Discuss time and space complexity.
*
* NOTE:
* Focus on correctness, clean design, and concurrency.
* Do not introduce unnecessary design patterns.
  */
