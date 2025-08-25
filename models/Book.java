package models;

/**
 * Domain model: Book
 */
public class Book {
    private final String title;
    private final String author;
    private final String genre;
    private final String isbn;
    private boolean isAvailable;

    public Book(String title, String author, String genre, String isbn) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.isbn = isbn;
        this.isAvailable = true;
    }

    // Getters
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getGenre() { return genre; }
    public String getIsbn() { return isbn; }
    public boolean isAvailable() { return isAvailable; }

    // State change
    public void setAvailable(boolean available) { this.isAvailable = available; }

    @Override
    public String toString() {
        return title + " by " + author + " [" + (isAvailable ? "Available" : "Borrowed") + "]";
    }

}