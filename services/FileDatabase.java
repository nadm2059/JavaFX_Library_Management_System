package services;

import models.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Minimal CSV-based persistence (no external libs).
 * Files:
 *  - data/books.csv      => isbn,title,author,genre,isAvailable
 *  - data/users.csv      => id,type,name
 *  - data/loans.csv      => userId,isbn   (current loans only)
 *  - data/metrics.csv    => isbn,borrowCount
 */
public class FileDatabase {
    private final Path dataDir;

    public FileDatabase(String directory) { this.dataDir = Paths.get(directory); }

    // --- Public API used by Library ---
    public void loadInto(Library library) {
        try {
            ensureDir();
            Map<String, Book> books = loadBooks();
            books.values().forEach(library::addBook);

            Map<String, User> users = loadUsers();
            users.values().forEach(library::registerUser);

            // restore borrow counts & availability first
            Map<String, Integer> metrics = loadMetrics();
            library.getBorrowCountsInternal().putAll(metrics);

            // restore current loans (sets availability=false and links to users)
            for (String line : safeReadAll("loans.csv")) {
                String[] p = splitCsv(line, 2);
                if (p == null) continue;
                String userId = p[0];
                String isbn = p[1];
                User u = users.get(userId);
                Book b = books.get(isbn);
                if (u != null && b != null && b.isAvailable()) {
                    u.borrowBook(b);
                }
            }
        } catch (Exception e) {
            System.err.println("[FileDatabase] Load failed: " + e.getMessage());
        }
    }

    public void saveFrom(Library library) {
        try {
            ensureDir();
            saveBooks(library.getAllBooks());
            saveUsers(library.getUsers());
            saveLoans(library.getUsers());
            saveMetrics(library.getBorrowCountsInternal());
        } catch (Exception e) {
            System.err.println("[FileDatabase] Save failed: " + e.getMessage());
        }
    }

    // --- Books ---
    private Map<String, Book> loadBooks() throws IOException {
        Map<String, Book> map = new HashMap<>();
        for (String line : safeReadAll("books.csv")) {
            String[] p = splitCsv(line, 5);
            if (p == null) continue;
            Book b = new Book(p[1], p[2], p[3], p[0]);
            boolean avail = Boolean.parseBoolean(p[4]);
            b.setAvailable(avail);
            map.put(b.getIsbn(), b);
        }
        return map;
    }

    private void saveBooks(Collection<Book> books) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Book b : books) {
            lines.add(String.join(",",
                    escape(b.getIsbn()),
                    escape(b.getTitle()),
                    escape(b.getAuthor()),
                    escape(b.getGenre()),
                    String.valueOf(b.isAvailable())));
        }
        writeAll("books.csv", lines);
    }

    // --- Users ---
    private Map<String, User> loadUsers() throws IOException {
        Map<String, User> map = new HashMap<>();
        for (String line : safeReadAll("users.csv")) {
            String[] p = splitCsv(line, 3);
            if (p == null) continue;
            String id = p[0];
            String type = p[1];
            String name = p[2];
            User u = switch (type) {
                case "Student" -> new Student(id, name);
                case "Teacher" -> new Teacher(id, name);
                case "Admin" -> new Admin(id, name);
                default -> null;
            };
            if (u != null) map.put(id, u);
        }
        return map;
    }

    private void saveUsers(List<User> users) throws IOException {
        List<String> lines = new ArrayList<>();
        for (User u : users) {
            lines.add(String.join(",",
                    escape(u.getId()),
                    u.getClass().getSimpleName(),
                    escape(u.getName())));
        }
        writeAll("users.csv", lines);
    }

    // --- Loans ---
    private void saveLoans(List<User> users) throws IOException {
        List<String> lines = new ArrayList<>();
        for (User u : users) {
            for (Book b : u.getBorrowedBooks()) {
                lines.add(String.join(",", escape(u.getId()), escape(b.getIsbn())));
            }
        }
        writeAll("loans.csv", lines);
    }

    // --- Metrics ---
    private Map<String, Integer> loadMetrics() throws IOException {
        Map<String, Integer> map = new HashMap<>();
        for (String line : safeReadAll("metrics.csv")) {
            String[] p = splitCsv(line, 2);
            if (p == null) continue;
            map.put(p[0], Integer.parseInt(p[1]));
        }
        return map;
    }

    private void saveMetrics(Map<String, Integer> borrowCounts) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, Integer> e : borrowCounts.entrySet()) {
            lines.add(e.getKey() + "," + e.getValue());
        }
        writeAll("metrics.csv", lines);
    }

    // --- Utilities ---
    private void ensureDir() throws IOException { if (!Files.exists(dataDir)) Files.createDirectories(dataDir); }

    private List<String> safeReadAll(String file) throws IOException {
        Path p = dataDir.resolve(file);
        if (!Files.exists(p)) return List.of();
        return Files.readAllLines(p, StandardCharsets.UTF_8);
    }

    private void writeAll(String file, List<String> lines) throws IOException {
        Path p = dataDir.resolve(file);
        Files.write(p, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static String[] splitCsv(String line, int expected) {
        // simple CSV (no embedded commas); guard for malformed lines
        String[] parts = line.split(",");
        return parts.length == expected ? parts : null;
    }

    private static String escape(String s) {
        // minimal escape: trim and replace newlines/commas just in case
        return s.replace("\n", " ").replace(",", " ").trim();
    }
}
