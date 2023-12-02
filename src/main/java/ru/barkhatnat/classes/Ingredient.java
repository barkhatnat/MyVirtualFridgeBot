package ru.barkhatnat.classes;

public record Ingredient(String name) {

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

}
