package ru.barkhatnat.classes.managers;

import ru.barkhatnat.exceptions.ExpirationDateException;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class DateEditor {
    public static Timestamp stringToTimestamp(String date) throws ExpirationDateException {
        if (!isValidExpirationDate(date)) {
            throw new ExpirationDateException("Illegal date format");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate localDate = LocalDate.parse(date, formatter);
        Timestamp timestamp = Timestamp.valueOf(localDate.atStartOfDay());
        if (checkExpirationDate(timestamp)) {
            return Timestamp.valueOf(localDate.atStartOfDay());
        }
        throw new ExpirationDateException("Expiration date can't be before today");
    }
    public static String timestampToString(Timestamp timestamp) {
        LocalDateTime localDateTime = timestamp.toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return localDateTime.format(formatter);
    }
    public static boolean checkExpirationDate(Timestamp date) {
        return date.after(Timestamp.valueOf(LocalDateTime.now()));
    }

    private static boolean isValidExpirationDate(String date) {
        String pattern = "\\d{2}\\.\\d{2}\\.\\d{4}";
        return Pattern.matches(pattern, date);
    }

}
