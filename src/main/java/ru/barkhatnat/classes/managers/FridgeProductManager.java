package ru.barkhatnat.classes.managers;

import ru.barkhatnat.classes.Fridge;
import ru.barkhatnat.classes.FridgeProduct;
import ru.barkhatnat.classes.Ingredient;
import ru.barkhatnat.dao.FridgeProductDao;
import ru.barkhatnat.exceptions.AmountException;
import ru.barkhatnat.exceptions.ExpirationDateException;
import ru.barkhatnat.exceptions.FridgeProductNumberException;
import ru.barkhatnat.exceptions.SearchingException;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import static ru.barkhatnat.classes.managers.DateEditor.stringToTimestamp;

public class FridgeProductManager {
    public Fridge getFridge() {
        return fridge;
    }

    private final Fridge fridge;
    private final FridgeProductDao fridgeProductDao = new FridgeProductDao();


    public FridgeProductManager() {
        this.fridge = new Fridge();
    }

    public void addProductToFridge(String name, double amount, String expirationDate) throws SQLException, ExpirationDateException, AmountException, SearchingException {
        Ingredient ingredient = IngredientManager.nameToIngredient(name);
        if (checkAmount(amount)) {
            FridgeProduct product = new FridgeProduct(ingredient, amount, stringToTimestamp(expirationDate));
            integrateProduct(product);
        } else {
            throw new AmountException("Amount of an ingredient can't be less than 0");
        }
    }

    private void integrateProduct(FridgeProduct product) throws SQLException, SearchingException {
        HashSet<FridgeProduct> products = fridge.getProducts();
        Iterator<FridgeProduct> iterator = products.iterator();
        boolean isProductFound = false;
        while (iterator.hasNext()) {
            FridgeProduct pr = iterator.next();
            if (pr.getIngredient().equals(product.getIngredient()) && pr.getExpirationDate().equals(product.getExpirationDate())) {
                double currentAmount = pr.getAmount();
                pr.setAmount(currentAmount + product.getAmount());
                fridgeProductDao.updateFridgeProductAmount(product, pr.getExpirationDate(), currentAmount + product.getAmount());
                isProductFound = true;
                break;
            }
        }
        if (!isProductFound) {
            fridge.addProduct(product);
            fridgeProductDao.addFridgeProduct(product);
        }
    }

    public ArrayList<FridgeProduct> findProductByName(String name) throws SQLException, SearchingException {
        ArrayList<FridgeProduct> foundProducts = new ArrayList<>();
        for (FridgeProduct product : fridge.getProducts()) {
            for (FridgeProduct sameNameProduct : fridgeProductDao.findFridgeProductByName(name)) {
                if (sameNameProduct.getIngredient().equals(new Ingredient(name))) {
                    foundProducts.add(sameNameProduct);
                }
            }
        }
        return foundProducts;
    }

    public void removeProductFromFridgeWithName(String name) throws SQLException, SearchingException, FridgeProductNumberException {
        ArrayList<FridgeProduct> productsToRemove = fridgeProductDao.findFridgeProductByName(name);
        if (productsToRemove.size() == 1) {
            fridgeProductDao.deleteFridgeProductWithName(name);
            fridge.removeProduct(productsToRemove.get(0));
        } else {
            throw new FridgeProductNumberException("There are 2 or more products with this name");
        }
    }

    public void removeProductFromFridgeWithNameAndExpirationDate(String name, String expirationDate) throws SQLException, SearchingException, FridgeProductNumberException, ExpirationDateException {
        ArrayList<FridgeProduct> productsToRemove = fridgeProductDao.findFridgeProductByName(name);
        boolean isProductFound = false;
        Timestamp editedExpirationDate = stringToTimestamp(expirationDate);
        for (FridgeProduct product : productsToRemove) {
            if (editedExpirationDate.equals(product.getExpirationDate())) {
                fridgeProductDao.deleteFridgeProductWithNameAndExpirationDate(name, editedExpirationDate);
                fridge.removeProduct(product);
                isProductFound = true;
                break;
            }
        }
        if (!isProductFound) {
            throw new SearchingException("No product with this name and expiration date");
        }
    }

