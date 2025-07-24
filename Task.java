package com.taskscheduler.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Task model class representing a task in the system
 */
public class Task {
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("completed")
    private boolean completed;
    
    // Default constructor for JSON deserialization
    public Task() {}
    
    public Task(String id, String name, boolean completed) {
        this.id = id;
        this.name = name;
        this.completed = completed;
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Task task = (Task) obj;
        return id != null ? id.equals(task.id) : task.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", completed=" + completed +
                '}';
    }
}