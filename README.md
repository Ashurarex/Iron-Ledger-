# 🏋️ Iron Ledger

> An intelligent desktop fitness tracking application that combines powerful workout logging with AI-driven progression guidance.

[![Java](https://img.shields.io/badge/Java-11+-orange?style=flat-square&logo=java)](https://www.java.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14+-blue?style=flat-square&logo=postgresql)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)](LICENSE)
[![Status](https://img.shields.io/badge/Status-Active%20Development-brightgreen?style=flat-square)](https://github.com/Ashurarex/Iron-Ledger-)

---

## 📋 Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Installation](#installation)
- [Usage](#usage)
- [Project Structure](#project-structure)
- [Database Schema](#database-schema)
- [Core Modules](#core-modules)
- [UI/UX Components](#uiux-components)
- [Performance Optimizations](#performance-optimizations)
- [Known Issues](#known-issues)
- [Roadmap](#roadmap)
- [Contributing](#contributing)

---

## 🎯 Overview

Iron Ledger is a **desktop fitness tracking application** designed to help users log workouts, track progress, and receive intelligent progression recommendations. Built with Java Swing for a native desktop experience, it integrates a layered architecture with PostgreSQL backend to provide a robust, scalable solution for serious lifters.

Unlike basic workout loggers, Iron Ledger includes an **Auto Progressive Overload Engine** that analyzes your training data and suggests optimal weight increases based on your performance trends—transforming it from a simple tracker into an **intelligent training assistant**.

---

## ✨ Key Features

### 🔐 Authentication & Security
- Secure user registration and login
- **BCrypt password hashing** with industry-standard encryption
- Session management system
- User-specific data isolation

### 📝 Workout Logging System
- Create and organize custom workouts
- Log multiple exercises per workout
- Track multiple sets with reps and weight
- Session preview before saving
- Real-time data persistence verification

### 💪 Exercise Library
- Database-backed exercise catalog (not hardcoded)
- Filter by muscle groups
- Equipment-based categorization
- Difficulty levels
- Dynamic dropdown population

### 📊 Advanced Analytics
- **Strength Progression Tracking** (line graphs)
- **Volume Analytics** (bar charts)
- **Personal Records** (max weight per exercise)
- Custom GraphPanel using Graphics2D
- Visual performance insights

### 🤖 Auto Progressive Overload Engine
- Analyzes last 3–5 training sessions
- Calculates average & max weight, total volume
- Detects progression, plateau, and regression trends
- **Intelligent weight suggestions** (2.5%–5% increases)
- Prevents unsafe jumps (>10% threshold)
- Actionable recommendations with reasoning

### 🎨 Modern UI/UX
- Dark & light theme support
- Material Design-inspired components
- Smooth animations & transitions
- Floating label inputs with focus states
- Toast notifications & inline feedback
- Loading and empty states
- Responsive CardLayout-based navigation

### ⚡ Performance First
- Non-blocking database operations (SwingWorker)
- Exercise data caching
- Optimized repaint scope
- No EDT thread blocking
- Minimal layout recalculation

---

## 🏗️ Architecture

Iron Ledger follows a **clean, layered architecture** for maintainability and scalability:

```
┌─────────────────────────────────────┐
│         UI Layer (Swing)            │
│  Panels, MainFrame, CardLayout      │
├─────────────────────────────────────┤
│       Service Layer                 │
│  Business Logic & Analytics         │
├─────────────────────────────────────┤
│     Repository Layer (JDBC)         │
│  Database queries, PreparedStatements│
├─────────────────────────────────────┤
│  Model Layer (Domain Objects)       │
│  Users, Workouts, Exercises, Logs   │
├─────────────────────────────────────┤
│  Config & Utils                     │
│  DB Connection, Theme, Security     │
└─────────────────────────────────────┘
```

### Layer Responsibilities

| Layer | Purpose | Key Classes |
|-------|---------|------------|
| **UI** | User interface & navigation | `MainFrame`, `DashboardPanel`, `WorkoutLoggerPanel`, `AnalyticsPanel` |
| **Service** | Business logic & calculations | `WorkoutService`, `AnalyticsService`, `ProgressionEngine` |
| **Repository** | Database access | `UserRepository`, `WorkoutRepository`, `ExerciseRepository` |
| **Model** | Domain objects | `User`, `Workout`, `Exercise`, `WorkoutLog` |
| **Config/Utils** | Infrastructure | `DatabaseConfig`, `ThemeManager`, `SessionManager`, `Animator` |

---

## 🛠️ Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Language** | Java | 11+ |
| **UI Framework** | Swing | JDK Built-in |
| **Database** | PostgreSQL | 14+ |
| **Database Driver** | JDBC | PostgreSQL Driver |
| **Security** | BCrypt | Latest |
| **Hosting** | Supabase | PostgreSQL |
| **Build Tool** | Maven/Gradle | (Configure as needed) |

---

## 📦 Installation

### Prerequisites

- **Java 11 or higher** ([Download](https://www.oracle.com/java/technologies/downloads/))
- **PostgreSQL 14+** or **Supabase account** ([Sign up](https://supabase.com/))
- **Maven** (optional, for dependency management)

### Step 1: Clone the Repository

```bash
git clone https://github.com/Ashurarex/Iron-Ledger-.git
cd Iron-Ledger-
```

### Step 2: Set Up Database

1. Create a PostgreSQL database (or use Supabase):
   ```sql
   CREATE DATABASE iron_ledger;
   ```

2. Initialize the database schema:
   ```sql
   -- Users table
   CREATE TABLE users (
       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
       name VARCHAR(255) NOT NULL,
       email VARCHAR(255) UNIQUE NOT NULL,
       password_hash VARCHAR(255) NOT NULL,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       is_active BOOLEAN DEFAULT true
   );

   -- Exercises table
   CREATE TABLE exercises (
       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
       name VARCHAR(255) NOT NULL,
       muscle_group VARCHAR(100) NOT NULL,
       equipment VARCHAR(100),
       difficulty VARCHAR(50)
   );

   -- Workouts table
   CREATE TABLE workouts (
       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
       user_id UUID NOT NULL REFERENCES users(id),
       name VARCHAR(255) NOT NULL,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   );

   -- Workout logs table
   CREATE TABLE workout_logs (
       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
       workout_id UUID NOT NULL REFERENCES workouts(id),
       exercise_id UUID NOT NULL REFERENCES exercises(id),
       set_number INT NOT NULL,
       reps INT NOT NULL,
       weight FLOAT NOT NULL
   );
   ```

### Step 3: Configure Database Connection

Update `DatabaseConfig.java` (or your config file) with your credentials:

```java
public class DatabaseConfig {
    private static final String URL = "jdbc:postgresql://your-host:5432/iron_ledger";
    private static final String USER = "your_username";
    private static final String PASSWORD = "your_password";
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
```

### Step 4: Install Dependencies

If using Maven:
```bash
mvn clean install
```

If using Gradle:
```bash
gradle build
```

### Step 5: Run the Application

```bash
java -cp target/iron-ledger.jar com.ironledger.ui.MainFrame
```

Or compile and run directly:
```bash
javac -d bin src/**/*.java
java -cp bin:lib/* com.ironledger.ui.MainFrame
```

---

## 🚀 Usage

### 1. Register & Login
- Launch the app and create a new account
- Secure login with BCrypt-hashed passwords
- Session automatically maintained

### 2. Log a Workout
- Navigate to **"Log Workout"**
- Select or create a workout
- Add exercises from the database
- Log sets, reps, and weight
- Preview before saving
- Data instantly persists to database

### 3. View Workout History
- Go to **"Workout History"**
- Browse all past workouts
- Click to view detailed logs
- Track your training timeline

### 4. Analyze Performance
- Navigate to **"Analytics"**
- View strength progression graphs
- Monitor volume trends
- Check personal records
- Identify patterns in your data

### 5. Get Progression Recommendations
- After logging a set, receive intelligent suggestions
- System analyzes your last 3–5 sessions
- Get recommended weight increases (2.5%–5%)
- Understand the reasoning behind suggestions
- Avoid plateaus with data-driven guidance

### 6. Manage Exercise Library
- Access **"Exercise Library"**
- Browse all exercises
- Filter by muscle group
- View exercise details
- (Admin: Add custom exercises)

---

## 📁 Project Structure

```
Iron-Ledger/
├── src/
│   ├── com/ironledger/
│   │   ├── ui/
│   │   │   ├── MainFrame.java
│   │   │   ├── panels/
│   │   │   │   ├── DashboardPanel.java
│   │   │   │   ├── WorkoutLoggerPanel.java
│   │   │   │   ├── WorkoutHistoryPanel.java
│   │   │   │   ├── AnalyticsPanel.java
│   │   │   │   └── ExerciseLibraryPanel.java
│   │   │   ├── components/
│   │   │   │   ├── PrimaryButton.java
│   │   │   │   ├── MaterialTextField.java
│   │   │   │   ├── CardPanel.java
│   │   │   │   └── GraphPanel.java
│   │   │   └── utils/
│   │   │       ├── ThemeManager.java
│   │   │       ├── Typography.java
│   │   │       └── Animator.java
│   │   │
│   │   ├── service/
│   │   │   ├── WorkoutService.java
│   │   │   ├── AnalyticsService.java
│   │   │   ├── ProgressionEngine.java
│   │   │   ├── ExerciseService.java
│   │   │   └── AuthService.java
│   │   │
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   ├── WorkoutRepository.java
│   │   │   ├── ExerciseRepository.java
│   │   │   └── WorkoutLogRepository.java
│   │   │
│   │   ├── model/
│   │   │   ├── User.java
│   │   │   ├── Workout.java
│   │   │   ├── Exercise.java
│   │   │   ├── WorkoutLog.java
│   │   │   └── ProgressionSuggestion.java
│   │   │
│   │   └── config/
│   │       ├── DatabaseConfig.java
│   │       └── SessionManager.java
│   │
│   └── resources/
│       └── (icons, themes, assets)
│
├── db/
│   └── schema.sql
│
├── pom.xml (or build.gradle)
└── README.md
```

---

## 🗄️ Database Schema

### Users Table
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT true
);
```

### Exercises Table
```sql
CREATE TABLE exercises (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    muscle_group VARCHAR(100) NOT NULL,
    equipment VARCHAR(100),
    difficulty VARCHAR(50)
);
```

### Workouts Table
```sql
CREATE TABLE workouts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Workout Logs Table
```sql
CREATE TABLE workout_logs (
    id UUID PRIMARY KEY,
    workout_id UUID NOT NULL REFERENCES workouts(id),
    exercise_id UUID NOT NULL REFERENCES exercises(id),
    set_number INT NOT NULL,
    reps INT NOT NULL,
    weight FLOAT NOT NULL
);
```

---

## 🎯 Core Modules

### 1. Authentication Module
- **Secure registration** with email validation
- **BCrypt hashing** for passwords (preventing plaintext storage)
- **Session management** via SessionManager singleton
- User-specific data isolation

### 2. Workout Logging System
- Create custom workouts
- Add multiple exercises per workout
- Log multiple sets (set_number, reps, weight)
- Session preview before database commit
- UUID-based tracking for consistency

### 3. Analytics Engine
- **Strength Progression**: Track weight increases over time
- **Volume Analytics**: Monitor total reps × weight per session
- **Personal Records**: Find max weight per exercise
- **Custom Graphics2D** rendering for performance graphs
- Time-series analysis and trend detection

### 4. Auto Progressive Overload Engine
Analyzes training history to suggest optimal progression:

**Algorithm:**
```
1. Fetch last 3–5 workout sessions
2. Calculate metrics:
   - Average weight per exercise
   - Maximum weight achieved
   - Total volume (reps × weight)
3. Detect trend:
   - Progression (weight/volume increasing)
   - Plateau (metrics stable)
   - Regression (metrics decreasing)
4. Generate suggestion:
   - If progressing: increase 2.5%–5%
   - If plateau: increase reps or weight
   - If regressing: reduce weight, maintain reps
5. Apply constraints:
   - Cap max increase at 10%
   - Round to nearest plate (2.5 lb increments)
   - Never suggest unsafe jumps
6. Return: { suggestedWeight, reason, trend }
```

---

## 🎨 UI/UX Components

### Design System

**Typography:**
- Centralized font definitions
- Consistent sizes (Title, Body, Caption)
- Dark & light mode support

**Theme Manager:**
- Dark mode (default)
- Light mode
- Recursive theme application
- No hardcoded colors

**Reusable Components:**
- `PrimaryButton` - Styled action buttons
- `MaterialTextField` - Floating label inputs
- `CardPanel` - Elevated container with shadow
- `GlassPanel` - Frosted glass effect
- `GraphPanel` - Custom graphics rendering

### Navigation
- **MainFrame** with CardLayout
- Sidebar navigation menu
- Smooth screen transitions
- No popup windows (inline feedback)

### Animations
- **Fade transitions** between panels
- **Slide animations** for elements
- **Hover feedback** on buttons
- **Focus animations** on inputs
- **Easing functions** (easeOut, easeInOut)
- Optimized repaint (no full-frame refresh)

### Feedback Systems
- **Toast notifications** for confirmations
- **Inline error messages** (no modal dialogs)
- **Loading states** for async operations
- **Empty states** with helpful guidance
- **Disabled button states** (visual feedback)

---

## ⚡ Performance Optimizations

### Database Operations
- **SwingWorker** for non-blocking DB calls
- **PreparedStatements** only (SQL injection prevention)
- **Connection pooling** (future enhancement)
- **Exercise caching** to reduce queries

### UI Rendering
- **Limited repaint scope** (avoid full-frame repaints)
- **Minimal layout recalculation**
- **Graphics2D caching** for graphs
- **Smart EDT scheduling** (no thread blocking)

### Memory Management
- **UUID-based object tracking** (no memory leaks)
- **Efficient session storage**
- **Resource cleanup** on panel disposal

### Results
- **Smooth animations** (no lag)
- **Responsive UI** (instant feedback)
- **Fast database queries** (sub-100ms)
- **Minimal memory footprint**

---

## 🐛 Known Issues

| Issue | Status | Workaround |
|-------|--------|-----------|
| Material input animation edge cases | Minor | Reload panel if animation stutters |
| Swing rendering limitations | Minor | Use system theme for best results |
| Advanced blur effects | Not implemented | Consider future enhancement |
| Dropdown renderer timing | Fixed | Exercises now load reliably |
| WorkoutId consistency | Fixed | Session tracking verified |

---

## 🗺️ Roadmap

### Phase 6: Periodization & Advanced Training
- [ ] Workout periodization planner
- [ ] Periodization templates (PPL, Upper/Lower, Full-Body)
- [ ] Periodization calendar visualizer

### Phase 7: Fatigue & Recovery
- [ ] Fatigue detection algorithm
- [ ] Recovery tracking
- [ ] Deload recommendations
- [ ] RPE (Rate of Perceived Exertion) logging

### Phase 8: AI & Recommendations
- [ ] AI-based workout recommendations
- [ ] Personalized training programs
- [ ] Form cues & exercise tutorials
- [ ] Injury prevention insights

### Phase 9: Cloud & Sync
- [ ] Cloud data synchronization
- [ ] Multi-device support
- [ ] Backup & restore

### Phase 10: Mobile
- [ ] Flutter/React Native mobile app
- [ ] Mobile-specific UI optimizations
- [ ] Push notifications
- [ ] Offline-first sync

---

## 🤝 Contributing

Contributions are welcome! Here's how to get started:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/AmazingFeature`)
3. **Commit** your changes (`git commit -m 'Add AmazingFeature'`)
4. **Push** to the branch (`git push origin feature/AmazingFeature`)
5. **Open** a Pull Request

### Contribution Guidelines
- Follow existing code style
- Add tests for new features
- Update documentation
- Keep commits atomic and descriptive

---

## 📝 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

---

## 👤 Author

**Raghavendra (Ashurarex)**
- GitHub: [@Ashurarex](https://github.com/Ashurarex)
- Project: [Iron Ledger](https://github.com/Ashurarex/Iron-Ledger-)

---

## 🙏 Acknowledgments

- PostgreSQL & Supabase for reliable database hosting
- Java Swing community for UI inspiration
- BCrypt for security best practices
- All contributors and users

---

## 📞 Support

Have questions? Issues? Feature requests?

- **Open an Issue**: [GitHub Issues](https://github.com/Ashurarex/Iron-Ledger-/issues)
- **Discussions**: [GitHub Discussions](https://github.com/Ashurarex/Iron-Ledger-/discussions)

---

<div align="center">

**Built with 💪 and ☕ by Raghavendra**

⭐ Star this project if it helps you! ⭐

</div>
