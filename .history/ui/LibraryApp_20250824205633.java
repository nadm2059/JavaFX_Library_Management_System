package ui;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.*;
import services.Library;
import services.RecommendationEngine;
import services.FileDatabase;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Minimal JavaFX UI: list books, search, borrow/return, show recommendations
 */
public class LibraryApp extends Application {
    private final Library library = new Library();
    private final ObservableList<Book> books = FXCollections.observableArrayList();
    private final ObservableList<User> users = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        // wire persistence and seed if empty
        FileDatabase db = new FileDatabase("data");
        library.attachDatabase(db);
        library.loadAll();

        if (library.getAllBooks().isEmpty()) seedSampleData();
        books.setAll(library.getAllBooks().stream()
                .sorted(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER))
                .toList());
        users.setAll(library.getUsers());

        // UI controls
        ListView<Book> listView = new ListView<>(books);
        listView.setPrefWidth(480);

        TextField searchField = new TextField();
        searchField.setPromptText("Search by title/author/genre...");

        ComboBox<User> userBox = new ComboBox<>(users);
        userBox.setPromptText("Select user");

        Button borrowBtn = new Button("Borrow");
        Button returnBtn = new Button("Return");
        Button recBtn = new Button("Get Recs");
        Button saveBtn = new Button("Save");

        TextArea output = new TextArea();
        output.setEditable(false);
        output.setPrefRowCount(10);

        // Layout
        HBox top = new HBox(10, new Label("User:"), userBox, searchField);
        top.setPadding(new Insets(10));
        HBox actions = new HBox(10, borrowBtn, returnBtn, recBtn, saveBtn);
        actions.setPadding(new Insets(10));
        VBox right = new VBox(10, actions, new Label("Log:"), output);
        right.setPadding(new Insets(10));
        right.setPrefWidth(420);

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(listView);
        root.setRight(right);

        // Behavior: search filter
        searchField.textProperty().addListener((obs, o, n) -> {
            String q = n == null ? "" : n.trim().toLowerCase();
            List<Book> filtered = library.getAllBooks().stream()
                    .filter(b -> b.getTitle().toLowerCase().contains(q)
                            || b.getAuthor().toLowerCase().contains(q)
                            || b.getGenre().toLowerCase().contains(q))
                    .sorted(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER))
                    .collect(Collectors.toList());
            books.setAll(filtered);
        });

        borrowBtn.disableProperty().bind(Bindings.createBooleanBinding(
                () -> userBox.getValue() == null || listView.getSelectionModel().getSelectedItem() == null,
                userBox.valueProperty(), listView.getSelectionModel().selectedItemProperty()));

        returnBtn.disableProperty().bind(borrowBtn.disableProperty());

        borrowBtn.setOnAction(e -> {
            User u = userBox.getValue();
            Book b = listView.getSelectionModel().getSelectedItem();
            if (u != null && b != null) {
                boolean ok = library.borrowBook(u, b.getIsbn());
                output.appendText((ok ? "Borrowed: " : "Failed to borrow: ") + b + " by " + u + "\n");
                listView.refresh();
            }
        });

        returnBtn.setOnAction(e -> {
            User u = userBox.getValue();
            Book b = listView.getSelectionModel().getSelectedItem();
            if (u != null && b != null) {
                boolean ok = library.returnBook(u, b.getIsbn());
                output.appendText((ok ? "Returned: " : "Failed to return: ") + b + " by " + u + "\n");
                listView.refresh();
            }
        });

        recBtn.setOnAction(e -> {
            User u = userBox.getValue();
            if (u == null) {
                output.appendText("Select a user to get recommendations.\n");
                return;
            }
            var engine = new RecommendationEngine(library);
            var recs = engine.recommendFor(u, 5);
            output.appendText("Recommendations for " + u.getName() + ":\n");
            for (Book rb : recs) output.appendText(" • " + rb + "\n");
        });

        saveBtn.setOnAction(e -> {
            library.saveAll();
            output.appendText("Data saved to ./data\n");
        });

        stage.setTitle("Smart Library Management System");
        stage.setScene(new Scene(root, 960, 600));
        stage.show();
    }

    private void seedSampleData() {
        // seed books
        library.addBook(new Book("Effective Java", "Joshua Bloch", "Programming", "11111"));
        library.addBook(new Book("Clean Code", "Robert C. Martin", "Programming", "22222"));
        library.addBook(new Book("Design Patterns", "GoF", "Programming", "33333"));
        library.addBook(new Book("Dune", "Frank Herbert", "Science Fiction", "44444"));
        library.addBook(new Book("Neuromancer", "William Gibson", "Science Fiction", "55555"));
        library.addBook(new Book("Sapiens", "Yuval Noah Harari", "History", "66666"));
        library.addBook(new Book("Educated", "Tara Westover", "Memoir", "77777"));
        library.addBook(new Book("The Hobbit", "J.R.R. Tolkien", "Fantasy", "88888"));
        library.addBook(new Book("Atomic Habits", "James Clear", "Self-Help", "99999"));

        // seed users
        library.registerUser(new Student("S001", "Alice"));
        library.registerUser(new Teacher("T001", "Dr. Bob"));
        library.registerUser(new Admin("A001", "Charlie (Admin)"));
    }

    public static void main(String[] args) { launch(args); }
}
