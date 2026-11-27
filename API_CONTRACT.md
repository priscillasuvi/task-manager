# API Contract

This project exposes method-based operations rather than HTTP routes. Each method returns `ApiResponse(statusCode, body)`.

## Error body
```json
{"error":{"code":"VALIDATION_ERROR","message":"Validation failed","details":[{"field":"title","issue":"must not be empty"}]}}
```
`code` and `message` are always strings. `details` is a list of field/issue objects; for non-field errors it may be empty.

## Operations
- `createTask(TaskRequest, String idempotencyKey)` — 201; 400 validation; 422 key reused for different request; 500 unexpected.
- `getTask(int id)` — 200; 404 unknown ID.
- `listTasks(TaskQuery)` — 200; 400 invalid query; 500 unexpected.
- `updateTask(int id, TaskRequest)` — 200; 400 validation; 404 unknown ID.
- `completeTask(int id)` — 200; 404 unknown ID.
- `deleteTask(int id)` — 200; 404 unknown ID. Deletes descendants.
- `createSubtask(int parentId, TaskRequest)` — 201; 400 invalid request or parent is itself a subtask; 404 unknown parent.
- `listSubtasks(int parentId)` — 200; 404 unknown parent.
- `createTaskFromText(String text, String idempotencyKey)` — 201; 400 blank text; 422 idempotency conflict; 502 model failure/invalid output; 503 missing key.
- `breakDownTask(int id, String idempotencyKey)` — 201; 400 subtask cannot be broken down; 404 unknown task; 422 idempotency conflict; 502 model failure/invalid output; 503 missing key.

## Error codes
| Status | Code | Meaning |
|---:|---|---|
| 400 | VALIDATION_ERROR | Input validation failed |
| 404 | NOT_FOUND | Requested task does not exist |
| 422 | IDEMPOTENCY_CONFLICT | Key was already used for a different request |
| 502 | BAD_GATEWAY | LLM failed or returned invalid/unusable content |
| 503 | SERVICE_UNAVAILABLE | Required LLM API key is not configured |
| 500 | INTERNAL_ERROR | Unexpected application error |

Model errors are mapped to 502, except missing configuration (503). Model-generated data is always validated before saving.
