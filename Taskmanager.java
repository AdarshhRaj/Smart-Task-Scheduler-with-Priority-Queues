package com.taskscheduler.service;

import com.taskscheduler.model.Task;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for task management operations
 * Equivalent to the PHP functions for task handling
 */
public class TaskManager {
    private final FileStorage fileStorage;
    
    public TaskManager(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
    }
    
    /**
     * Add a new task to the list
     * Equivalent to PHP addTask($task_name)
     */
    public boolean addTask(String taskName) {
        if (taskName == null || taskName.trim().isEmpty()) {
            return false;
        }
        
        List<Task> tasks = fileStorage.loadTasks();
        
        // Check for duplicate tasks
        boolean taskExists = tasks.stream()
                .anyMatch(task -> task.getName().equalsIgnoreCase(taskName.trim()));
        
        if (taskExists) {
            return false; // Duplicate task should not be added
        }
        
        // Generate unique ID and add task
        String taskId = UUID.randomUUID().toString();
        Task newTask = new Task(taskId, taskName.trim(), false);
        tasks.add(newTask);
        
        fileStorage.saveTasks(tasks);
        return true;
    }
    
    /**
     * Get all tasks from tasks.txt
     * Equivalent to PHP getAllTasks()
     */
    public List<Task> getAllTasks() {
        return fileStorage.loadTasks();
    }
    
    /**
     * Mark/unmark a task as complete
     * Equivalent to PHP markTaskAsCompleted($task_id, $is_completed)
     */
    public boolean markTaskAsCompleted(String taskId, boolean isCompleted) {
        if (taskId == null || taskId.trim().isEmpty()) {
            return false;
        }
        
        List<Task> tasks = fileStorage.loadTasks();
        boolean taskFound = false;
        
        for (Task task : tasks) {
            if (task.getId().equals(taskId)) {
                task.setCompleted(isCompleted);
                taskFound = true;
                break;
            }
        }
        
        if (taskFound) {
            fileStorage.saveTasks(tasks);
            return true;
        }
        
        return false;
    }
    
    /**
     * Delete a task from the list
     * Equivalent to PHP deleteTask($task_id)
     */
    public boolean deleteTask(String taskId) {
        if (taskId == null || taskId.trim().isEmpty()) {
            return false;
        }
        
        List<Task> tasks = fileStorage.loadTasks();
        boolean taskRemoved = tasks.removeIf(task -> task.getId().equals(taskId));
        
        if (taskRemoved) {
            fileStorage.saveTasks(tasks);
            return true;
        }
        
        return false;
    }
    
    /**
     * Get all pending (incomplete) tasks
     * Used for email reminders
     */
    public List<Task> getPendingTasks() {
        return getAllTasks().stream()
                .filter(task -> !task.isCompleted())
                .collect(Collectors.toList());
    }
    
    /**
     * Get task by ID
     */
    public Task getTaskById(String taskId) {
        return getAllTasks().stream()
                .filter(task -> task.getId().equals(taskId))
                .findFirst()
                .orElse(null);
    }
}