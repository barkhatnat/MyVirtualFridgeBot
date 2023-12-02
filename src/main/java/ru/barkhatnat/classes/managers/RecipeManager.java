package ru.barkhatnat.classes.managers;

import ru.barkhatnat.classes.Ingredient;
import ru.barkhatnat.classes.Recipe;
import ru.barkhatnat.classes.RecipeBook;
import ru.barkhatnat.dao.RecipeDao;
import ru.barkhatnat.exceptions.AmountException;
import ru.barkhatnat.exceptions.RecipeNameException;
import ru.barkhatnat.exceptions.SearchingException;

import java.sql.SQLException;


public class RecipeManager {

    private final RecipeBook recipeBook;
    private final RecipeDao recipeDao = new RecipeDao();

    public RecipeManager() {
        this.recipeBook = new RecipeBook();
    }

    public Recipe addRecipeTitle(String name) throws RecipeNameException {
        Recipe recipe = new Recipe(name, 1);
        if (recipeBook.getRecipes().contains(recipe)) {
            throw new RecipeNameException("Recipe with this name already exists");
        }
        recipeBook.addRecipe(recipe);
        return recipe;
    }

    public void addIngredientToRecipe(Recipe recipe, String name, double amount) throws AmountException, SQLException {
        Ingredient ingredient = IngredientManager.nameToIngredient(name);
        if (checkAmount(amount)) {
            recipe.addNewIngredientAndAmount(ingredient, amount);
        } else {
            throw new AmountException("Amount of an ingredient can't be less than 0");
        }
    }

    public void addToBook(Recipe recipe) throws SQLException, SearchingException {
        recipeDao.addRecipe(recipe);
    }

    private boolean checkAmount(double amount) {
        return amount > 0;
    }

    public void removeIngredientFromRecipe(Recipe recipe, String name) throws SQLException, SearchingException {
        Ingredient ingredient = IngredientManager.ingredientDao.findIngredientByName(name);
        recipe.removeIngredient(ingredient);
        recipeDao.deleteIngredient(recipe, ingredient);
    }

    public void removeRecipe(String name) throws SQLException, SearchingException {
        Recipe recipe = recipeDao.findRecipeByName(name);
        recipeBook.removeRecipe(recipe);
        recipeDao.deleteRecipe(name);
    }

    public RecipeBook getRecipeBook() {
        return recipeBook;
    }

    public RecipeDao getRecipeDao() {
        return recipeDao;
    }
}
