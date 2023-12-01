package ru.barkhatnat.classes;

import java.util.HashSet;

public class RecipeBook {
    private HashSet<Recipe> recipes = new HashSet<>();

    public void addRecipe(Recipe recipe) {
        recipes.add(recipe);
    }

    public void removeRecipe(Recipe recipe) {
        recipes.remove(recipe);
    }

    public HashSet<Recipe> getRecipes() {
        return new HashSet<>(recipes);
    }
}
