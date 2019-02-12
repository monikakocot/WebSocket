package pl.akademiakodu.superchat.models;

import lombok.extern.java.Log;
import org.apache.tomcat.jni.Local;
import org.apache.tomcat.jni.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import pl.akademiakodu.superchat.services.AdminOptionsService;
import pl.akademiakodu.superchat.services.UserList;
import pl.akademiakodu.superchat.services.UserLoginService;
import pl.akademiakodu.superchat.services.UserOptionsService;


import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

@EnableWebSocket
@Component
@Log
public class ChatSocket extends TextWebSocketHandler implements WebSocketConfigurer {

    @Autowired
    UserList userList;
    @Autowired
    UserLoginService userLoginService;
    @Autowired
    UserOptionsService userOptionsService;
    @Autowired
    AdminOptionsService adminOptionsService;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry
                .addHandler(this, "/chat")
                // chat - end point - the place where clients will be connected
                //this - TextWebSocketHandler
                .setAllowedOrigins("*"); // who can connect with out Websocket - * - every host. We can put also specific adress IP
        //. addInterceptors  - Inteceptor e.g. we can do sth before connect sb to chat: save user to database / to session,
        //check if users is not banned
        //.withSockJS - popular method
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Ktoś dołączył do chatu :D"); // checking at the begging if working well
        //userList.add(session);
        userList.add(new UserChatModel(session)); // arg only session cause nickname will be given like first message

        UserChatModel userChatModel = userList.findUserBySessionId(session).get();
        userChatModel.sendMessage("Witaj na chacie!");
        userChatModel.sendMessage("Twoja pierwsza wiadomość będzie Twoim nickiem :)");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        //userList.remove(session);
        userList.remove(userList.findUserBySessionId(session).get());
        log.info("Ktoś opuścił Chat");
        System.out.println("Ktoś opuścił Chat");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        System.out.println("Wiadomość: " + message.getPayload()); // checking at the begging if working well
        UserChatModel sender = userList.findUserBySessionId(session).get();

        if(userLoginService.checkFirstMessage(session, message, sender))
            return;
        if (userOptionsService.isRaisedMaxMessages(sender)) {
            return;
        }

        if(System.currentTimeMillis() - sender.getKickedTime().getMinute() > 1)
        adminOptionsService.checkIfMessageIsCommand(sender, message);

        if (message.getPayload().equals("archiwum")) {
            userOptionsService.printHistory(sender);
            System.out.println("Wyświetlono archiwum wiadomości");
            return;
        }

        if (message.getPayload().isEmpty()) {
            sender.getSession().sendMessage(new TextMessage("Nie możesz wysłać pustej wiadomosci"));
            System.out.println("Nie możesz wysłać pustej wiadomości");
            return;
        }
        if (sender.isEmpty()) {
            sender.getSession().sendMessage(new TextMessage("Nie mozesz pisac"));
            // becouse admin made him mute (isEmpty==true)
            return;
        }

        if (sender.isBanned()) {
            sender.getSession().sendMessage(new TextMessage("Nie mozesz pisac. Jesteś zbanowany!"));
            // becouse admin made him mute (isBanned==true)
            LocalTime now = LocalTime.now();
            LocalTime banned = sender.getKickedTime();
            sender.sendMessage("Musisz poczekać jeszcze: " + (30- Duration.between(banned,now).toMillis()/10000) + "sekund" );
            if(Duration.between(banned,now).toMillis()/10000 > 30){
                sender.setBanned(false);
                sender.setCounter(0);
                return;
            }
            return;
        }

        userOptionsService.sendMessageToAll(sender.getNickname() + ": " + message.getPayload());
        userOptionsService.addMessageToArchive(message.getPayload());

    }

}

/* Another version of printHistory:
    private void sendMessageArchiveToUser(UserChatModel sender) throws IOException {
        for (String lastTenMessage : lastTenMessages) {
            sender.getSession().sendMessage(new TextMessage(lastTenMessage));
        }
    }
*/


