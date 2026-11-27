package com.learning.taskmanager.service;

import com.learning.taskmanager.model.*;
import com.learning.taskmanager.repository.*;
import com.learning.taskmanager.parser.*;
import com.learning.taskmanager.event.*;
import com.learning.taskmanager.exception.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.*;

public class TaskService {
    private final TaskRepository repo;
    private final TaskParser parser;
    private final BreakdownStrategy breakdown;
    private final IdempotencyStore idem = IdempotencyStore.getInstance();
    private final List<TaskEventListener> listeners = new ArrayList<>();

    public TaskService(TaskRepository r, TaskParser p, BreakdownStrategy b) {
        repo = r;
        parser = p;
        breakdown = b;
    }

    public void addListener(TaskEventListener l) {
        listeners.add(l);
    }

    public void removeListener(TaskEventListener l) {
        listeners.remove(l);
    }

    private void notify(String e, Task t) {
        for (TaskEventListener l : new ArrayList<>(listeners)) l.onEvent(e, t);
    }

    public Task get(int id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Task " + id + " not found"));
    }

    public List<Task> all() {
        return repo.findAll();
    }

    public Task createAndStore(TaskRequest r, String key) {
        String fp = "create|" + r.fingerprint();
        ApiResponse old = idem.get(key, fp);
        if (old != null) return findSaved(old);
        Task t = repo.save(build(r, null, null));
        notify("CREATED", t);
        idem.put(key, fp, new ApiResponse(201, "{\"data\":" + taskJson(t) + "}"));
        return t;
    }

