package org.example.telega2.BotsCases;

import org.example.telega2.Models.Audio;
import org.example.telega2.Repositories.UserRepository;
import org.example.telega2.Models.UserState;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SEARCH {
    public static SendMessage Search(RestTemplate restTemplate, String Url, Update update, UserState user, long chatId, UserRepository repository) {
        String url = String.format("%s/api/searchOfTrack/", Url) + update.getMessage().getText();
        List<Audio> tracks;
        var answer = user.getAnswer();
        SendMessage message;
        ResponseEntity<List<Audio>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        tracks = response.getBody();

        if (tracks == null || Objects.requireNonNull(tracks).isEmpty()) {
            message = SendMessage.builder()
                    .chatId(String.valueOf(chatId))
                    .text("Такого трека еще нет в базе данных или вы ввели что-то некорректное")
                    .allowSendingWithoutReply(true)
                    .build();
        } else {
            //кладем в мапу треки из ответа сервера
            tracks.forEach(i -> answer.put(i.getId(), i.getAuthor() + " - " + i.getName()));
            List<InlineKeyboardRow> rowsInline = new ArrayList<>();

            List<String> answerList = new ArrayList<>(user.getAnswer().values());
            //создаем клавиатура для выбора треков
            for (String s : answerList) {
                // Добавляем кнопку в строку
                InlineKeyboardRow row = new InlineKeyboardRow(InlineKeyboardButton.builder()
                        .text(s)
                        .callbackData(s)
                        .build());

                rowsInline.add(row); // Добавляем строку в список строк
            }

            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(rowsInline);

            /*  keys.addAll(answer.values().stream().toList());*/
            message = SendMessage.builder()
                    .chatId(String.valueOf(chatId))
                    .text("Выберите трек")
                    /*.replyMarkup(InlineKeyboardMarkup
                            .builder()
                            .keyboardRow(
                                  keys
                            )*/
                    .replyMarkup(inlineKeyboardMarkup)
                    .allowSendingWithoutReply(true)
                    .build();

        }
        //сохраняем мапу треков для пользователя
        user.setAnswer(answer);
        System.out.println(user.getAnswer());
        repository.save(user);

        return message;
                    /* message = SendMessage.builder()
                             .chatId(String.valueOf(chatid))
                             .text(String.join("\n", answer))
                             .parseMode("HTML")
                             .allowSendingWithoutReply(true)
                          .build();*/

    }


}
