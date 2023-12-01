package ru.barkhatnat.classes;

import java.util.HashSet;

public class Fridge {
    private final HashSet<FridgeProduct> products = new HashSet<>();

    public Fridge() {
    }

    public HashSet<FridgeProduct> getProducts() {
        return new HashSet<>(products);
    }

    public void addProduct(FridgeProduct product) {
        products.add(product);
    }

    public void update(FridgeProduct oldProduct, FridgeProduct newProduct) {
        products.remove(oldProduct);
        products.add(newProduct);
    }

    public void removeProduct(FridgeProduct product) {
        products.remove(product);
    }
}
