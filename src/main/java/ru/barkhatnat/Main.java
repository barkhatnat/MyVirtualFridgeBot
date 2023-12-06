package ru.barkhatnat;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.barkhatnat.db.DatabaseConnectionManager;
import ru.barkhatnat.db.DatabaseCreator;
import ru.barkhatnat.service.TelegramBot;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws TelegramApiException {
        final TelegramBot telegramBot = new TelegramBot("6819922923:AAH2kinTWBb1Fd7CeiDNtnKzGC5c_WgDmOM");
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            DatabaseConnectionManager.openConnection();
            DatabaseCreator.create();
            botsApi.registerBot(telegramBot);
        } catch (TelegramApiException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
