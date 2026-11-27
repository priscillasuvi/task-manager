# Task Manager — AI Backend Engineering (Plain Java)

A framework-free Java 17 Maven project implementing a task-management service as Java methods that return `ApiResponse`. It supports CRUD, validation, natural-language task creation, subtasks, atomic breakdown, idempotency, filtering/search/sorting/pagination, and observer events.

## Run
- Install JDK 17+ and Maven 3.8+.
- Open this folder as a Maven project in IntelliJ IDEA or Cursor.
- Compile: `mvn clean package`
- Run offline demo and tests: `mvn exec:java` is not configured; run `com.learning.taskmanager.app.Main` from the IDE, or:
  `java -cp target/classes com.learning.taskmanager.app.Main`
- No external dependencies or test framework are required. `TestRunner` uses fake LLM responses and runs offline.
- To enable the example HTTP-backed LLM client, set `LLM_API_KEY` in your environment. Never commit secrets. If absent, AI operations return 503.

## Packages
`model` (Task, requests, queries), `service` (business logic), `repository` (storage interface and singleton implementation), `parser` (TaskParser strategies and LLM client), `event` (Observer listeners), `exception` (API exceptions), `api` (TaskApi and response mapping), `app` (Main and TestRunner).

## Example
```java
TaskApi api = DemoFactory.createOfflineApi();
ApiResponse response = api.createTask(
    new TaskRequest("Pay rent", "Monthly rent", "false", "2026-12-01", "HIGH"), "rent-001");
System.out.println(response.toJson());
```
Example success: `{"data":{"id":1,"title":"Pay rent",...}}` with status 201.
Validation errors use one shape:
```json
{"error":{"code":"VALIDATION_ERROR","message":"Validation failed","details":[{"field":"title","issue":"must not be empty"}]}}
```

## API operations
| Operation | Parameters | Success | Error statuses |
|---|---|---:|---|
| createTask | TaskRequest, idempotencyKey | 201 | 400, 404, 422, 500 |
| getTask | id | 200 | 404, 500 |
| listTasks | TaskQuery | 200 | 400, 500 |
| updateTask | id, TaskRequest | 200 | 400, 404, 500 |
| completeTask | id | 200 | 404, 500 |
| deleteTask | id | 200 | 404, 500 |
| createSubtask | parentId, TaskRequest | 201 | 400, 404, 500 |
| listSubtasks | parentId | 200 | 404, 500 |
| createTaskFromText | text, idempotencyKey | 201 | 400, 422, 502, 503 |
| breakDownTask | id, idempotencyKey | 201 | 400, 404, 422, 502, 503 |
| getTask | unknown id | 404 | — |

All API methods return `ApiResponse`. Error `code` and `message` are strings. Status 400 is validation, 404 is missing resource, 422 is idempotency conflict, 502 is upstream/model or invalid model output, 503 is missing model configuration, and unexpected errors are 500.

## Design patterns
- Builder: `Task.Builder`, `TaskQuery.Builder`
- Singleton: `InMemoryTaskRepository`, `IdempotencyStore`
- Factory: `ParserFactory`, `ErrorResponseFactory`, `DemoFactory`
- Strategy: `TaskParser`, `BreakdownStrategy`, `TaskFilter`, `SortStrategy`
- Observer: `TaskEventListener`, `ConsoleLogListener`, `AuditTrailListener`

## AI prompts
Task creation prompt supplies today's date and requests JSON with `title`, `description`, `dueDate`, and `recurrence`; JSON-only output makes parsing predictable and date context resolves relative phrases. Breakdown prompt requests one `title|description` item per line to simplify parsing and validate all items before persistence.

`TaskParser` is an extension point: implement `parse(String text, String today)` and inject it into `TaskService`; no service changes are needed to add a provider or parser.

## Test critique / hardening
`TestRunner` includes blank fields, invalid boolean/priority/date, unknown ID, repeated idempotency key, key conflict, malformed model output, model failure, subtask-of-subtask, empty breakdown and out-of-range pagination. Fake clients avoid network and API keys. Successful idempotent retries return the original response and do not invoke the parser again.
