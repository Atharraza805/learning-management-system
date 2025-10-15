# 🎓 Learning Management System (LMS)

A comprehensive Learning Management System built with **Java Swing** and **MySQL** database.

![Java](https://img.shields.io/badge/Java-17+-orange?style=flat&logo=java)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue?style=flat&logo=mysql)
![License](https://img.shields.io/badge/License-MIT-green?style=flat)

## ✨ Features

### 👨‍🎓 For Students
- ✅ Enroll in courses
- ✅ View enrolled courses and grades
- ✅ Submit assignments
- ✅ Track assignment deadlines
- ✅ View course materials

### 👨‍🏫 For Teachers
- ✅ Create and manage courses
- ✅ Create assignments
- ✅ Grade student submissions
- ✅ View enrolled students
- ✅ Track student performance

### 👨‍💼 For Administrators
- ✅ Complete user management (CRUD)
- ✅ Course management
- ✅ System statistics dashboard
- ✅ Role-based access control
- ✅ View all system data

## 🛠️ Technologies Used

| Technology | Purpose |
|------------|---------|
| **Java** | Core programming language |
| **Swing** | GUI framework |
| **MySQL** | Database management |
| **JDBC** | Database connectivity |
| **VS Code** | Development IDE |

## 📦 Installation & Setup

### Prerequisites
- ☕ JDK 17 or higher
- 🐬 MySQL 8.0 or higher
- 🔌 MySQL Connector/J (JDBC Driver)

### Step-by-Step Setup

1. **Clone the repository**
```bash
   git clone https://github.com/YOUR-USERNAME/learning-management-system.git
   cd learning-management-system
```

2. **Set up MySQL database**
   - Open MySQL Workbench
   - Create a new database: `lms_db`
   - Run the database schema (see Database Setup below)

3. **Configure database connection**
   - Open `src/database/DatabaseConnection.java`
   - Update these lines with your MySQL credentials:
```java
     private static final String USER = "root";  // Your MySQL username
     private static final String PASSWORD = "your_password";  // Your MySQL password
```

4. **Download MySQL Connector**
   - Download from: [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/)
   - Extract and place the `.jar` file in the `lib/` folder

5. **Compile the project**
```bash
   # Windows
   javac -d bin -cp "lib/*" src/database/*.java src/ui/*.java src/*.java
   
   # Mac/Linux
   javac -d bin -cp "lib/*" src/database/*.java src/ui/*.java src/*.java
```

6. **Run the application**
```bash
   # Windows
   java -cp "bin;lib/*" Main
   
   # Mac/Linux
   java -cp "bin:lib/*" Main
```

## 🗄️ Database Setup

Run these SQL commands in MySQL:
```sql
CREATE DATABASE lms_db;
USE lms_db;

-- Create users table
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    role ENUM('admin', 'teacher', 'student') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create courses table
CREATE TABLE courses (
    course_id INT PRIMARY KEY AUTO_INCREMENT,
    course_name VARCHAR(100) NOT NULL,
    course_code VARCHAR(20) UNIQUE NOT NULL,
    description TEXT,
    teacher_id INT,
    credits INT,
    FOREIGN KEY (teacher_id) REFERENCES users(user_id)
);

-- Create enrollments table
CREATE TABLE enrollments (
    enrollment_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT,
    course_id INT,
    enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    grade VARCHAR(5),
    FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE
);

-- Create assignments table
CREATE TABLE assignments (
    assignment_id INT PRIMARY KEY AUTO_INCREMENT,
    course_id INT,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    due_date DATE,
    max_marks INT,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE
);

-- Create submissions table
CREATE TABLE submissions (
    submission_id INT PRIMARY KEY AUTO_INCREMENT,
    assignment_id INT,
    student_id INT,
    submission_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    marks_obtained INT,
    feedback TEXT,
    FOREIGN KEY (assignment_id) REFERENCES assignments(assignment_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Insert sample data
INSERT INTO users (username, password, full_name, email, role) VALUES
('admin', 'admin123', 'Admin User', 'admin@lms.com', 'admin'),
('teacher1', 'teacher123', 'John Doe', 'john@lms.com', 'teacher'),
('student1', 'student123', 'Jane Smith', 'jane@lms.com', 'student');
```

## 🔑 Default Login Credentials

| Role | Username | Password |
|------|----------|----------|
| **Admin** | `admin` | `admin123` |
| **Teacher** | `teacher1` | `teacher123` |
| **Student** | `student1` | `student123` |

⚠️ **Note:** Change these credentials after first login!

## 📂 Project Structure
```
learning-management-system/
├── src/
│   ├── database/
│   │   └── DatabaseConnection.java    # Database connection manager
│   ├── ui/
│   │   ├── LoginFrame.java           # Login interface
│   │   ├── StudentDashboard.java     # Student interface
│   │   ├── TeacherDashboard.java     # Teacher interface
│   │   └── AdminDashboard.java       # Admin interface
│   └── Main.java                     # Application entry point
├── lib/
│   └── mysql-connector-java.jar      # JDBC driver
├── .gitignore
├── LICENSE
└── README.md
```

## 🎯 Key Features Implemented

- ✅ **Authentication System**: Role-based login with password verification
- ✅ **Modern GUI**: Clean interface with color-coded dashboards
- ✅ **Database Integration**: Full CRUD operations using JDBC
- ✅ **Course Management**: Create, edit, and delete courses
- ✅ **Assignment System**: Create assignments and submit solutions
- ✅ **Grading System**: Teachers can grade student submissions
- ✅ **Statistics Dashboard**: Real-time system statistics for admins
- ✅ **Data Integrity**: Cascade delete for maintaining referential integrity

  ## 🔮 Future Enhancements

- [ ] Messaging system between users
- [ ] PDF report generation
- [ ] Email notifications
- [ ] Attendance tracking
- [ ] Discussion forums
- [ ] Real-time notifications

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Java Swing Documentation
- MySQL Documentation
- Stack Overflow Community
- Claude AI for guidance

---