    private Task findSaved(ApiResponse r) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\\"id\\\":(\\d+)").matcher(r.getBody());
        if (m.find()) return get(Integer.parseInt(m.group(1)));
        throw new IllegalStateException("Stored idempotent response missing task ID");
    }

    public Task update(int id, TaskRequest r) {
        Task old = get(id);
        Task t = build(r, old.getParentId(), old.getRecurrence()).toBuilder().id(id).build();
        Task saved = repo.save(t);
        notify("UPDATED", saved);
        return saved;
    }

    public Task complete(int id) {
        Task saved = repo.save(get(id).toBuilder().completed(true).build());
        notify("COMPLETED", saved);
        return saved;
    }

    public void delete(int id) {
        Task t = get(id);
        for (Task c : repo.findAll()) if (Objects.equals(c.getParentId(), id)) delete(c.getId());
        repo.delete(id);
        notify("DELETED", t);
    }

    public Task subtask(int parent, TaskRequest r) {
        Task p = get(parent);
        if (p.getParentId() != null) throw validation("parentId", "a subtask cannot have subtasks");
        Task s = repo.save(build(r, parent, null));
        notify("CREATED", s);
        return s;
    }

    public List<Task> subtasks(int parent) {
        get(parent);
        return repo.findAll().stream().filter(t -> Objects.equals(t.getParentId(), parent)).toList();
    }

    public Task createFromText(String text, String key) {
        String fp = "ai|" + text;
        ApiResponse old = idem.get(key, fp);
        if (old != null) return findSaved(old);
        if (blank(text)) throw validation("text", "must not be empty");
        ParsedTask p;
        try {
            p = parser.parse(text, LocalDate.now().toString());
        } catch (ServiceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new BadGatewayException("LLM call or parsing failed", e);
        }
        TaskRequest r = new TaskRequest(p.getTitle(), p.getDescription(), "false", p.getDueDate(), "MEDIUM");
        try {
            validate(r);
        } catch (ValidationException e) {
            throw new BadGatewayException("LLM returned invalid task fields");
        }
        Task t = repo.save(build(r, null, p.getRecurrence()));
        notify("CREATED", t);
        idem.put(key, fp, new ApiResponse(201, "{\"data\":" + taskJson(t) + "}"));
        return t;
    }

    public List<Task> breakDown(int id, String key) {
        Task parent = get(id);
        if (parent.getParentId() != null) throw validation("id", "a subtask cannot be broken down");
        String fp = "breakdown|" + id;
        ApiResponse old = idem.get(key, fp);
        if (old != null) return tasksFromResponse(old);
        List<TaskRequest> requests;
        try {
            requests = breakdown.breakdown(parent);
        } catch (Exception e) {
            if (e instanceof ApiException) throw (ApiException) e;
            throw new BadGatewayException("Breakdown failed", e);
        }
        if (requests == null || requests.isEmpty()) throw new BadGatewayException("Model returned no subtasks");
        for (TaskRequest r : requests)
            try {
                validate(r);
            } catch (ValidationException e) {
                throw new BadGatewayException("Model returned invalid subtask");
            }
        List<Task> saved = new ArrayList<>();
        for (TaskRequest r : requests) {
            Task t = repo.save(build(r, id, null));
            saved.add(t);
            notify("CREATED", t);
        }
        idem.put(key, fp, new ApiResponse(201, "{\"data\":" + tasksJson(saved) + "}"));
        return saved;
    }

    private List<Task> tasksFromResponse(ApiResponse r) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\\"id\\\":(\\d+)").matcher(r.getBody());
        List<Task> out = new ArrayList<>();
        while (m.find()) out.add(get(Integer.parseInt(m.group(1))));
        return out;
    }

    public List<Task> query(TaskQuery q) {
        if (q.page < 0 || q.size < 1) throw validation("page", "page must be >=0 and size must be >0");
        Stream<Task> s = repo.findAll().stream();
        if (!blank(q.search))
            s = s.filter(t -> (t.getTitle() + " " + t.getDescription()).toLowerCase().contains(q.search.toLowerCase()));
        if (q.status != null) s = s.filter(t -> t.isCompleted() == q.status.equalsIgnoreCase("completed"));
        if (q.priority != null) s = s.filter(t -> t.getPriority().name().equalsIgnoreCase(q.priority));
        Comparator<Task> c = switch (q.sortBy == null ? "id" : q.sortBy) {
            case "title" -> Comparator.comparing(Task::getTitle);
            case "dueDate" -> Comparator.comparing(t -> t.getDueDate() == null ? "9999-99-99" : t.getDueDate());
            default -> Comparator.comparingInt(Task::getId);
        };
        List<Task> all = s.sorted(c).toList();
        int start = (int) Math.min((long) q.page * q.size, all.size()), end = Math.min(start + q.size, all.size());
        return all.subList(start, end);
    }

    public void validate(TaskRequest r) {
        List<String[]> issues = new ArrayList<>();
        if (r == null) {
            issues.add(new String[]{"request", "must not be null"});
            throw new ValidationException(issues);
        }
        if (blank(r.getTitle())) issues.add(new String[]{"title", "must not be empty"});
        if (blank(r.getDescription())) issues.add(new String[]{"description", "must not be empty"});
        if (!"true".equals(r.getCompleted()) && !"false".equals(r.getCompleted()))
            issues.add(new String[]{"completed", "must be exactly true or false"});
        if (!blank(r.getDueDate())) try {
            if (!r.getDueDate().matches("\\d{4}-\\d{2}-\\d{2}")) throw new Exception();
            LocalDate.parse(r.getDueDate());
        } catch (Exception e) {
            issues.add(new String[]{"dueDate", "must be a real YYYY-MM-DD date"});
        }
        if (blank(r.getPriority()) || !Set.of("LOW", "MEDIUM", "HIGH").contains(r.getPriority().trim().toUpperCase()))
            issues.add(new String[]{"priority", "must be LOW, MEDIUM, or HIGH"});
        if (!issues.isEmpty()) throw new ValidationException(issues);
    }

    private Task build(TaskRequest r, Integer parent, String recurrence) {
        validate(r);
        return Task.builder().title(r.getTitle().trim()).description(r.getDescription().trim()).completed(Boolean.parseBoolean(r.getCompleted())).dueDate(blankToNull(r.getDueDate())).priority(Priority.valueOf(r.getPriority().trim().toUpperCase())).parentId(parent).recurrence(recurrence).build();
    }

    private boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private String blankToNull(String s) {
        return blank(s) ? null : s.trim();
    }

    private ValidationException validation(String f, String m) {
        return new ValidationException(Collections.singletonList(new String[]{f, m}));
    }

    public static String taskJson(Task t) {
        return "{\"id\":" + t.getId() + ",\"title\":\"" + esc(t.getTitle()) + "\",\"description\":\"" + esc(t.getDescription()) + "\",\"completed\":" + t.isCompleted() + ",\"dueDate\":\"" + esc(t.getDueDate()) + "\",\"priority\":\"" + t.getPriority() + "\",\"parentId\":" + t.getParentId() + "}";
    }

    public static String tasksJson(List<Task> ts) {
        return "[" + ts.stream().map(TaskService::taskJson).collect(Collectors.joining(",")) + "]";
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }
}
