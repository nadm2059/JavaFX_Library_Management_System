package models;

/** Student with limit=3 */
public class Student extends User {
    public Student(String id, String name) { super(id, name); }
    @Override public int getMaxBooksAllowed() { return 3; }
}


