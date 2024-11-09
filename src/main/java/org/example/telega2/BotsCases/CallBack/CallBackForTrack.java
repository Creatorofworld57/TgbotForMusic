package org.example.telega2.BotsCases.CallBack;

import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import static java.lang.Math.toIntExact;


public class CallBackForTrack {

    public static String TracksHandler(Update update, Map<Long,String> answer, String Url, TelegramClient telegramClient,long chat_id){
      String  current = update.getCallbackQuery().getData();

        System.out.println("hasCallBack");
        // Find the track key in 'tracks' list
        answer.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(update.getCallbackQuery().getData()))
                .map(Map.Entry::getKey)
                .findFirst()
                .ifPresentOrElse(
                        foundKey -> {
                            String audioUrl = String.format("%s/api/audio/",Url) + foundKey;
                            System.out.println(audioUrl);

                            try {
                                // Create InputFile and SendAudio to send the track
                                InputFile inputFile = getAudioInputFile(audioUrl);
                                SendAudio audioMessage = SendAudio.builder()
                                        .chatId(String.valueOf(chat_id))
                                        .audio(inputFile)
                                        .replyMarkup(new InlineKeyboardMarkup(Collections.singletonList(new InlineKeyboardRow(InlineKeyboardButton.builder()
                                                .text("Слова")
                                                .callbackData("words")
                                                .build()))))
                                        .build();
                                    System.out.println("success");
                                telegramClient.execute(audioMessage);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        },
                        () -> System.out.println("Track not found for the given message text.")
                );
        //удаляем map с треками
        //answer.clear();
        long message_id = update.getCallbackQuery().getMessage().getMessageId();
        EditMessageText new_message = EditMessageText.builder()
                .chatId(chat_id)
                .messageId(toIntExact(message_id))
                .text(update.getCallbackQuery().getData())
                .build();
        try {
            telegramClient.execute(new_message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

       /* SendMessage messageWord = SendMessage.builder()
                .chatId(String.valueOf(chatId))

                /* .replyMarkup(InlineKeyboardMarkup
                        .builder()
                        .keyboardRow(
                              keys
                        )
                .replyMarkup(new InlineKeyboardMarkup(Collections.singletonList(new InlineKeyboardRow(InlineKeyboardButton.builder()
                        .text("Слова")
                        .callbackData("words")
                        .build()))))

                .build();*/
        return current;
    }
    public static InputFile getAudioInputFile(String audioUrl) {
        try {
            // Открываем соединение по URL
            URL url = new URL(audioUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            connection.setConnectTimeout(15000); // 15 seconds for connection timeout
            connection.setReadTimeout(30000);

            // Проверка успешности соединения
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // Читаем данные в массив байтов
                byte[] audioBytes = connection.getInputStream().readAllBytes();

                // Создаем новый InputStream из массива байтов
                InputStream audioStream = new ByteArrayInputStream(audioBytes);

                // Создаем InputFile для Telegram с потоком и именем файла
                InputFile inputFile = new InputFile(audioStream, "audio.mp3");

                // Закрываем соединение (поток audioStream остается открытым)
                connection.disconnect();

                return inputFile;
            } else {
                System.out.println("Не удалось получить аудиофайл. Код ответа: " + connection.getResponseCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
