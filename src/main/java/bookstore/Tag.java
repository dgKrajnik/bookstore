package org.dgkrajnik.bookstore;

public class Tag {
    private int id;
    private String name;

    protected Tag(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getID() {
        return id;
    }
    public String getName() {
        return name;
    }
}
