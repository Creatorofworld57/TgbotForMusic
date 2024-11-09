package org.example.telega2;

import lombok.RequiredArgsConstructor;
import org.example.telega2.BotsCases.CallBack.CallBackForLyrics;
import org.example.telega2.BotsCases.CallBack.CallBackForTrack;
import org.example.telega2.BotsCases.SEARCH;
import org.example.telega2.Models.Audio;
import org.example.telega2.Models.UserState;
import org.example.telega2.Repositories.UserRepository;
import org.example.telega2.Utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.*;

import static org.example.telega2.BotsCases.CallBack.CallBackForTrack.getAudioInputFile;


@Component
@RequiredArgsConstructor
public class UniversalTelegramBot implements SpringLongPollingBot {

    @Value("${botToken}")
    private String botToken;

    @Value("${url}")
    String Url;
    final TelegramClient telegramClient;
    @Override
    public String getBotToken() {
        return botToken;
    }
    final RestTemplate restTemplate;
    final UserRepository repositoryOfUser;

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        final Logger logger = LoggerFactory.getLogger(UniversalTelegramBot.class);


        return (LongPollingSingleThreadUpdateConsumer) update -> {
         Optional<UserState> userOpt = repositoryOfUser.findByChatId(update.hasMessage()? update.getMessage().getChatId() : update.getCallbackQuery().getMessage().getChatId());
            UserState user;
        if(userOpt.isEmpty()){
                user = new UserState();
                user.setChatId(update.getMessage().getChatId());
                user.setConstants(Constants.NOTHING);
                user.setAnswer(new HashMap<>(Map.of(45L,"asds")));
                user.setTracks(new ArrayList<>(List.of(new Audio())));
                repositoryOfUser.save(user);
            }
            else
                user=userOpt.get();



            List<Audio> tracks = user.getTracks();

            final SendMessage[] message = new SendMessage[1];
            final SendAudio[] audio = new SendAudio[1];
            final long[] chatId = {user.getChatId()};


            if (update.hasMessage() && update.getMessage().hasText()) {
                var answer = user.getAnswer();
                if(!answer.isEmpty())
                     answer.clear();
                user.setAnswer(answer);

                String messageText = update.getMessage().getText();

                if (Objects.equals(update.getMessage().getText(), "/start")) {
                    user.setConstants(Constants.NOTHING);
                    message[0] = SendMessage.builder()
                            .chatId(String.valueOf(chatId[0]))
                            .text(("""
                                    Бот поддерживает следующие команды:
                                    /echo - дублирует ваше сообщение\s
                                    /currentTrack - показывает текущий трек на сайте\s
                                    /end - если ...
                                    """
                            ))
                            .build();
                    repositoryOfUser.save(user);
                } else if (Objects.equals(messageText, "/current_track")) {
                    user.setConstants(Constants.NOTHING);

                    System.out.println(update.getMessage().getChat().getUserName());
                    System.out.println(update.getMessage().getChat().getFirstName());
                    System.out.println(update.getMessage().getChat().getLastName());

                    // Fetch current audio track URL or ID from your backend
                    ResponseEntity<String> response = restTemplate.getForEntity(String.format("%s/api/currentAudio", Url), String.class);

                    if (response.getBody() != null) {
                        // Use InputFile directly with the URL
                        SendMessage alert = SendMessage.builder()
                                .chatId(chatId[0])
                                .text("Подождите пару секунд (а может и больше, я хуй знает)")
                                .build();
                        try {
                            telegramClient.execute(alert);
                        } catch (TelegramApiException e) {
                            logger.error("Failed to execute Alert  from message with ID: {}", update.getMessage().getChat().getId(), e);
                        }
                        InputFile inputFile = getAudioInputFile(String.format("%s/api/audio/", Url) + response.getBody().substring(0, response.getBody().indexOf("/")));
                        System.out.println(String.format("%s/api/audio/", Url) + response.getBody().substring(0, response.getBody().indexOf("/")));
                        if (inputFile != null) {
                            audio[0] = SendAudio.builder()
                                    .chatId(chatId[0] = update.getMessage().getChatId())
                                    .audio(inputFile)
                                    .caption(response.getBody().substring(response.getBody().indexOf("/") + 1))
                                    .build();
                            try {
                                telegramClient.execute(audio[0]);
                            } catch (TelegramApiException e) {
                                System.out.println("Current track е загрузился");
                                logger.error("Failed to execute Audio from command /currentAudio from message with ID: {}", update.getMessage().getChat().getId(), e);
                            }
                        } else logger.error("Failed to execute Input file in /currentTrack");
                    }
                    repositoryOfUser.save(user);
                } else if (Objects.equals(update.getMessage().getText(), "/echo")) {
                    user.setConstants(Constants.ECHO);

                    message[0] = SendMessage.builder()
                            .chatId(String.valueOf(chatId[0]))
                            .text(messageText)
                            .build(); repositoryOfUser.save(user);
                } else if (Objects.equals(update.getMessage().getText(), "/search")) {
                    user.setConstants(Constants.SEARCH);

                    message[0] = SendMessage.builder()
                            .chatId(String.valueOf(chatId[0]))
                            .text("Введите название трека или автора")
                            .build();
                    repositoryOfUser.save(user);
                }
                else {
                    if (user.getConstants() == Constants.ECHO) {
                        message[0] = SendMessage.builder()
                                .chatId(String.valueOf(chatId[0]))
                                .text(messageText)
                                .build();

                    } else if (user.getConstants()  == Constants.SEARCH) {
                      message[0] = SEARCH.Search(restTemplate, Url, update, user, chatId[0],repositoryOfUser);

                    } else {
                        message[0] = SendMessage.builder()
                                .chatId(String.valueOf(chatId[0]))
                                .text(("""
                                        Введите что-то корректное\s
                                                                        
                                        Бот поддерживает следующие команды:
                                        /echo - дублирует ваше сообщение\s
                                        /currentTrack - показывает текущий трек на сайте"""
                                ))
                                .build();
                        repositoryOfUser.save(user);
                    }

                }
                try {
                    telegramClient.execute(message[0]);
                } catch (TelegramApiException e) {
                    logger.error("Failed to execute variable message from message with ID: {}", update.getMessage().getChat().getId(), e);
                }

            } else if (update.hasCallbackQuery()) {
                user.setConstants(Constants.NOTHING);
                System.out.println(user.getAnswer());
                if (user.getAnswer().containsValue(update.getCallbackQuery().getData())) {
                    user.setCurrent(CallBackForTrack.TracksHandler(update, user.getAnswer(), Url, telegramClient, user.getChatId()));
                    repositoryOfUser.save(user);
                }
                else if ((update.getCallbackQuery().getData().equals("words"))) {
                    long message_id = update.getCallbackQuery().getMessage().getMessageId();
                    message[0] = CallBackForLyrics.getLyrics(Url,user.getAnswer(),user.getCurrent(), restTemplate, user.getChatId(), message_id, telegramClient);
                    try {
                        telegramClient.execute(message[0]); // Удаляем клавиатуру
                    } catch (TelegramApiException e) {
                        logger.error("Failed to execute Lyrics  from message with ID: {}", update.getMessage().getChat().getId(), e);
                    }
                    repositoryOfUser.save(user);
                }
                else
                System.out.println(user.getAnswer());
            }
        };
    }


}
