package ru.barkhatnat.dao;

import ru.barkhatnat.classes.FridgeProduct;
import ru.barkhatnat.classes.Ingredient;
import ru.barkhatnat.db.DatabaseConnectionManager;
import ru.barkhatnat.exceptions.SearchingException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class FridgeProductDao {
    private final IngredientDao ingredientDao = new IngredientDao();

    public void addFridgeProduct(FridgeProduct fridgeProduct) throws SQLException, SearchingException {
        String query = "INSERT INTO fridge_product (ingredient_id, amount, expiration_date) VALUES (?, ?, ?)";
        PreparedStatement preparedStatement = DatabaseConnectionManager.connection.prepareStatement(query);
        preparedStatement.setInt(1, ingredientDao.findIdByIngredient(fridgeProduct.getIngredient()));
        preparedStatement.setDouble(2, fridgeProduct.getAmount());
        preparedStatement.setTimestamp(3, fridgeProduct.getExpirationDate());
        preparedStatement.executeUpdate();
    }

    public int findIdByFridgeProduct(FridgeProduct fridgeProduct) throws SQLException, SearchingException {
        String query = "SELECT * FROM fridge_product WHERE ingredient_id = ?";
        PreparedStatement preparedStatement = DatabaseConnectionManager.connection.prepareStatement(query);
        preparedStatement.setString(1, fridgeProduct.getIngredient().name());
        ResultSet result = preparedStatement.executeQuery();
        if (result.next()) {
            return result.getInt("id");
        }
        throw new SearchingException("There is no ingredient like this in database");
    }

    public ArrayList<FridgeProduct> findFridgeProductByName(String name) throws SQLException, SearchingException {
        String query = "SELECT * FROM fridge_product WHERE ingredient_id = ?";
        Ingredient ingredient = ingredientDao.findIngredientByName(name);
        PreparedStatement preparedStatement = DatabaseConnectionManager.connection.prepareStatement(query);
        preparedStatement.setInt(1, ingredientDao.findIdByIngredient(ingredient));
        ResultSet result = preparedStatement.executeQuery();
        ArrayList<FridgeProduct> products = new ArrayList<>();
        while (result.next()) {
           products.add(new FridgeProduct(ingredient, result.getDouble("amount"), result.getTimestamp("expiration_date")));
        }
        if (!products.isEmpty()){
            return products;
        }
        throw new SearchingException("There is no product in fridge like this in database");
    }

    public void deleteFridgeProductWithName(String name) throws SQLException, SearchingException {
        Ingredient ingredient = ingredientDao.findIngredientByName(name);
        String sql = "DELETE FROM fridge_product WHERE ingredient_id = ?";
        PreparedStatement preparedStatement = DatabaseConnectionManager.connection.prepareStatement(sql);
        preparedStatement.setInt(1, ingredientDao.findIdByIngredient(ingredient));
        preparedStatement.execute();
    }
    public void deleteFridgeProductWithNameAndExpirationDate(String name, Timestamp date) throws SQLException, SearchingException {
        Ingredient ingredient = ingredientDao.findIngredientByName(name);
        String sql = "DELETE FROM fridge_product WHERE ingredient_id = ? and expiration_date =?";
        PreparedStatement preparedStatement = DatabaseConnectionManager.connection.prepareStatement(sql);
        preparedStatement.setInt(1, ingredientDao.findIdByIngredient(ingredient));
        preparedStatement.setTimestamp(2, date);

        preparedStatement.execute();
    }

    public void updateFridgeProductExpirationDate(FridgeProduct fridgeProduct, Timestamp date, Timestamp oldDate) throws SQLException, SearchingException {
        String sql = "UPDATE fridge_product SET  expiration_date = ? WHERE ingredient_id = ? AND expiration_date = ?";
        PreparedStatement preparedStatement = DatabaseConnectionManager.connection.prepareStatement(sql);
        preparedStatement.setTimestamp(1, date);
        preparedStatement.setInt(2, ingredientDao.findIdByIngredient(fridgeProduct.getIngredient()));
        preparedStatement.setTimestamp(3, oldDate);

        preparedStatement.execute();
    }

    public void updateFridgeProductAmount(FridgeProduct fridgeProduct, Timestamp expirationDate, double amount) throws SQLException, SearchingException {
        String sql = "UPDATE fridge_product SET  amount = ? WHERE ingredient_id = ? AND expiration_date = ?";
        PreparedStatement preparedStatement = DatabaseConnectionManager.connection.prepareStatement(sql);
        preparedStatement.setDouble(1, amount);
        preparedStatement.setInt(2, ingredientDao.findIdByIngredient(fridgeProduct.getIngredient()));
        preparedStatement.setTimestamp(3, expirationDate);
        preparedStatement.execute();
    }
}

