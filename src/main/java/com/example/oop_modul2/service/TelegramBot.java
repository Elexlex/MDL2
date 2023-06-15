package com.example.oop_modul2.service;

import com.example.oop_modul2.config.BotConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;
    public TelegramBot(BotConfig config){
        this.config = config;
    }
    @Override
    public void onUpdateReceived(Update update) {

        if(update.hasMessage() && update.getMessage().hasText()){
            String messageText = update.getMessage().getText();

            long chatId = update.getMessage().getChatId();


            String spellName = null;
            String firstPart;

            String[] parts = messageText.split(" ", 2);

            if (parts.length == 1) {

                firstPart = parts[0];

            } else {
                firstPart = parts[0];
                spellName = parts[1];
            }





            if(messageText.equals("/start")){
                startCommand(chatId, update.getMessage().getChat().getFirstName());
            }else if(firstPart.equals("/spell")){
                spellCommand(chatId, spellName);
            }else{
                sendMessage(chatId,"Unknown command");
            }
        }

    }

    private void spellCommand(long chatId, String spell){

        if(spell==null){
            sendMessage(chatId,"Wrong spell");
        }else {
            sendMessage(chatId, spellSearchURL(spell));

        }


    }

    private String spellSearchURL(String name) {
        String spellInfo = "";
        char[] changeName = new char[]{'_', ' '};
        try {
            String modifiedName = URLEncoder.encode(name.replaceAll(String.valueOf(changeName), "-"), StandardCharsets.UTF_8);
            String searchUrl = "http://dnd5e.wikidot.com/spell:" + modifiedName;

            HttpURLConnection searchConnection = (HttpURLConnection) new URL(searchUrl).openConnection();
            searchConnection.setRequestMethod("GET");

            int searchResponseCode = searchConnection.getResponseCode();

            if (searchResponseCode == HttpURLConnection.HTTP_OK) {
                spellInfo = searchUrl;
            } else {
                spellInfo = "Failed to search spell. Error code: " + searchResponseCode;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return spellInfo;
    }

    private void startCommand(long chatId,String name){
        String ans = "Hi, " + name + ", please type a spell from D&D 5e and I will get you a link to it \"/spell spell name\"";

        sendMessage(chatId,ans);
    }

    private void sendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);

        try{
            execute(message);
        }catch (TelegramApiException e){
            e.printStackTrace();
        }
    }
    @Override
    public String getBotToken(){
        return config.getToken();
    }
    @Override
    public String getBotUsername() {
        return config.getBotName();
    }
}
