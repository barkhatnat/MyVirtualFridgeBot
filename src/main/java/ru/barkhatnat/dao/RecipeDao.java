package ru.barkhatnat.dao;

import ru.barkhatnat.classes.Ingredient;
import ru.barkhatnat.classes.Recipe;
import ru.barkhatnat.db.DatabaseConnectionManager;
import ru.barkhatnat.exceptions.SearchingException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class RecipeDao {
    private final IngredientDao ingredientDao = new IngredientDao();

    public void addRecipe(Recipe recipe) throws SQLException, SearchingException {
        String query = "INSERT INTO recipe (name, ingredient_id, amount, number_of_servings) VALUES (?,?,?,?)";
        PreparedStatement preparedStatement = DatabaseConnectionManager.connection.prepareStatement(query);
        for (Ingredient ingredient : recipe.getIngredientAmountCatalog().keySet()) {
            preparedStatement.setString(1, recipe.getName());
            preparedStatement.setInt(2, ingredientDao.findIdByIngredient(ingredient));
            preparedStatement.setDouble(3, recipe.getIngredientAmountCatalog().get(ingredient));
            preparedStatement.setInt(4, recipe.getNumberOfServings());
            preparedStatement.executeUpdate();
        }
    }

    public int findIdByRecipe(Recipe recipe) throws SQLException, SearchingException {
        String query = "SELECT * FROM recipe WHERE name = ?";
        PreparedStatement preparedStatement = DatabaseConnectionManager.connection.prepareStatement(query);
        preparedStatement.setString(1, recipe.getName());
        ResultSet result = preparedStatement.executeQuery();
        if (result.next()) {
            return result.getInt("id");
        }
        throw new SearchingException("There is no recipe like this in the database");
    }

    public void deleteRecipe(String name) throws SQLException {
        String sql = "DELETE FROM recipe WHERE name = ?";
        PreparedStatement preparedStatement = DatabaseConnectionManager.connection.prepareStatement(sql);
        preparedStatement.setString(1, name);
        preparedStatement.execute();
    }

    public void updateRecipeNumberOfServings(Recipe recipe, int number) throws SQLException, SearchingException {
        String sql = "UPDATE recipe SET  number_of_servings  = ? WHERE id = ?";
        PreparedStatement preparedStatement = DatabaseConnectionManager.connection.prepareStatement(sql);
        preparedStatement.setInt(1, number);
        preparedStatement.setInt(2, findIdByRecipe(recipe));
        preparedStatement.execute();

    }

    public void updateRecipeIngredient(Recipe recipe, Ingredient oldIngredient, Ingredient newIngredient) throws SQLException, SearchingException {
        String sql = "UPDATE recipe SET ingredient_id = ? WHERE ingredient_id =? AND name = ?";
        boolean ingredientIsFound = false;
        PreparedStatement preparedStatement = DatabaseConnectionManager.connection.prepareStatement(sql);
        preparedStatement.setString(3, recipe.getName());
        for (Ingredient ingredient : recipe.getIngredientAmountCatalog().keySet()) {
            if (ingredient.equals(oldIngredient)) {
                ingredientIsFound = true;
                preparedStatement.setInt(1, ingredientDao.findIdByIngredient(newIngredient));
                preparedStatement.setInt(2, ingredientDao.findIdByIngredient(oldIngredient));
                preparedStatement.execute();
                break;
            }
        }
        if (!ingredientIsFound) {
            throw new SearchingException("There is no ingredient like this in the database");
        }
    }

    public void updateRecipeAmount(Recipe recipe, Ingredient updatingIngredient, double amount) throws SQLException, SearchingException {
        String sql = "UPDATE recipe SET amount = ? WHERE ingredient_id =? AND name = ?";
        boolean ingredientIsFound = false;
        PreparedStatement preparedStatement = DatabaseConnectionManager.connection.prepareStatement(sql);
        preparedStatement.setString(3, recipe.getName());
        for (Ingredient ingredient : recipe.getIngredientAmountCatalog().keySet()) {
            if (ingredient.equals(updatingIngredient)) {
                ingredientIsFound = true;
                preparedStatement.setDouble(1, amount);
                preparedStatement.setInt(2, ingredientDao.findIdByIngredient(ingredient));
                preparedStatement.execute();
                break;
            }
        }
        if (!ingredientIsFound) {
            throw new SearchingException("There is no ingredient like this in the database");
        }
    }

    public void deleteIngredient(Recipe recipe, Ingredient ingredient) throws SQLException, SearchingException {
        String sql = "DELETE FROM recipe WHERE ingredient_id = ? AND id = ?";
        PreparedStatement preparedStatement = DatabaseConnectionManager.connection.prepareStatement(sql);
        preparedStatement.setInt(1, ingredientDao.findIdByIngredient(ingredient));
        preparedStatement.setInt(2, findIdByRecipe(recipe));
        preparedStatement.execute();
    }

    public Recipe findRecipeByName(String name) throws SQLException, SearchingException {
        String query = "SELECT * FROM recipe WHERE name = ?";
        PreparedStatement preparedStatement = DatabaseConnectionManager.connection.prepareStatement(query);
        preparedStatement.setString(1, name);
        ResultSet result = preparedStatement.executeQuery();
        HashMap<Ingredient, Double> ingredientAmountCatalog = new HashMap<>();
        int numberOfServings = 0;
        Recipe recipe = null;
        if (result.next()) {
            numberOfServings = result.getInt("number_of_servings");
            recipe = new Recipe(name, numberOfServings);
            recipe.addNewIngredientAndAmount(ingredientDao.findIngredientById(result.getInt("ingredient_id")), result.getDouble("amount"));
        }
        while (result.next()) {
            recipe.addNewIngredientAndAmount(ingredientDao.findIngredientById(result.getInt("ingredient_id")), result.getDouble("amount"));
        }
        return recipe;
    }
}
