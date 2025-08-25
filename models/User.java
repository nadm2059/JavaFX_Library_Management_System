package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base for all users
 */
public abstract class User {
    protected final String id;
    protected final String name;
    protected final List<Book> borrowedBooks = new ArrayList<>();

    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public abstract int getMaxBooksAllowed();

    // Borrow/Return
    public boolean borrowBook(Book book) {
        if (borrowedBooks.size() < getMaxBooksAllowed() && book.isAvailable()) {
            borrowedBooks.add(book);
            book.setAvailable(false);
            return true;
        }
        return false;
    }

    public boolean returnBook(Book book) {
        if (borrowedBooks.contains(book)) {
            borrowedBooks.remove(book);
            book.setAvailable(true);
            return true;
        }
        return false;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public List<Book> getBorrowedBooks() { return Collections.unmodifiableList(borrowedBooks); }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + id + ", " + name + ")";
    }
}


