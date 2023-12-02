package ru.barkhatnat.db;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseCreator {
    private DatabaseCreator() {
    }

    public static void create() throws SQLException {
        DatabaseConnectionManager.openConnection();
        DatabaseConnectionManager.executeUpdate("CREATE TABLE ingredient (id integer AUTO_INCREMENT primary key, name varchar NOT NULL, UNIQUE(name))");
        DatabaseConnectionManager.executeUpdate("CREATE TABLE fridge_product (id integer AUTO_INCREMENT primary key, ingredient_id integer REFERENCES ingredient(id), amount DOUBLE PRECISION NOT NULL, expiration_date TIMESTAMP NOT NULL)");
        DatabaseConnectionManager.executeUpdate("CREATE TABLE recipe (id integer AUTO_INCREMENT primary key, name varchar NOT NULL, ingredient_id integer REFERENCES ingredient(id), amount DOUBLE PRECISION NOT NULL, number_of_servings integer NOT NULL)");
    }
}
