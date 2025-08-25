import models.*;
import services.FileDatabase;
import services.Library;
import services.RecommendationEngine;

import java.util.List;
import java.util.Scanner;

/**
 * CLI runner. Run with no args for CLI; run `java ui.LibraryApp` (or Main gui) for JavaFX.
 */
public class Main {
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        Library library = new Library();
        FileDatabase db = new FileDatabase("data");
        library.attachDatabase(db);
        library.loadAll();

        if (library.getAllBooks().isEmpty()) seed(library); // first run convenience

        if (args.length > 0 && args[0].equalsIgnoreCase("gui")) {
            ui.LibraryApp.main(new String[]{});
            return;
        }

        System.out.println("=== Smart Library (CLI) ===");
        outer:
        while (true) {
            System.out.println("\n1) User Mode  2) Admin Mode  3) Save  4) Exit");
            System.out.print("> ");
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> userMode(library);
                case "2" -> adminMode(library);
                case "3" -> { library.saveAll(); System.out.println("Saved to ./data"); }
                case "4" -> { library.saveAll(); break outer; }
                default -> System.out.println("Invalid option.");
            }
        }
        System.out.println("Goodbye!");
    }

    // --- User Mode (borrow/return/search/recommend) ---
    private static void userMode(Library library) {
        System.out.print("Enter User ID: ");
        String uid = sc.nextLine().trim();
        var userOpt = library.findUserById(uid);
        if (userOpt.isEmpty()) { System.out.println("User not found."); return; }
        User u = userOpt.get();
        RecommendationEngine engine = new RecommendationEngine(library);

        while (true) {
            System.out.println("\nUser: " + u +
                    "\n 1) List books\n 2) Search by title\n 3) Borrow by ISBN\n 4) Return by ISBN\n 5) Recommendations\n 6) Back");
            System.out.print("> ");
            String ch = sc.nextLine().trim();
            switch (ch) {
                case "1" -> library.displayBooks();
                case "2" -> { System.out.print("Title: ");
                    String t = sc.nextLine();
                    var b = library.searchBookByTitle(t);
                    System.out.println(b == null ? "Not found." : b);
                }
                case "3" -> { System.out.print("ISBN to borrow: ");
                    String isbn = sc.nextLine().trim();
                    boolean ok = library.borrowBook(u, isbn);
                    System.out.println(ok ? "Borrowed." : "Borrow failed.");
                }
                case "4" -> { System.out.print("ISBN to return: ");
                    String isbn = sc.nextLine().trim();
                    boolean ok = library.returnBook(u, isbn);
                    System.out.println(ok ? "Returned." : "Return failed.");
                }
                case "5" -> {
                    List<Book> recs = engine.recommendFor(u, 5);
                    if (recs.isEmpty()) System.out.println("No recommendations.");
                    else recs.forEach(b -> System.out.println(" • " + b));
                }
                case "6" -> { return; }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    // --- Admin Mode (add/remove/list/register) ---
    private static void adminMode(Library library) {
        System.out.print("Admin ID: ");
        String aid = sc.nextLine().trim();
        var adminOpt = library.findUserById(aid);
        if (adminOpt.isEmpty() || !(adminOpt.get() instanceof Admin)) {
            System.out.println("Access denied.");
            return;
        }
        while (true) {
            System.out.println("\nAdmin Menu:\n 1) Add Book\n 2) Remove Book\n 3) List Books\n 4) Register User\n 5) List Users\n 6) Back");
            System.out.print("> ");
            String ch = sc.nextLine().trim();
            switch (ch) {
                case "1" -> {
                    System.out.print("Title: "); String t = sc.nextLine();
                    System.out.print("Author: "); String a = sc.nextLine();
                    System.out.print("Genre: "); String g = sc.nextLine();
                    System.out.print("ISBN: "); String i = sc.nextLine();
                    library.addBook(new Book(t, a, g, i));
                    System.out.println("Added.");
                }
                case "2" -> {
                    System.out.print("ISBN to remove: "); String i = sc.nextLine();
                    boolean ok = library.removeBookByIsbn(i.trim());
                    System.out.println(ok ? "Removed." : "Not found.");
                }
                case "3" -> library.displayBooks();
                case "4" -> {
                    System.out.print("User Type (Student/Teacher/Admin): "); String ty = sc.nextLine().trim();
                    System.out.print("ID: "); String id = sc.nextLine().trim();
                    System.out.print("Name: "); String nm = sc.nextLine();
                    User u = switch (ty) {
                        case "Student" -> new Student(id, nm);
                        case "Teacher" -> new Teacher(id, nm);
                        case "Admin" -> new Admin(id, nm);
                        default -> null;
                    };
                    if (u == null) System.out.println("Invalid type."); else { library.registerUser(u); System.out.println("Registered."); }
                }
                case "5" -> library.getUsers().forEach(System.out::println);
                case "6" -> { return; }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private static void seed(Library library) {
        library.addBook(new Book("Effective Java", "Joshua Bloch", "Programming", "11111"));
        library.addBook(new Book("Clean Code", "Robert C. Martin", "Programming", "22222"));
        library.addBook(new Book("Design Patterns", "GoF", "Programming", "33333"));
        library.addBook(new Book("Dune", "Frank Herbert", "Science Fiction", "44444"));
        library.addBook(new Book("Neuromancer", "William Gibson", "Science Fiction", "55555"));
        library.addBook(new Book("Sapiens", "Yuval Noah Harari", "History", "66666"));
        library.addBook(new Book("Educated", "Tara Westover", "Memoir", "77777"));
        library.addBook(new Book("The Hobbit", "J.R.R. Tolkien", "Fantasy", "88888"));
        library.addBook(new Book("Atomic Habits", "James Clear", "Self-Help", "99999"));

        library.registerUser(new Student("S001", "Alice"));
        library.registerUser(new Teacher("T001", "Dr. Bob"));
        library.registerUser(new Admin("A001", "Charlie (Admin)"));
    }
}

