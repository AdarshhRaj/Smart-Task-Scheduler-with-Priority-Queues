package com.taskscheduler.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskscheduler.model.PendingSubscription;
import com.taskscheduler.model.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for handling file storage operations
 */
public class FileStorage {
    private static final String DATA_DIR = "src/main/resources/data/";
    private static final String TASKS_FILE = DATA_DIR + "tasks.txt";
    private static final String SUBSCRIBERS_FILE = DATA_DIR + "subscribers.txt";
    private static final String PENDING_SUBSCRIPTIONS_FILE = DATA_DIR + "pending_subscriptions.txt";
    
    private final ObjectMapper objectMapper;
    
    public FileStorage() {
        this.objectMapper = new ObjectMapper();
        initializeDataDirectory();
    }
    
    private void initializeDataDirectory() {
        try {
            Path dataPath = Paths.get(DATA_DIR);
            if (!Files.exists(dataPath)) {
                Files.createDirectories(dataPath);
            }
            
            // Initialize files if they don't exist
            initializeFileIfNotExists(TASKS_FILE, "[]");
            initializeFileIfNotExists(SUBSCRIBERS_FILE, "[]");
            initializeFileIfNotExists(PENDING_SUBSCRIPTIONS_FILE, "{}");
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize data directory", e);
        }
    }
    
    private void initializeFileIfNotExists(String filePath, String defaultContent) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            Files.write(Paths.get(filePath), defaultContent.getBytes());
        }
    }
    
    // Task operations
    public List<Task> loadTasks() {
        try {
            String content = Files.readString(Paths.get(TASKS_FILE));
            if (content.trim().isEmpty()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(content, new TypeReference<List<Task>>() {});
        } catch (IOException e) {
            System.err.println("Error loading tasks: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public void saveTasks(List<Task> tasks) {
        try {
            String json = objectMapper.writeValueAsString(tasks);
            Files.write(Paths.get(TASKS_FILE), json.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save tasks", e);
        }
    }
    
    // Subscriber operations
    public List<String> loadSubscribers() {
        try {
            String content = Files.readString(Paths.get(SUBSCRIBERS_FILE));
            if (content.trim().isEmpty()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(content, new TypeReference<List<String>>() {});
        } catch (IOException e) {
            System.err.println("Error loading subscribers: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public void saveSubscribers(List<String> subscribers) {
        try {
            String json = objectMapper.writeValueAsString(subscribers);
            Files.write(Paths.get(SUBSCRIBERS_FILE), json.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save subscribers", e);
        }
    }
    
    // Pending subscription operations
    public Map<String, PendingSubscription> loadPendingSubscriptions() {
        try {
            String content = Files.readString(Paths.get(PENDING_SUBSCRIPTIONS_FILE));
            if (content.trim().isEmpty()) {
                return new HashMap<>();
            }
            return objectMapper.readValue(content, new TypeReference<Map<String, PendingSubscription>>() {});
        } catch (IOException e) {
            System.err.println("Error loading pending subscriptions: " + e.getMessage());
            return new HashMap<>();
        }
    }
    
    public void savePendingSubscriptions(Map<String, PendingSubscription> pendingSubscriptions) {
        try {
            String json = objectMapper.writeValueAsString(pendingSubscriptions);
            Files.write(Paths.get(PENDING_SUBSCRIPTIONS_FILE), json.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save pending subscriptions", e);
        }
    }
}