package ru.barkhatnat.dao;

import ru.barkhatnat.classes.Ingredient;
import ru.barkhatnat.db.DatabaseConnectionManager;
import ru.barkhatnat.exceptions.SearchingException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IngredientDao {
    public void addIngredient(Ingredient ingredient) throws SQLException {
        String query = "INSERT INTO ingredient (name) VALUES (?)";
        PreparedStatement preparedStatement = DatabaseConnectionManager.connection.prepareStatement(query);
        preparedStatement.setString(1, ingredient.getName());
        preparedStatement.executeUpdate();
    }

    public Ingredient findIngredientById(int id) throws SQLException, SearchingException {
        String query = "SELECT * FROM ingredient WHERE id = ?";
        PreparedStatement preparedStatement = DatabaseConnectionManager.connection.prepareStatement(query);
        preparedStatement.setInt(1, id);
        ResultSet result = preparedStatement.executeQuery();
        if (result.next()) {
            return new Ingredient(result.getString("name"));
        }
        throw new SearchingException("There is no ingredient with this id number");
    }

    public Ingredient findIngredientByName(String name) throws SQLException, SearchingException {
        String query = "SELECT * FROM ingredient WHERE name = ?";
        PreparedStatement preparedStatement = DatabaseConnectionManager.connection.prepareStatement(query);
        preparedStatement.setString(1, name);
        ResultSet result = preparedStatement.executeQuery();
        if (result.next()) {
            return new Ingredient(result.getString("name"));
        }
        throw new SearchingException("There is no ingredient with this id number");
    }

    public int findIdByIngredient(Ingredient ingredient) throws SQLException, SearchingException {
        String query = "SELECT * FROM ingredient WHERE name = ?";
        PreparedStatement preparedStatement = DatabaseConnectionManager.connection.prepareStatement(query);
        preparedStatement.setString(1, ingredient.getName());
        ResultSet result = preparedStatement.executeQuery();
        if (result.next()) {
            return result.getInt("id");
        }
        throw new SearchingException("There is no ingredient like this in database");
    }
}
