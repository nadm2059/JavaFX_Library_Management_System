package services;

import models.Book;
import models.User;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Core service: manages catalog, users, and transactions
 */
public class Library {
    private final Map<String, Book> bookCatalog = new HashMap<>(); // isbn -> Book
    private final Map<String, Integer> borrowCounts = new HashMap<>(); // isbn -> count
    private final List<User> users = new ArrayList<>();

    private FileDatabase db; // optional persistence layer

    // --- Persistence wiring ---
    public void attachDatabase(FileDatabase database) { this.db = database; }
    public void loadAll() { if (db != null) db.loadInto(this); }
    public void saveAll() { if (db != null) db.saveFrom(this); }

    // --- Catalog management ---
    public void addBook(Book book) {
        bookCatalog.put(book.getIsbn(), book);
        borrowCounts.putIfAbsent(book.getIsbn(), 0);
    }

    public boolean removeBookByIsbn(String isbn) {
        Book removed = bookCatalog.remove(isbn);
        borrowCounts.remove(isbn);
        // also ensure no user still holds this book
        if (removed != null) {
            for (User u : users) {
                u.getBorrowedBooks().stream()
                        .filter(b -> b.getIsbn().equals(isbn))
                        .findFirst()
                        .ifPresent(b -> u.returnBook(b));
            }
        }
        return removed != null;
    }

    // --- User management ---
    public void registerUser(User user) { users.add(user); }

    public Optional<User> findUserById(String id) {
        return users.stream().filter(u -> u.getId().equals(id)).findFirst();
    }

    public List<User> getUsers() { return Collections.unmodifiableList(users); }

    // --- Search ---
    public Book searchBookByTitle(String title) {
        for (Book book : bookCatalog.values()) {
            if (book.getTitle().equalsIgnoreCase(title)) return book;
        }
        return null;
    }

    public List<Book> searchBooksByAuthor(String author) {
        return bookCatalog.values().stream()
                .filter(b -> b.getAuthor().equalsIgnoreCase(author))
                .collect(Collectors.toList());
    }

    public List<Book> searchBooksByGenre(String genre) {
        return bookCatalog.values().stream()
                .filter(b -> b.getGenre().equalsIgnoreCase(genre))
                .collect(Collectors.toList());
    }

    // --- Borrowing & returning ---
    public boolean borrowBook(User user, String isbn) {
        Book book = bookCatalog.get(isbn);
        if (book == null) return false;
        boolean ok = user.borrowBook(book);
        if (ok) borrowCounts.merge(isbn, 1, Integer::sum);
        return ok;
    }

    public boolean returnBook(User user, String isbn) {
        Book book = bookCatalog.get(isbn);
        if (book == null) return false;
        return user.returnBook(book);
    }

    // --- Views & helpers ---
    public void displayBooks() {
        for (Book book : bookCatalog.values()) System.out.println(book);
    }

    public Collection<Book> getAllBooks() { return Collections.unmodifiableCollection(bookCatalog.values()); }

    public List<Book> getAvailableBooks() {
        return bookCatalog.values().stream().filter(Book::isAvailable).collect(Collectors.toList());
    }

    public int getBorrowCount(String isbn) { return borrowCounts.getOrDefault(isbn, 0); }

    // --- Accessors used by FileDatabase ---
    public Map<String, Book> getBookCatalogInternal() { return bookCatalog; }
    public Map<String, Integer> getBorrowCountsInternal() { return borrowCounts; }
}

