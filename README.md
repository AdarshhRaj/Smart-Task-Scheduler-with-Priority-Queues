# Task Scheduler - Java Implementation

A comprehensive Java-based task management system that allows users to add tasks to a common list and subscribe to receive hourly email reminders for pending tasks.


![taskschedulerimage](https://github.com/user-attachments/assets/96145f3e-c42f-405d-bd5e-55cc292eb374)

![task scheduler i9mage](https://github.com/user-attachments/assets/9b7d6b08-a309-40c7-ad74-c1b678a1ef08)


## 🚀 Features

### ✅ Task Management
- Add new tasks to the common list
- Prevent duplicate tasks
- Mark tasks as complete/incomplete
- Delete tasks
- Real-time task statistics
- Persistent storage using JSON text files

### ✅ Email Subscription System
- Email subscription with verification process
- Unique 6-digit verification codes
- HTML email templates
- One-click unsubscribe functionality
- Secure email encoding/decoding

### ✅ Automated Reminder System
- CRON job runs every hour automatically
- Sends HTML emails to verified subscribers
- Only includes pending (incomplete) tasks
- Includes unsubscribe links in all emails

## 📋 Requirements

- **Java 17** or later
- **Maven 3.6+** for building
- **Mailpit** (recommended for local email testing)

## 🛠️ Installation & Setup

### 1. Clone and Build

```bash
# Clone the repository
git clone <repository-url>
cd task-scheduler-java

# Build with Maven
mvn clean package
```

### 2. Configure Email Settings (Optional)

Edit `src/main/java/com/taskscheduler/service/EmailService.java` to configure SMTP settings:

```java
// For local testing with Mailpit (default)
props.put("mail.smtp.host", "localhost");
props.put("mail.smtp.port", "1025");
props.put("mail.smtp.auth", "false");

// For production (Gmail example)
// props.put("mail.smtp.host", "smtp.gmail.com");
// props.put("mail.smtp.port", "587");
// props.put("mail.smtp.auth", "true");
// props.put("mail.smtp.starttls.enable", "true");
```

### 3. Set Up CRON Job

```bash
# Make the setup script executable
chmod +x src/scripts/setup_cron.sh

# Run the CRON setup script
./src/scripts/setup_cron.sh
```

The script will:
- ✅ Check Java installation and version
- ✅ Build the project if needed
- ✅ Create a cron wrapper script
- ✅ Generate the CronTask class
- ✅ Add hourly cron job automatically
- ✅ Set up logging

## 🏃‍♂️ Running the Application

### Standard Mode (Hourly Reminders)
```bash
java -jar target/task-scheduler-1.0.0.jar
```

### Test Mode (5-minute Reminders)
```bash
java -jar target/task-scheduler-1.0.0.jar test
```

### Access the Web Interface
Open your browser and navigate to: **http://localhost:8080**

## 📁 Project Structure

```
task-scheduler-java/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/taskscheduler/
│   │   │       ├── TaskSchedulerApp.java      # Main application
│   │   │       ├── CronTask.java              # Standalone cron task
│   │   │       ├── model/
│   │   │       │   ├── Task.java              # Task model
│   │   │       │   └── PendingSubscription.java # Subscription model
│   │   │       ├── service/
│   │   │       │   ├── TaskManager.java       # Task operations
│   │   │       │   ├── EmailService.java      # Email operations
│   │   │       │   ├── FileStorage.java       # File I/O operations
│   │   │       │   └── CronService.java       # Scheduled reminders
│   │   │       └── web/
│   │   │           └── WebServer.java         # HTTP server
│   │   └── resources/
│   │       ├── static/
│   │       │   └── index.html                 # Web interface
│   │       └── data/                          # Auto-created
│   │           ├── tasks.txt                  # Task storage
│   │           ├── subscribers.txt            # Verified subscribers
│   │           └── pending_subscriptions.txt  # Pending verifications
│   └── scripts/
│       └── setup_cron.sh                      # CRON setup script
├── logs/                                       # Auto-created
│   └── cron.log                               # CRON job logs
├── pom.xml                                     # Maven dependencies
└── README.md
```

## 🎯 API Endpoints

### Task Management
- `GET /api/tasks` - Get all tasks
- `POST /api/tasks` - Add new task
- `PUT /api/tasks` - Update task status
- `DELETE /api/tasks?id={taskId}` - Delete task

### Email Subscription
- `POST /api/subscribe` - Subscribe email
- `GET /verify?email={encoded}&code={code}` - Verify subscription
- `GET /unsubscribe?email={encoded}` - Unsubscribe

## 📊 Data Storage Format

All data is stored in JSON format as specified:

### Tasks (`tasks.txt`)
```json
[
  {
    "id": "unique_task_id",
    "name": "Task Name",
    "completed": false
  }
]
```

### Subscribers (`subscribers.txt`)
```json
["user1@example.com", "user2@example.com"]
```

### Pending Subscriptions (`pending_subscriptions.txt`)
```json
{
  "user1@example.com": {
    "code": "123456",
    "timestamp": 1717694230
  }
}
```

## 📧 Email Templates

### Verification Email
- **Subject:** `Verify subscription to Task Planner`
- **Format:** HTML with verification link

### Task Reminder Email
- **Subject:** `Task Planner - Pending Tasks Reminder`
- **Format:** HTML with task list and unsubscribe link

## 🔧 Key Java Functions

### Task Management (TaskManager.java)
```java
public boolean addTask(String taskName)
public List<Task> getAllTasks()
public boolean markTaskAsCompleted(String taskId, boolean isCompleted)
public boolean deleteTask(String taskId)
```

### Email Operations (EmailService.java)
```java
public String generateVerificationCode()
public boolean subscribeEmail(String email)
public boolean verifySubscription(String email, String code)
public boolean unsubscribeEmail(String email)
public void sendTaskReminders(List<Task> pendingTasks)
public boolean sendTaskEmail(String email, List<Task> pendingTasks)
```

## 🧪 Testing Email Functionality

### Using Mailpit (Recommended)

1. **Install Mailpit:**
   ```bash
   # macOS
   brew install mailpit
   
   # Linux
   wget https://github.com/axllent/mailpit/releases/latest/download/mailpit-linux-amd64.tar.gz
   tar -xzf mailpit-linux-amd64.tar.gz
   sudo mv mailpit /usr/local/bin/
   ```

2. **Start Mailpit:**
   ```bash
   mailpit
   ```

3. **Access Mailpit Web Interface:**
   - Web UI: http://localhost:8025
   - SMTP: localhost:1025

## 🔍 Monitoring & Logs

### View CRON Logs
```bash
tail -f logs/cron.log
```

### Check CRON Jobs
```bash
crontab -l
```

### Manual CRON Test
```bash
./cron_task_scheduler.sh
```

## 🚫 Important Requirements Met

- ✅ **No Database Usage** - Uses JSON text files only
- ✅ **Exact HTML Format** - Follows specified email templates
- ✅ **Working CRON Job** - Automatically configured hourly reminders
- ✅ **Form Element IDs** - Matches required HTML element naming
- ✅ **JSON Data Format** - Follows exact schema specifications
- ✅ **Email Verification** - Complete verification workflow
- ✅ **Duplicate Prevention** - No duplicate tasks allowed
- ✅ **Pure Java** - No external libraries except specified dependencies

## 🛡️ Security Features

- Base64 email encoding in URLs
- Input validation and sanitization
- HTML escaping for XSS prevention
- Secure email verification process

## 📈 Performance

- Efficient file I/O operations
- Concurrent HTTP request handling
- Minimal memory footprint
- Fast JSON serialization/deserialization

## 🔧 Troubleshooting

### Common Issues

1. **Port 8080 already in use:**
   ```bash
   # Find and kill process using port 8080
   lsof -ti:8080 | xargs kill -9
   ```

2. **Java version issues:**
   ```bash
   # Check Java version
   java -version
   
   # Set JAVA_HOME if needed
   export JAVA_HOME=/path/to/java17
   ```

3. **CRON job not running:**
   ```bash
   # Check cron service status
   sudo systemctl status cron
   
   # View cron logs
   grep CRON /var/log/syslog
   ```

4. **Email not sending:**
   - Verify Mailpit is running on port 1025
   - Check email service configuration
   - Review cron.log for errors
 

## 🤝 Contributing

1. Follow Java naming conventions
2. Maintain JSON data format compatibility
3. Ensure all tests pass before submitting
4. Update documentation for new features
