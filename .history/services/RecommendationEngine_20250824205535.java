package services;

import models.Book;
import models.User;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Simple content+popularity based recommendations
 */
public class RecommendationEngine {
    private final Library library;
    public RecommendationEngine(Library library) { this.library = library; }

    public List<Book> recommendFor(User user, int limit) {
        Map<String, Long> genreCounts = user.getBorrowedBooks().stream()
                .map(Book::getGenre)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        List<Book> candidates = library.getAvailableBooks();

        Comparator<Book> popularityThenTitle = Comparator
                .comparingInt((Book b) -> library.getBorrowCount(b.getIsbn())).reversed()
                .thenComparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER);

        if (!genreCounts.isEmpty()) {
            List<String> preferred = genreCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .map(Map.Entry::getKey)
                    .toList();
            Map<String, Integer> rank = new HashMap<>();
            for (int i = 0; i < preferred.size(); i++) rank.put(preferred.get(i), i);
            Comparator<Book> byAffinity = Comparator.comparingInt(b -> rank.getOrDefault(b.getGenre(), Integer.MAX_VALUE));
            return candidates.stream().sorted(byAffinity.thenComparing(popularityThenTitle)).limit(limit).toList();
        }
        return candidates.stream().sorted(popularityThenTitle).limit(limit).toList();
    }
}