    public void editFridgeProductExpirationDateWithOneDate(String name, String expirationDate) throws SQLException, SearchingException, ExpirationDateException, FridgeProductNumberException {
        ArrayList<FridgeProduct> oldProducts = fridgeProductDao.findFridgeProductByName(name);
        if (oldProducts.size() == 1) {
            integrateEditedProductExpirationDate(oldProducts.get(0), expirationDate);
        } else {
            throw new FridgeProductNumberException("There are 2 or more products with this name");
        }
    }

    public void editFridgeProductExpirationDateWithTwoDates(String name, String newExpirationDate, String oldExpirationDate) throws SQLException, SearchingException, ExpirationDateException {
        ArrayList<FridgeProduct> oldProducts = fridgeProductDao.findFridgeProductByName(name);
        boolean isProductFound = false;
        for (FridgeProduct oldProduct : oldProducts) {
            if (stringToTimestamp(oldExpirationDate).equals(oldProduct.getExpirationDate())) {
                integrateEditedProductExpirationDate(oldProduct, newExpirationDate);
                isProductFound = true;
                break;
            }
        }
        if (!isProductFound) {
            throw new SearchingException("No product with this name and expiration date");
        }
    }

    public void editFridgeProductAmountWithName(String name, double amount) throws SQLException, SearchingException, FridgeProductNumberException {
        ArrayList<FridgeProduct> oldProducts = fridgeProductDao.findFridgeProductByName(name);
        if (oldProducts.size() == 1) {
            if (amount == 0.0) {
                fridgeProductDao.deleteFridgeProductWithName(name);
                fridge.removeProduct(oldProducts.get(0));
            } else {
                FridgeProduct product = oldProducts.get(0);
                integrateEditedProductAmount(product, amount);
            }

        } else {
            throw new FridgeProductNumberException("There are 2 or more products with this name");
        }

    }

    public void editFridgeProductAmountWithNameAndDate(String name, double amount, String oldExpirationDate) throws SQLException, SearchingException, ExpirationDateException {
        ArrayList<FridgeProduct> oldProducts = fridgeProductDao.findFridgeProductByName(name);
        boolean isProductFound = false;
        for (FridgeProduct oldProduct : oldProducts) {
            Timestamp editedOldExpirationDate = stringToTimestamp(oldExpirationDate);
            if (editedOldExpirationDate.equals(oldProduct.getExpirationDate())) {
                if (amount == 0.0) {
                    fridgeProductDao.deleteFridgeProductWithNameAndExpirationDate(name, editedOldExpirationDate);
                    fridge.removeProduct(oldProduct);
                    isProductFound = true;
                    break;
                } else {
                    integrateEditedProductAmount(oldProduct, amount);
                    isProductFound = true;
                    break;
                }
            }
        }
        if (!isProductFound) {
            throw new SearchingException("No product with this name and expiration date");
        }
    }

    private void integrateEditedProductAmount(FridgeProduct product, double amount) throws SQLException, SearchingException {
        fridge.update(product, new FridgeProduct(product.getIngredient(), amount, product.getExpirationDate()));
        fridgeProductDao.updateFridgeProductAmount(product, product.getExpirationDate(), amount);
    }

    private void integrateEditedProductExpirationDate(FridgeProduct product, String expirationDate) throws SQLException, SearchingException, ExpirationDateException {
        Timestamp editedExpirationDate = stringToTimestamp(expirationDate);
        HashSet<FridgeProduct> products = fridge.getProducts();
        Iterator<FridgeProduct> iterator = products.iterator();
        boolean isProductFound = false;
        while (iterator.hasNext()) {
            FridgeProduct pr = iterator.next();
            if (pr.getIngredient().equals(product.getIngredient()) && pr.getExpirationDate().equals(editedExpirationDate)) {
                double currentAmount = pr.getAmount();
                pr.setAmount(currentAmount + product.getAmount());
                fridgeProductDao.updateFridgeProductAmount(pr, editedExpirationDate, currentAmount + product.getAmount());
                isProductFound = true;
                break;
            }
        }
        if (!isProductFound) {
            fridge.update(product, new FridgeProduct(product.getIngredient(), product.getAmount(), editedExpirationDate));
            fridgeProductDao.updateFridgeProductExpirationDate(product, editedExpirationDate, product.getExpirationDate());
        }
    }

    public ArrayList<FridgeProduct> findFridgeProductByName(String name) throws SQLException, SearchingException {
        return fridgeProductDao.findFridgeProductByName(name);
    }

    private boolean checkAmount(double amount) {
        return amount > 0;
    }

}
