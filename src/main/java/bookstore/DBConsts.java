package org.dgkrajnik.bookstore;

class DBConsts {
    static final String SCHEMA = "\n"+
        "CREATE TABLE IF NOT EXISTS authors (\n"+
            "id INT IDENTITY PRIMARY KEY,\n"+
            "name VARCHAR(5000),\n"+
            "CONSTRAINT deduplicated_authors UNIQUE (id, name),\n"+
        ");\n"+
        "CREATE TABLE IF NOT EXISTS tags (\n"+
            "id INT IDENTITY PRIMARY KEY,\n"+
            "tag VARCHAR(5000),\n"+
        ");\n"+
        "CREATE TABLE IF NOT EXISTS books (\n"+
            "id INT IDENTITY PRIMARY KEY,\n"+
            "name VARCHAR(5000),\n"+
            "publish_date DATE,\n"+
            "price DECIMAL(12,4),\n"+
            "book_data BLOB(100M),\n"+
            "author_id INT FOREIGN KEY REFERENCES authors(id)\n"+
        ");\n"+
        "CREATE TABLE IF NOT EXISTS book_tags (\n"+
            "book_id INT NOT NULL FOREIGN KEY REFERENCES books(id),\n"+
            "tag_id INT NOT NULL FOREIGN KEY REFERENCES tags(id),\n"+
            "CONSTRAINT deduplicated_tags UNIQUE (book_id, tag_id),\n"+
        ");\n";
}
