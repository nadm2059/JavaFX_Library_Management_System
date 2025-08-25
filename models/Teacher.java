package models;

/** Teacher with limit=5 */
public class Teacher extends User {
    public Teacher(String id, String name) { super(id, name); }
    @Override public int getMaxBooksAllowed() { return 5; }
}

