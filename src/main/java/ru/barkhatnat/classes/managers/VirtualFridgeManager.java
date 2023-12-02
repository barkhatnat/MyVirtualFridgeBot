package ru.barkhatnat.classes.managers;

import ru.barkhatnat.classes.*;
import ru.barkhatnat.exceptions.ExpirationDateException;
import ru.barkhatnat.exceptions.FridgeProductNumberException;
import ru.barkhatnat.exceptions.NotAvailableRecipeException;
import ru.barkhatnat.exceptions.SearchingException;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static ru.barkhatnat.classes.managers.DateEditor.timestampToString;

public class VirtualFridgeManager {
    private final FridgeProductManager fridgeProductManager;
    private final RecipeManager recipeManager;
    RecipeBook recipeBook;
    Fridge fridge;
    HashMap<Ingredient, Double> readyToUseCatalog = new HashMap<>();

    public VirtualFridgeManager() {
        fridgeProductManager = new FridgeProductManager();
        recipeManager = new RecipeManager();
        recipeBook = recipeManager.getRecipeBook();
        fridge = fridgeProductManager.getFridge();
    }

    public Set<Recipe> findAvailableRecipes() {
        fillReadyToUseCatalog();
        return recipeBook.getRecipes().stream()
                .filter(recipe -> recipe.getIngredientAmountCatalog().entrySet().stream()
                        .allMatch(entry -> readyToUseCatalog.getOrDefault(entry.getKey(), 0.0) >= entry.getValue()))
                .collect(Collectors.toSet());
    }

    private Map<String, Double> calculatePurchases(Recipe recipe) {
        Map<String, Double> result = new HashMap<>();
        double totalPurchases = 0.0;
        for (Map.Entry<Ingredient, Double> entry : recipe.getIngredientAmountCatalog().entrySet()) {
            Ingredient ingredient = entry.getKey();
            double amountNeeded = entry.getValue() - readyToUseCatalog.getOrDefault(ingredient, 0.0);
            if (amountNeeded > 0) {
                totalPurchases += amountNeeded;
                result.put(ingredient.getName(), totalPurchases);
            }
        }
        return result;
    }

    public Map<String, Map<String, Double>> findRecipeWithMinimalPurchases() {
        fillReadyToUseCatalog();
        return recipeBook.getRecipes().stream()
//                .filter(recipe -> recipe.getIngredientAmountCatalog().entrySet().stream()
//                        .anyMatch(entry -> readyToUseCatalog.getOrDefault(entry.getKey(), 0.0) < entry.getValue()))
                .collect(Collectors.toMap(
                        Recipe::getName,
                        this::calculatePurchases
                ));
    }

    private void fillReadyToUseCatalog() {
        for (FridgeProduct product : fridge.getProducts()) {
            if (!product.isExpired()) {
                readyToUseCatalog.merge(product.getIngredient(), product.getAmount(), Double::sum);
            }
        }
    }

    public void chooseRecipe(Recipe recipe) throws NotAvailableRecipeException, FridgeProductNumberException, ExpirationDateException, SQLException, SearchingException {
        Set<Recipe> availableRecipes = findAvailableRecipes();
        if (!availableRecipes.contains(recipe)) {
            throw new NotAvailableRecipeException("This recipe is not available");
        }
        for (Map.Entry<Ingredient, Double> entry : recipe.getIngredientAmountCatalog().entrySet()) {
            Ingredient ingredient = entry.getKey();
            Double amountNeeded = entry.getValue();
            try {
                List<FridgeProduct> fridgeProducts = fridgeProductManager.findFridgeProductByName(ingredient.getName());
                fridgeProducts.sort(Comparator.comparing(FridgeProduct::getExpirationDate));
                for (FridgeProduct fridgeProduct : fridgeProducts) {
                    if (amountNeeded != 0) {
                        double amountAvailable = fridgeProduct.getAmount();
                        if (amountAvailable >= amountNeeded) {
                            fridgeProductManager.editFridgeProductAmountWithNameAndDate(fridgeProduct.getIngredient().getName(), amountAvailable - amountNeeded, timestampToString(fridgeProduct.getExpirationDate()));
                            amountNeeded = 0.0;
                        } else {
                            amountNeeded -= amountAvailable;
                            fridgeProductManager.editFridgeProductAmountWithNameAndDate(fridgeProduct.getIngredient().getName(), 0, timestampToString(fridgeProduct.getExpirationDate()));
                        }
                    }
                }
                if (amountNeeded > 0) {
                    throw new FridgeProductNumberException("Not enough quantity of " + ingredient.getName() + " in the fridge");
                }
            } catch (SQLException | SearchingException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public FridgeProductManager getFridgeProductManager() {
        return fridgeProductManager;
    }

    public RecipeManager getRecipeManager() {
        return recipeManager;
    }


    public HashMap<Ingredient, Double> getReadyToUseCatalog() {
        return readyToUseCatalog;
    }
}
