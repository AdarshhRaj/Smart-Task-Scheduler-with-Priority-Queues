package com.taskscheduler.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.taskscheduler.model.Task;
import com.taskscheduler.service.EmailService;
import com.taskscheduler.service.TaskManager;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP Server for handling web interface requests
 */
public class WebServer {
    private final TaskManager taskManager;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;
    private HttpServer server;
    
    public WebServer(TaskManager taskManager, EmailService emailService) {
        this.taskManager = taskManager;
        this.emailService = emailService;
        this.objectMapper = new ObjectMapper();
}
    
    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Static file serving
        server.createContext("/", new StaticFileHandler());
        
        // API endpoints
        server.createContext("/api/tasks", new TaskHandler());
        server.createContext("/api/subscribe", new SubscribeHandler());
        server.createContext("/verify", new VerifyHandler());  
        server.createContext("/unsubscribe", new UnsubscribeHandler());
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("Task Scheduler server started on http://localhost:" + port);
    }
    
    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }
    
    // Static file handler for serving HTML, CSS, JS
    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            if (path.equals("/")) {
                path = "/index.html";
            }
            
            String filePath = "src/main/resources/static" + path;
            File file = new File(filePath);
            
            if (file.exists() && !file.isDirectory()) {
                String contentType = getContentType(path);
                exchange.getResponseHeaders().set("Content-Type", contentType);
                
                byte[] fileContent = Files.readAllBytes(file.toPath());
                exchange.sendResponseHeaders(200, fileContent.length);
                
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(fileContent);
                }
            } else {
                // Return 404
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
        
        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html";
            if (path.endsWith(".css")) return "text/css";
            if (path.endsWith(".js")) return "application/javascript";
            if (path.endsWith(".json")) return "application/json";
            return "text/plain";
        }
    }
    
    // Task API handler
    private class TaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            
            switch (method) {
                case "GET":
                    handleGetTasks(exchange);
                    break;
                case "POST":
                    handleAddTask(exchange);
                    break;
                case "PUT":
                    handleUpdateTask(exchange);
                    break;
                case "DELETE":
                    handleDeleteTask(exchange);
                    break;
                default:
                    sendResponse(exchange, 405, "Method not allowed");
            }
        }
        
        private void handleGetTasks(HttpExchange exchange) throws IOException {
            List<Task> tasks = taskManager.getAllTasks();
            String json = objectMapper.writeValueAsString(tasks);
            
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            sendResponse(exchange, 200, json);
        }
        
        private void handleAddTask(HttpExchange exchange) throws IOException {
            String requestBody = readRequestBody(exchange);
            Map<String, String> params = parseFormData(requestBody);
            
            String taskName = params.get("task-name");
            if (taskName != null && !taskName.trim().isEmpty()) {
                boolean success = taskManager.addTask(taskName);
                if (success) {
                    sendResponse(exchange, 200, "{\"success\": true}");
                } else {
                    sendResponse(exchange, 400, "{\"success\": false, \"error\": \"Task already exists\"}");
                }
            } else {
                sendResponse(exchange, 400, "{\"success\": false, \"error\": \"Task name is required\"}");
            }
        }
        
        private void handleUpdateTask(HttpExchange exchange) throws IOException {
            String requestBody = readRequestBody(exchange);
            Map<String, String> params = parseJsonData(requestBody);
            
            String taskId = params.get("id");
            String completedStr = params.get("completed");
            
            if (taskId != null && completedStr != null) {
                boolean completed = Boolean.parseBoolean(completedStr);
                boolean success = taskManager.markTaskAsCompleted(taskId, completed);
                
                if (success) {
                    sendResponse(exchange, 200, "{\"success\": true}");
                } else {
                    sendResponse(exchange, 404, "{\"success\": false, \"error\": \"Task not found\"}");
                }
            } else {
                sendResponse(exchange, 400, "{\"success\": false, \"error\": \"Invalid parameters\"}");
            }
        }
        
        private void handleDeleteTask(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQueryString(query);
            
            String taskId = params.get("id");
            if (taskId != null) {
                boolean success = taskManager.deleteTask(taskId);
                
                if (success) {
                    sendResponse(exchange, 200, "{\"success\": true}");
                } else {
                    sendResponse(exchange, 404, "{\"success\": false, \"error\": \"Task not found\"}");
                }
            } else {
                sendResponse(exchange, 400, "{\"success\": false, \"error\": \"Task ID is required\"}");
            }
        }
    }
    
    // Email subscription handler
    private class SubscribeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method not allowed");
                return;
            }
            
            String requestBody = readRequestBody(exchange);
            Map<String, String> params = parseFormData(requestBody);
            
            String email = params.get("email");
            if (email != null && !email.trim().isEmpty()) {
                boolean success = emailService.subscribeEmail(email);
                
                if (success) {
                    sendResponse(exchange, 200, "{\"success\": true, \"message\": \"Verification email sent\"}");
                } else {
                    sendResponse(exchange, 400, "{\"success\": false, \"error\": \"Invalid email or already subscribed\"}");
                }
            } else {
                sendResponse(exchange, 400, "{\"success\": false, \"error\": \"Email is required\"}");
            }
        }
    }
    
    // Email verification handler
    private class VerifyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQueryString(query);
            
            String encodedEmail = params.get("email");
            String code = params.get("code");
            
            if (encodedEmail != null && code != null) {
                try {
                    String email = new String(Base64.getDecoder().decode(encodedEmail));
                    boolean success = emailService.verifySubscription(email, code);
                    
                    String response = success ? 
                        "<html><body><h2>Subscription Verified!</h2><p>You will now receive task reminders.</p></body></html>" :
                        "<html><body><h2>Verification Failed</h2><p>Invalid verification link.</p></body></html>";
                    
                    exchange.getResponseHeaders().set("Content-Type", "text/html");
                    sendResponse(exchange, success ? 200 : 400, response);
                    
                } catch (IllegalArgumentException e) {
                    sendResponse(exchange, 400, "<html><body><h2>Invalid Link</h2></body></html>");
                }
            } else {
                sendResponse(exchange, 400, "<html><body><h2>Missing Parameters</h2></body></html>");
            }
        }
    }
    
    // Unsubscribe handler
    private class UnsubscribeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQueryString(query);
            
            String encodedEmail = params.get("email");
            
            if (encodedEmail != null) {
                try {
                    String email = new String(Base64.getDecoder().decode(encodedEmail));
                    boolean success = emailService.unsubscribeEmail(email);
                    
                    String response = success ?
                        "<html><body><h2>Unsubscribed</h2><p>You have been unsubscribed from task reminders.</p></body></html>" :
                        "<html><body><h2>Error</h2><p>Unable to unsubscribe.</p></body></html>";
                    
                    exchange.getResponseHeaders().set("Content-Type", "text/html");
                    sendResponse(exchange, success ? 200 : 400, response);
                    
                } catch (IllegalArgumentException e) {
                    sendResponse(exchange, 400, "<html><body><h2>Invalid Link</h2></body></html>");
                }
            } else {
                sendResponse(exchange, 400, "<html><body><h2>Missing Parameters</h2></body></html>");
            }
        }
    }
    
    // Helper methods
    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
            return body.toString();
        }
    }
    
    private Map<String, String> parseFormData(String formData) {
        Map<String, String> params = new HashMap<>();
        if (formData != null && !formData.isEmpty()) {
            String[] pairs = formData.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    try {
                        String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                        String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                        params.put(key, value);
                    } catch (Exception e) {
                        // Skip invalid pairs
                    }
                }
            }
        }
        return params;
    }
    
    private Map<String, String> parseJsonData(String jsonData) {
        try {
            return objectMapper.readValue(jsonData, Map.class);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
    
    private Map<String, String> parseQueryString(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    try {
                        String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                        String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                        params.put(key, value);
                    } catch (Exception e) {
                        // Skip invalid pairs
                    }
                }
            }
        }
        return params;
    }
    
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }