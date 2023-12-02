package ru.barkhatnat;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.barkhatnat.service.TelegramBot;

public class Main {
    public static void main(String[] args) throws TelegramApiException {
        final TelegramBot telegramBot = new TelegramBot("6819922923:AAFfIhzwbtWzr2EVZv7MHzJmEy-V_Uds4Qs");
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            botsApi.registerBot(telegramBot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
