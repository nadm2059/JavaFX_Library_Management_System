Here’s your **README** in proper Markdown format ready for GitHub:

```markdown
# Smart Library Management System

A **Java-based Library Management System** with **CLI and JavaFX GUI** support, featuring user roles, book catalog management, borrowing/returning functionality, a recommendation engine, and CSV-based persistence.

---

## Features

### User Roles
- **Student**: Can borrow up to 3 books.
- **Teacher**: Can borrow up to 5 books.
- **Admin**: Can borrow up to 10 books and manage the library (add/remove books, register users).

### Functionality
- **Search books** by title, author, or genre.
- **Borrow and return books**.
- **Recommendations** based on user borrowing history and book popularity.
- **Persistence**: Saves and loads library data from CSV files in `data/` folder.
- **CLI Mode**: Command-line interface for all operations.
- **JavaFX GUI Mode**: Modern graphical interface with list views, search, borrow/return buttons, and logs.

### Persistence
Stores library data in `data/` folder with the following CSV files:
- `books.csv` — Stores book info (`isbn,title,author,genre,isAvailable`)
- `users.csv` — Stores user info (`id,type,name`)
- `loans.csv` — Current loans (`userId,isbn`)
- `metrics.csv` — Borrow counts for recommendations (`isbn,count`)

---

## Project Structure

```

SmartLibrarySystem/
├── Main.java                  # CLI entry point
├── module-info.java           # Java module configuration
├── models/                    # Domain models
│   ├── Book.java
│   ├── User.java
│   ├── Student.java
│   ├── Teacher.java
│   └── Admin.java
├── services/                  # Library services
│   ├── Library.java
│   ├── RecommendationEngine.java
│   └── FileDatabase.java
└── ui/                        # JavaFX GUI
└── LibraryApp.java

````

---

## Requirements

- **Java JDK 17+** (supports JavaFX)
- **JavaFX SDK 17+** (or newer, e.g., JavaFX 24)
- Command-line environment (Windows, macOS, Linux)
- Optional: IDE like **VS Code** or **IntelliJ IDEA**

---

## Build & Run Instructions

### 1. Compile (Windows example)

```cmd
cd C:\javafxlib
mkdir out
javac --module-path "C:\path\to\javafx-sdk-17.0.16\lib" --add-modules javafx.controls,javafx.graphics -d out ^
Main.java module-info.java models\*.java services\*.java ui\*.java
````

> Replace `"C:\path\to\javafx-sdk-17.0.16\lib"` with your actual JavaFX SDK path.

---

### 2. Run CLI

```cmd
java -cp out Main
```

* Follow prompts:

  * **1)** User Mode
  * **2)** Admin Mode
  * **3)** Save
  * **4)** Exit

* Example seeded IDs:

  * Admin: `A001`
  * Student: `S001`
  * Teacher: `T001`

---

### 3. Run GUI

```cmd
java --module-path "C:\path\to\javafx-sdk-17.0.16\lib" --add-modules javafx.controls,javafx.graphics -cp out ui.LibraryApp
```

* Opens JavaFX window with list of books, search bar, borrow/return buttons, and log area.
* User can select a user from the drop-down to interact with books.

---

## Sample Data

The system seeds default users and books if the data folder is empty:

**Users**

```
S001, Student, Alice
T001, Teacher, Dr. Bob
A001, Admin, Charlie (Admin)
```

**Books**

* Effective Java (Programming)
* Clean Code (Programming)
* Design Patterns (Programming)
* Dune (Science Fiction)
* Neuromancer (Science Fiction)
* Sapiens (History)
* Educated (Memoir)
* The Hobbit (Fantasy)
* Atomic Habits (Self-Help)

---

## Notes

* **Persistence:** All changes are saved to `data/` using CSV. Make sure `data/` exists or is created automatically.
* **Admin Access:** Only users of type Admin can add/remove books or register new users.
* **Recommendations:** Generated using user borrowing history and book popularity.

---

## License

MIT License – free to use, modify, and distribute.

```


