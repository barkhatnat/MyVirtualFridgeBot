package ru.barkhatnat.classes;

import java.util.HashMap;
import java.util.Objects;

public class Recipe {
    private String name;
    private int numberOfServings;
    private final HashMap<Ingredient, Double> ingredientAmountCatalog;

    public Recipe(String name, int numberOfServings) {
        this.name = name;
        this.ingredientAmountCatalog = new HashMap<>();
        this.numberOfServings = numberOfServings;
    }

    public void addNewIngredientAndAmount(Ingredient ingredient, double amount) {
        ingredientAmountCatalog.put(ingredient, amount);
    }

    public void removeIngredient(Ingredient ingredient) {
        ingredientAmountCatalog.remove(ingredient);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumberOfServings() {
        return numberOfServings;
    }

    public void setNumberOfServings(int numberOfServings) {
        this.numberOfServings = numberOfServings;
    }

    public HashMap<Ingredient, Double> getIngredientAmountCatalog() {
        return new HashMap<>(ingredientAmountCatalog);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Recipe recipe)) {
            return false;
        }
        return name.equals(recipe.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
