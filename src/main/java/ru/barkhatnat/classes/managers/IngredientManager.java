package ru.barkhatnat.classes.managers;

import ru.barkhatnat.classes.Ingredient;
import ru.barkhatnat.dao.IngredientDao;

import java.sql.SQLException;
import java.util.HashSet;

public class IngredientManager {
    public static final IngredientDao ingredientDao = new IngredientDao();
    private static final HashSet<Ingredient> ingredients = new HashSet<>();

    private IngredientManager() {
    }

    public static Ingredient nameToIngredient(String name) throws SQLException {
        Ingredient ingredient = null;
        for (Ingredient ingr : ingredients) {
            if (name.equals(ingr.getName())) {
                ingredient = ingr;
                break;
            }
        }
        if (ingredient == null) {
            ingredient = new Ingredient(name);
            ingredients.add(ingredient);
            ingredientDao.addIngredient(ingredient);
        }
        return ingredient;
    }
}
