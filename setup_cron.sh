#!/bin/bash

# setup_cron.sh - Automatically configure CRON job for Task Scheduler
# This script sets up a CRON job that runs every hour to send task reminders

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Get the current directory (where the script is located)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Java executable path
JAVA_CMD="java"
if [ -n "$JAVA_HOME" ]; then
    JAVA_CMD="$JAVA_HOME/bin/java"
fi

# JAR file path (assuming Maven build creates the JAR in target directory)
JAR_FILE="$PROJECT_ROOT/target/task-scheduler-1.0.0.jar"

# Log file for cron job output
LOG_FILE="$PROJECT_ROOT/logs/cron.log"

# Create logs directory if it doesn't exist
mkdir -p "$PROJECT_ROOT/logs"

print_info "Setting up CRON job for Task Scheduler..."
print_info "Project root: $PROJECT_ROOT"
print_info "JAR file: $JAR_FILE"

# Check if Java is installed
if ! command -v $JAVA_CMD &> /dev/null; then
    print_error "Java is not installed or not found in PATH"
    print_error "Please install Java 17 or later and ensure it's in your PATH"
    exit 1
fi

# Check Java version
JAVA_VERSION=$($JAVA_CMD -version 2>&1 | head -n1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt "17" ]; then
    print_error "Java 17 or later is required. Found version: $JAVA_VERSION"
    exit 1
fi

print_info "Java version check passed: $JAVA_VERSION"

# Check if JAR file exists
if [ ! -f "$JAR_FILE" ]; then
    print_warning "JAR file not found at $JAR_FILE"
    print_info "Building the project with Maven..."
    
    # Check if Maven is installed
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed. Please install Maven and build the project manually:"
        print_error "cd $PROJECT_ROOT && mvn clean package"
        exit 1
    fi
    
    # Build the project
    cd "$PROJECT_ROOT"
    mvn clean package -q
    
    if [ $? -ne 0 ]; then
        print_error "Failed to build the project"
        exit 1
    fi
    
    if [ ! -f "$JAR_FILE" ]; then
        print_error "JAR file still not found after build. Please check the build process."
        exit 1
    fi
    
    print_info "Project built successfully"
fi

# Create a wrapper script for the cron job
CRON_SCRIPT="$PROJECT_ROOT/cron_task_scheduler.sh"

cat > "$CRON_SCRIPT" << EOF
#!/bin/bash
# Auto-generated cron script for Task Scheduler
# This script is called by cron to send task reminders

# Set environment variables
export JAVA_HOME="$JAVA_HOME"
export PATH="$PATH"

# Change to project directory
cd "$PROJECT_ROOT"

# Log the execution
echo "\$(date): Starting task reminder job" >> "$LOG_FILE"

# Run the task scheduler in cron mode (sends reminders and exits)
$JAVA_CMD -cp "$JAR_FILE" com.taskscheduler.CronTask >> "$LOG_FILE" 2>&1

# Log completion
echo "\$(date): Task reminder job completed" >> "$LOG_FILE"
EOF

# Make the cron script executable
chmod +x "$CRON_SCRIPT"

print_info "Created cron wrapper script: $CRON_SCRIPT"

# Create the Java class for cron execution
CRON_JAVA_FILE="$PROJECT_ROOT/src/main/java/com/taskscheduler/CronTask.java"

cat > "$CRON_JAVA_FILE" << 'EOF'
package com.taskscheduler;

import com.taskscheduler.model.Task;
import com.taskscheduler.service.EmailService;
import com.taskscheduler.service.FileStorage;
import com.taskscheduler.service.TaskManager;

import java.util.List;

/**
 * Standalone class for cron job execution
 * This class is called by the cron job to send task reminders
 */
public class CronTask {
    public static void main(String[] args) {
        try {
            System.out.println("Starting task reminder cron job...");
            
            // Initialize services
            FileStorage fileStorage = new FileStorage();
            TaskManager taskManager = new TaskManager(fileStorage);
            EmailService emailService = new EmailService(fileStorage);
            
            // Get pending tasks
            List<Task> pendingTasks = taskManager.getPendingTasks();
            
            if (pendingTasks.isEmpty()) {
                System.out.println("No pending tasks to remind about.");
                return;
            }
            
            System.out.println("Found " + pendingTasks.size() + " pending tasks. Sending reminders...");
            
            // Send reminders
            emailService.sendTaskReminders(pendingTasks);
            
            System.out.println("Task reminder cron job completed successfully.");
            
        } catch (Exception e) {
            System.err.println("Error in cron task: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
EOF

print_info "Created cron task Java class: $CRON_JAVA_FILE"

# Rebuild to include the new CronTask class
print_info "Rebuilding project to include cron task..."
cd "$PROJECT_ROOT"
mvn clean package -q

if [ $? -ne 0 ]; then
    print_error "Failed to rebuild the project"
    exit 1
fi

# Add cron job entry
CRON_JOB="0 * * * * $CRON_SCRIPT"

# Check if cron job already exists
if crontab -l 2>/dev/null | grep -q "$CRON_SCRIPT"; then
    print_warning "Cron job already exists for this script"
    print_info "Existing cron jobs:"
    crontab -l 2>/dev/null | grep "$CRON_SCRIPT"
else
    # Add the cron job
    (crontab -l 2>/dev/null; echo "$CRON_JOB") | crontab -
    
    if [ $? -eq 0 ]; then
        print_info "Cron job added successfully!"
        print_info "Job will run every hour (at minute 0)"
        print_info "Command: $CRON_JOB"
    else
        print_error "Failed to add cron job"
        exit 1
    fi
fi

print_info "Current cron jobs:"
crontab -l 2>/dev/null | grep -v "^#" | grep -v "^$"

print_info ""
print_info "Setup completed successfully!"
print_info "============================================"
print_info "Task Scheduler CRON job is now configured to run every hour"
print_info "Log file: $LOG_FILE"
print_info "Cron script: $CRON_SCRIPT"
print_info "============================================"
print_info ""
print_info "To test the cron job manually, run:"
print_info "$CRON_SCRIPT"
print_info ""
print_info "To remove the cron job, run:"
print_info "crontab -e"
print_info "and delete the line containing: $CRON_SCRIPT"
print_info ""
print_info "To view cron logs, run:"
print_info "tail -f $LOG_FILE"