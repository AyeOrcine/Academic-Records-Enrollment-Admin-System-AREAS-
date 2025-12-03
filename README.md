      AREAS — Academic Records Enrollment Admin System

     ✨✨ AREAS: Academic Records Enrollment Admin System ✨✨
     
A simple console-based Java application for managing student academic records, course enrollment, and basic analytics. Designed as an OOP demo for coursework.

     🌟 Overview
     

AREAS is a Java console program that helps administrators and instructors manage student records and course enrollments. It demonstrates object-oriented concepts (encapsulation, inheritance, polymorphism, abstraction), modular file handling, and a clear CLI menu interface.

Core capabilities:

Add and manage student records

Enroll students in courses and remove enrollments

View student/course lists and search records

Show students with overdue requirements or missing documents

Basic analytics (enrollment counts, students per course)

     📋 Project File Structure
AREAS/
└── SourceCode/
    ├── Student.java
    ├── Course.java
    ├── Enrollment.java
    ├── user.java
    ├── RecordsManager.java
    └── utils/
        ├── FileHandler.java
        └── InputValidator.java


File responsibilities

AREASApp.java – Program entry point; displays menu and handles user interaction.

RecordsManager.java – Manages collections of students, courses, and enrollments (add, remove, list, search).

Student.java – Represents a student record (ID, name, program, year, status, documents).

Course.java – Represents a course (code, title, units, instructor).

Enrollment.java – Links students to courses, stores enrollment date and status.

utils/FileHandler.java – Saves/loads records to disk (CSV or serialized objects).

utils/InputValidator.java – Centralized input validation helpers.

    📁 How to run
    

Open your terminal inside the SourceCode/ folder.

Compile all Java files:

javac SourceCode/*.java


Run the program:

java AREASApp

⚙ Features

Add Student — Create a student record with required details.

Add Course — Add new course metadata.

Enroll Student — Enroll a student into a course.

View Records — Display all students, courses, or enrollments.

Search — Lookup by student ID, course code, or name.

Remove / Update — Remove records or update student/course details.

Alerts — View students missing required documents or with outstanding items.

Analytics — Display counts and percentages (students per course, enrollment trends).

Save / Load — Persist records to disk and load them on startup.

Exit — Exit program gracefully.

🔨 OOP Principles Demonstrated

Encapsulation — Private fields with getters/setters in model classes (Student, Course).

Abstraction — Clear separation of responsibilities (RecordsManager, FileHandler).

Inheritance — (If extended) e.g., UndergraduateStudent and GraduateStudent can inherit from Student.

Polymorphism — Common interfaces or abstract classes used when dealing with different record handlers.

✅ Example CLI Flow
===== AREAS: Academic Records Enrollment Admin System =====
[1] Admin Login

[2] Instructor Login

[3]Student Login

[4] Register Student

[5] Register Instructor

[6] Search Student

[7] View Announcements

[8] Exit


Enter choice:


Sample outputs:

Student added: Juan Dela Cruz (ID: S2025001)

Enrolled S2025001 -> CS101 (Intro to Programming)



👾 Contributors

Project Team — Academic Systems Group
(No personal or identifying contributor information included.)

🫂 Acknowledgments

Thanks to our instructor and classmates for support and feedback. This project is intended for educational demonstration of OOP and basic file I/O in Java.

📝 Notes & Extensions (Ideas)

Add role-based access (admin/instructor) with simple authentication.

Implement CSV/JSON export and import features.

Add reporting (PDF) or a simple GUI (Swing/JavaFX) for better UX.

Expand analytics (enrollment trends by semester, course load per student).
