package ru.barkhatnat.classes;

import ru.barkhatnat.dao.IngredientDao;

import java.util.Objects;

public class Ingredient {
    private final String name;

    public Ingredient(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Ingredient ingredient)) {
            return false;
        }
        return name.equals(ingredient.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
