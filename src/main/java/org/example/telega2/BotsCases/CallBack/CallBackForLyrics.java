package org.example.telega2.BotsCases.CallBack;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Map;
import java.util.Objects;

public class CallBackForLyrics {
    public static SendMessage getLyrics(String Url, Map<Long,String> answer, String current, RestTemplate restTemplate, long chatId, long message_id, TelegramClient telegramClient){

        System.out.printf("%s/api/audio_lyrics/%d%n", Url, answer.entrySet().stream().filter(entry -> entry.getValue().equals(current)).map(Map.Entry::getKey)
                .findFirst().get());
        ResponseEntity<String> response = restTemplate.getForEntity(String.format("%s/api/audio_lyrics/%d", Url, answer.entrySet().stream().filter(entry -> entry.getValue().equals(current)).map(Map.Entry::getKey)
                .findFirst().get()), String.class);
        EditMessageReplyMarkup editMarkup = new EditMessageReplyMarkup();
        editMarkup.setChatId(chatId);
        editMarkup.setMessageId((int) message_id);
        editMarkup.setReplyMarkup(null);
        try {
            telegramClient.execute(editMarkup); // Удаляем клавиатуру
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(Objects.requireNonNullElse(response.getBody(), "нету слов"))
                .build();

    }
}
