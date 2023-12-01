package ru.barkhatnat.exceptions;

public class NotAvailableRecipeException extends Exception {

    public NotAvailableRecipeException(String errorMessage) {
        super(errorMessage);
    }

}
