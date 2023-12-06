package ru.barkhatnat.service;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.barkhatnat.classes.Ingredient;
import ru.barkhatnat.classes.Recipe;
import ru.barkhatnat.classes.RecipeBook;
import ru.barkhatnat.classes.managers.DateEditor;
import ru.barkhatnat.classes.managers.IngredientManager;
import ru.barkhatnat.classes.managers.VirtualFridgeManager;
import ru.barkhatnat.db.DatabaseConnectionManager;
import ru.barkhatnat.db.DatabaseCreator;
import ru.barkhatnat.exceptions.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TelegramBot extends TelegramLongPollingBot {
    VirtualFridgeManager virtualFridgeManager;

    public TelegramBot(String token) {
        super(token);
        virtualFridgeManager = new VirtualFridgeManager();
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "получить приветственное сообщение"));
        listOfCommands.add(new BotCommand("/addproduct", "добавить продукт в холодильник"));
        listOfCommands.add(new BotCommand("/showproducts", "посмотреть все продукты в холодильнике"));
        listOfCommands.add(new BotCommand("/deleteproduct", "удалить продукт из холодильника"));
        listOfCommands.add(new BotCommand("/addrecipe", "добавить рецепт в кулинарную книгу"));
        listOfCommands.add(new BotCommand("/showrecipes", "посмотреть все рецепты в кулинарной книге"));
        listOfCommands.add(new BotCommand("/deleterecipe", "удалить рецепт из кулинарной книги"));
        listOfCommands.add(new BotCommand("/available", "посмотреть список доступных к использованию рецептов"));
        listOfCommands.add(new BotCommand("/chooserecipe", "выбрать рецепт для приготовления"));
        listOfCommands.add(new BotCommand("/minimalpurchases", "посмотреть рецепты, для которых нужны минимальные дозакупки"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            switch (messageText) {
                case "/start" -> startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                case "/addproduct" -> addProductCommandReceived(chatId);
                case "/showproducts" -> showProductsCommandReceived(chatId);
                case "/deleteproduct" -> deleteProductsCommandReceived(chatId);
                case "/addrecipe" -> addRecipeCommandReceived(chatId);
                case "/showrecipes" -> showRecipesCommandReceived(chatId);
                case "/deleterecipe" -> deleteRecipeCommandReceived(chatId);
                case "/available" -> findAvailableRecipesCommandReceived(chatId);
                case "/chooserecipe" -> chooseRecipeCommandReceived(chatId);
                case "/minimalpurchases" -> findMinPurchasesCommandReceived(chatId);
                default -> {
                    if (update.getMessage().isReply() && update.getMessage().getReplyToMessage().getText().equals("Cделай реплай этого сообщения и укажи название, количество (просто число, без меры измерения), а также дату, когда заканчивается " +
                            "срок годности продукта, который вы хотите добавить в холодильник. Укажите эти параметры через запятую. " +
                            "Учти, что для введения дробного количества игредиента в качестве разделителя нужно " +
                            "использовать точку. Срок годности нужно указать в формате дд.мм.ггг (например, 05.04.2024).\nПример ввода: Молоко, 1, 15.12.2023")) {
                        sendMessage(chatId, addProduct(update.getMessage().getText()));
                    } else if (update.getMessage().isReply() && update.getMessage().getReplyToMessage().getText().equals("Cделай реплай этого сообщения и укажи название продукта, который ты хочешь удалить.")) {
                        sendMessage(chatId, deleteProductFirstTry(update.getMessage().getText()));
                    } else if (update.getMessage().isReply() && update.getMessage().getReplyToMessage().getText().equals("Ого! Оказывается, есть несколько продуктов с таким названием. " +
                            "Необходимо уточнить, какой из них ты имел в виду. " +
                            "В реплае на это сообщение укажи название и срок годности продукта, " +
                            "который ты хочешь удалить. Напиши эти параметры через запятую")) {
                        sendMessage(chatId, deleteProductSecondTry(update.getMessage().getText()));
                    } else if (update.getMessage().isReply() && update.getMessage().getReplyToMessage().getText().equals("Cделай реплай этого сообщения и укажи название рецепта, который ты хочешь добавить.")) {
                        sendMessage(chatId, addRecipeName(update.getMessage().getText()));
                    } else if (update.getMessage().isReply() && (update.getMessage().getReplyToMessage().getText().equals("Название рецепта успешно добавлено. В реплае на это сообщение укажи " +
                            "информацию о первом ингредиенте: название и количество через запятую.") || update.getMessage().getReplyToMessage().getText().equals("Ингредиент успешно добавлен в рецепт. Для того, чтобы добавить ещё ингредиент, " +
                            "сделай реплай на это сообщение и укажи название и количество ингредиента через запятую. Напиши 'Стоп', если все ингредиенты добавлены."))) {
                        sendMessage(chatId, addIngredient(update.getMessage().getText()));
                    } else if (update.getMessage().isReply() && update.getMessage().getReplyToMessage().getText().equals("Cделай реплай этого сообщения и укажи название рецепта, который ты хочешь удалить.")) {
                        sendMessage(chatId, deleteRecipe(update.getMessage().getText()));
                    } else if (update.getMessage().isReply() && update.getMessage().getReplyToMessage().getText().equals("Cделай реплай этого сообщения и укажи название рецепта, который ты хочешь приготовить.")) {
                        sendMessage(chatId, chooseRecipe(update.getMessage().getText()));
                    } else {
                        sendMessage(chatId, "Извините, такой комнады не существует");
                    }
                }
            }

        }
    }

    Recipe currentRecipe = null;

    private void findMinPurchasesCommandReceived(long chatId) {
        Map<String, Map<String, Double>> availableRecipes = virtualFridgeManager.findRecipeWithMinimalPurchases();
        StringBuilder output = new StringBuilder();
        if (!availableRecipes.isEmpty()) {
            for (String recipe : availableRecipes.keySet()) {
                output.append("Рецепт: ").append(recipe).append("\n")
                        .append("Необходимые дозакупки:").append("\n");
                for (String ingredient : availableRecipes.get(recipe).keySet()) {
                    output.append(ingredient)
                            .append(" ").append(availableRecipes.get(recipe).get(ingredient)).append("\n");
                }
            }
        } else {
            output.append("Нет рецептов для приготовления которых нужны дозакупки");
        }
        sendMessage(chatId, String.valueOf(output));
    }

    private String chooseRecipe(String name) {
        String output = "Рецепт приготовлен! Приятного аппетита!\nПродукты в холодильнике обновлены.";
        try {
            Recipe recipe = virtualFridgeManager.getRecipeManager().getRecipeDao().findRecipeByName(name);
            virtualFridgeManager.chooseRecipe(recipe);
        } catch (SQLException | SearchingException | NotAvailableRecipeException | ExpirationDateException e) {
            output = "Ой-ой... Произошла ошибка! Извини меня за это... Попробуй выполнить команду ещё раз";
        } catch (FridgeProductNumberException e) {
            output = "В холодильнике недостаточно продуктов для приготовления этого рецепта. " +
                    "Ты можешь узнать доступные рецепты с помощью команды /available.";
        }
        return output;
    }

    private void chooseRecipeCommandReceived(long chatId) {
        sendMessage(chatId, "Cделай реплай этого сообщения и укажи название рецепта, который ты хочешь приготовить.");
    }

    private void findAvailableRecipesCommandReceived(long chatId) {
        Set<Recipe> availableRecipes = virtualFridgeManager.findAvailableRecipes();
        String output = "Доступных к приготовлению рецептов нет.";
        if (!availableRecipes.isEmpty()) {
            output = showRecipes(availableRecipes);
        }
        sendMessage(chatId, output);
    }

    private void deleteRecipeCommandReceived(long chatId) {
        sendMessage(chatId, "Cделай реплай этого сообщения и укажи название рецепта, который ты хочешь удалить.");
    }

    private String deleteRecipe(String name) {
        String output = "Рецепт успешно удалён.";
        try {
            virtualFridgeManager.getRecipeManager().removeRecipe(name);
        } catch (SQLException | SearchingException e) {
            output = "Ой-ой... Произошла ошибка! Извини меня за это... Попробуй выполнить команду ещё раз";
        }
        return output;
    }

    private void showRecipesCommandReceived(long chatId) {
        StringBuilder output = new StringBuilder();
        RecipeBook recipeBook = virtualFridgeManager.getRecipeManager().getRecipeBook();
        output.append(showRecipes(recipeBook.getRecipes()));
        if (String.valueOf(output).isEmpty()) {
            output.append("К сожалению, в кулинарной книге нет рецептов... :(");
        }
        sendMessage(chatId, String.valueOf(output));
    }

    private String showRecipes(Set<Recipe> recipes) {
        StringBuilder output = new StringBuilder();
        for (Recipe recipe : recipes) {
            output.append("Рецепт: ").append(recipe.getName()).append("\n")
                    .append("Состав:").append("\n");
            for (Ingredient ingredient : recipe.getIngredientAmountCatalog().keySet()) {
                output.append(ingredient.name())
                        .append(" ").append(recipe.getIngredientAmountCatalog().get(ingredient)).append("\n");
            }
            output.append('\n');
        }
        return String.valueOf(output);
    }

    private String addRecipeName(String name) {
        String output = "Название рецепта успешно добавлено. В реплае на это сообщение укажи " +
                "информацию о первом ингредиенте: название и количество через запятую.";
        try {
            currentRecipe = virtualFridgeManager.getRecipeManager().addRecipeTitle(name);
        } catch (RecipeNameException e) {
            output = "Рецепт с таким названием уже существует";
        }
        return output;
    }

    private String addIngredient(String input) {
        String result = "Ингредиент успешно добавлен в рецепт. Для того, чтобы добавить ещё ингредиент, " +
                "сделай реплай на это сообщение и укажи название и количество ингредиента через запятую. Напиши 'Стоп', если все ингредиенты добавлены.";
        if (input.equals("Стоп")) {
            try {
                virtualFridgeManager.getRecipeManager().addToBook(currentRecipe);
                currentRecipe = null;
                result = "Рецепт успешно записан в кулинарную книгу";
            } catch (SQLException | SearchingException e) {
                result = "Ой-ой... Произошла ошибка! Извини меня за это... Попробуй выполнить команду ещё раз";
            }
        } else {
            String[] parts = input.split(",");
            if (parts.length == 2) {
                String productName = parts[0];
                double amount = Double.parseDouble(parts[1].trim());
                try {
                    virtualFridgeManager.getRecipeManager().addIngredientToRecipe(currentRecipe, productName, amount);
                } catch (AmountException e) {
                    result = "Упс! Кажется, что-то не так с количеством продукта! Количество продукта не может быть отрицательное.";
                } catch (SQLException e) {
                    result = "Ой-ой... Произошла ошибка! Извини меня за это... Попробуй выполнить команду ещё раз";
                }
            } else {
                result = "Упс! Данные, которые ты пытаешься ввести не соответствуют шаблону!";
            }
        }
        return result;
    }

    private void addRecipeCommandReceived(long chatId) {
        sendMessage(chatId, "Cделай реплай этого сообщения и укажи название рецепта, который ты хочешь добавить.");
    }

    private void startCommandReceived(long chatId, String name) {
        sendMessage(chatId, "Привет, " + name + "! Я твой виртуальный помощник в кулинарии и учете продуктов. " +
                "Я могу помочь тебе добавлять продукты в холодильник, добавлять твои рецепты в кулинарную книгу " +
                "и на основе доступных ингредиентов находить рецепты! И это ещё не все мои возможности... " +
                "Просто отправь мне команду, и я с удовольствием помогу тебе!\n\n" +
                "Если хочешь добавить новый продукт в холодильник, используй команду /addproduct.\n\n" +
                "Если хочешь посмотреть, что у тебя есть, используй команду /showproducts.\n\n" +
                "Ну а если ты захочешь удалить что-то из списка, просто вызови команду /deleteproduct. " +
                "Также у меня есть команды для добавления (/addrecipe), просмотра (/showrecipes) и удаления рецептов (/deleterecipe). " +
                "Для просмотра доступных к использованию рецептов используй команду /available. " +
                "И, конечно, чтобы выбрать рецепт для приготовления, используй команду /chooserecipe. \n" +
                "Не забудь проверить рецепты, для которых нужны минимальные дозакупки, с помощью команды /minimalpurchases.");
    }

    private String deleteProductFirstTry(String name) {
        String output = "Продукт успешно удалён";
        try {
            virtualFridgeManager.getFridgeProductManager().removeProductFromFridgeWithName(name);
        } catch (SQLException | SearchingException e) {
            output = "Ой-ой... Произошла ошибка! Извини меня за это... Попробуй выполнить команду ещё раз";
        } catch (FridgeProductNumberException e) {
            output = "Ого! Оказывается, есть несколько продуктов с таким названием. " +
                    "Необходимо уточнить, какой из них ты имел в виду. " +
                    "В реплае на это сообщение укажи название и срок годности продукта, " +
                    "который ты хочешь удалить. Напиши эти параметры через запятую";
        }
        return output;
    }

    private String deleteProductSecondTry(String input) {
        String result = "Продукт успешно удалён";
        String[] parts = input.split(",");
        if (parts.length == 2) {
            String productName = parts[0];
            String date = parts[1].trim();
            try {
                virtualFridgeManager.getFridgeProductManager().removeProductFromFridgeWithNameAndExpirationDate(productName, date);
            } catch (SQLException | SearchingException | FridgeProductNumberException e) {
                result = e.getMessage();
            } catch (ExpirationDateException e) {
                result = "Упс! Кажется, что-то не так со сроком годности продукта! " +
                        "Проверь соответсвие введенного срока годности шаблону дд.мм.гггг.";
            }
        } else {
            result = "Упс! Данные, которые ты пытаешься ввести не соответствуют шаблону!";
        }
        return result;
    }

    private String addProduct(String input) {
        String result = "Продукт успешно добавлен";
        String[] parts = input.split(",");
        if (parts.length == 3) {
            try {
                String productName = parts[0];
                double volume = Double.parseDouble(parts[1].trim());
                String date = parts[2].trim();
                virtualFridgeManager.getFridgeProductManager().addProductToFridge(productName, volume, date);
            } catch (SQLException | SearchingException e) {
                result = "Ой-ой... Произошла ошибка! Извини меня за это... Попробуй выполнить команду ещё раз";
            } catch (ExpirationDateException e) {
                result = "Упс! Кажется, что-то не так со сроком годности продукта! " +
                        "Проверь соответсвие введенного срока годности шаблону дд.мм.гггг. " +
                        "Также обратите внимание на то, что срок годности не может быть раньше текущей даты.";
            } catch (AmountException  e) {
                result = "Упс! Кажется, что-то не так с количеством продукта! Количество продукта не может быть отрицательное.";
            }catch (NumberFormatException e){
                result = "Данные, которые ты пытаешься ввести не соответствуют шаблону!";
            }
        } else {
            result = "Упс! Данные, которые ты пытаешься ввести не соответствуют шаблону!";
        }
        return result;
    }

    private void addProductCommandReceived(long chatId) {
        sendMessage(chatId, "Cделай реплай этого сообщения и укажи название, количество (просто число, без меры измерения), а также дату, когда заканчивается " +
                "срок годности продукта, который вы хотите добавить в холодильник. Укажите эти параметры через запятую. " +
                "Учти, что для введения дробного количества игредиента в качестве разделителя нужно " +
                "использовать точку. Срок годности нужно указать в формате дд.мм.ггг (например, 05.04.2024).\nПример ввода: Молоко, 1, 15.12.2023");
    }

    private void showProductsCommandReceived(long chatId) {
        StringBuilder output = new StringBuilder();
        try {
            ResultSet result = DatabaseConnectionManager.executeQuery("select * from fridge_product");
            while (result.next()) {
                output.append("Название: ").append(IngredientManager.ingredientDao.findIngredientById(result.getInt("ingredient_id")).name())
                        .append("\n").append("Количество: ").append(result.getDouble("amount"))
                        .append("\n").append("Годен до: ").append(DateEditor.timestampToString(result.getTimestamp("expiration_date")))
                        .append("\n\n");
            }
        } catch (SQLException | SearchingException e) {
            output = new StringBuilder("Ой-ой... Произошла ошибка! Извини меня за это... Попробуй выполнить команду ещё раз");
        }
        if (String.valueOf(output).isEmpty()) {
            output.append("К сожалению, в холодильнике нет продуктов... :(");
        }
        sendMessage(chatId, String.valueOf(output));
    }

    private void deleteProductsCommandReceived(long chatId) {
        sendMessage(chatId, "Cделай реплай этого сообщения и укажи название продукта, который ты хочешь удалить.");
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotUsername() {
        return "MyVirtualFridgeBot";
    }

}
