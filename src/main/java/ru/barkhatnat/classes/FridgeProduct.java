package ru.barkhatnat.classes;

import java.sql.Timestamp;
import java.util.Objects;

public class FridgeProduct {
    private Ingredient ingredient;
    private double amount;
    private Timestamp expirationDate;
    private boolean isExpired;

    public FridgeProduct(Ingredient ingredient, double amount, Timestamp expirationDate) {
        this.ingredient = ingredient;
        this.amount = amount;
        this.expirationDate = expirationDate;
        isExpired = false;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Timestamp getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Timestamp expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FridgeProduct fridgeProduct)) {
            return false;
        }
        return ingredient.equals(fridgeProduct.ingredient) && amount == fridgeProduct.amount && expirationDate.equals(fridgeProduct.expirationDate);
    }

    public void markAsExpired() {
        isExpired = true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingredient, amount, expirationDate);
    }

    public boolean isExpired() {
        return isExpired;
    }
}
