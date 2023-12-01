package ru.barkhatnat.db;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseCreator {
    private DatabaseCreator() throws SQLException {
    }

    public static void create() throws SQLException {
        DatabaseConnectionManager.openConnection();
        DatabaseConnectionManager.executeUpdate("CREATE TABLE ingredient (id integer AUTO_INCREMENT primary key, name varchar NOT NULL, UNIQUE(name))");
        DatabaseConnectionManager.executeUpdate("CREATE TABLE fridge_product (id integer AUTO_INCREMENT primary key, ingredient_id integer REFERENCES ingredient(id), amount DOUBLE PRECISION NOT NULL, expiration_date TIMESTAMP NOT NULL)");
        DatabaseConnectionManager.executeUpdate("CREATE TABLE recipe (id integer AUTO_INCREMENT primary key, name varchar NOT NULL, ingredient_id integer REFERENCES ingredient(id), amount DOUBLE PRECISION NOT NULL, number_of_servings integer NOT NULL)");
    }

    public static void show() throws SQLException {
        ResultSet result = DatabaseConnectionManager.executeQuery("SHOW TABLES");
        while (result.next()) {
            System.out.println(result.getString("TABLE_NAME"));
        }
    }

    public static void showFP() throws SQLException {
        ResultSet result = DatabaseConnectionManager.executeQuery("select * from fridge_product");
        while (result.next()) {
            System.out.print("ID " + result.getInt("id"));
            System.out.print(", Ingredient ID: " + result.getInt("ingredient_id"));
            System.out.print(", Amount: " + result.getDouble("amount"));

            System.out.print(", Exp date: " + result.getTimestamp("expiration_date") + "\n");

        }
    }

    public static void showRecipe() throws SQLException {
        ResultSet result = DatabaseConnectionManager.executeQuery("select * from recipe");
        while (result.next()) {
            System.out.print("ID " + result.getInt("id"));
            System.out.print(", Name: " + result.getString("name"));
            System.out.print(", Ingredient ID: " + result.getInt("ingredient_id"));
            System.out.print(", Amount: " + result.getDouble("amount"));
            System.out.print(", Number: " + result.getInt("number_of_servings") + "\n");
        }
    }

    public static void showIngredient() throws SQLException {
        ResultSet result = DatabaseConnectionManager.executeQuery("select * from ingredient");
        while (result.next()) {
            System.out.print("ID " + result.getInt("id"));
            System.out.print(", Name: " + result.getString("name") + "\n");
        }
    }

    public static void showRB() throws SQLException {
        ResultSet result = DatabaseConnectionManager.executeQuery("select * from recipe_book");
        while (result.next()) {
            System.out.print("ID " + result.getInt("id"));
            System.out.print(", Recipe: " + result.getInt("recipe_id") + "\n");
        }
    }
}
