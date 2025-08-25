package models;

package models;

/** Admin role (limit=10) */
public class Admin extends User {
    public Admin(String id, String name) { super(id, name); }
    @Override public int getMaxBooksAllowed() { return 10; }
}

