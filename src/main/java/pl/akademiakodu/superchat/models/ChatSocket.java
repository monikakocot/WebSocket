package pl.akademiakodu.superchat.models;

import lombok.extern.java.Log;
import org.apache.tomcat.jni.Time;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;


import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

@EnableWebSocket
@Component
@Log
public class ChatSocket extends TextWebSocketHandler implements WebSocketConfigurer {

    //List<WebSocketSession> userList = new ArrayList<>();
    List<UserChatModel> userList = new ArrayList<>(); // now user will be UserChatModel
    private Deque<String> lastTenMessages = new ArrayDeque<>();

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

        UserChatModel userChatModel = findUserBySessionId(session);
        userChatModel.sendMessage("Witaj na chacie!");
        userChatModel.sendMessage("Twoja pierwsza wiadomość jest Twoim nickiem");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        //userList.remove(session);
        userList.removeIf(s -> s.getSession().getId().equals(session.getId()));
        log.info("Ktoś opuścił Chat");
        System.out.println("Ktoś opuścił Chat");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        System.out.println("Wiadomość: " + message.getPayload()); // checking at the begging if working well
        UserChatModel sender = findUserBySessionId(session);

        if (sender.getNickname() == null) {

            if (!isNickFree(message.getPayload()) || message.getPayload().equals("ADMIN")) {
                sender.getSession().sendMessage(new TextMessage("Ten nick jest zajęty"));
                return;

            } else if (message.getPayload().equals("amanda")) {
                sender.setNickname(message.getPayload());
                sender.getSession().sendMessage(new TextMessage("Wpisz hasło"));
                return;

            } else {
                //userList.add(new UserChatModel(session,message.getPayload()));
                sender.setNickname(message.getPayload());
                sender.sendMessage("Ustawiono Twój nick");
                return;
            }
        } else if (sender.getNickname().equals("amanda")) {

            if (message.getPayload().equals("password")) {
                AdminChatModel adminChatModel = new AdminChatModel(sender.getSession(), "ADMIN", message.getPayload());
                sender.setAdmin(true);
                userList.remove(sender);
                userList.add(adminChatModel);
                sender.getSession().sendMessage(new TextMessage("Zostałeś adminem"));
                return;
            } else {
                sender.getSession().sendMessage(new TextMessage("Złe hasło"));
                sender.getSession().sendMessage(new TextMessage("Podaj swój nick"));
                userList.remove(sender);
                System.out.println("usunięto usera");
                userList.add(new UserChatModel(session));
                return;

            }
        }


        checkIfMessageIsCommand(sender, message);
        if (isRaisedMaxMessages(sender)) {
            sender.getSession().sendMessage(new TextMessage("Zostałeś zbanowany!"));
            sender.setBanned(true);
            sender.setEmpty(true);
            System.out.println("BAN!");
            return;
        }

        if (sender.isEmpty()) {
            sender.getSession().sendMessage(new TextMessage("Nie mozesz pisac"));
            // becouse admin made him mute (isEmpty==true)
            return;
        }

        if (message.getPayload().equals("archiwum")) {
            printHistory(sender);
            System.out.println("Wyświetlono archiwum wiadomości");
            return;
        }

        if (message.getPayload().isEmpty()) {
            sender.getSession().sendMessage(new TextMessage("Nie mozesz wysłać pustej wiadomosci"));
            System.out.println("Nie możesz wysłać pustej wiadomości");
            return;
        }

       /* for (UserChatModel user : userList) {
            user.getSession()
                    .sendMessage(new TextMessage(sender.getNickname() + ": " + message.getPayload()));
        }*/

        sendMessageToAll(sender.getNickname() + ": " + message.getPayload());
        addMessageToArchive(message.getPayload());

    }// koniec metody


    private void sendMessageToAll(String message) throws IOException {
        for (UserChatModel userModel : userList) {
            userModel.sendMessage(message); //send message to every user (here even to person who wrote message)
        }
    }

    private UserChatModel findUserBySessionId(WebSocketSession session) {

        return userList.stream()
                .filter(s -> s.getSession().getId().equals(session.getId()))
                .findAny().get(); //get() cause finAny return Optional type (not UserChatModel)
    }

    private Optional<UserChatModel> findUserByNick(String nickname) {
        return userList.stream()
                .filter(s -> s.getNickname() != null && s.getNickname().equals(nickname))
                .findFirst();
    }

    private boolean isNickFree(String nickname) {
        return userList
                .stream()
                .filter(s -> s.getNickname() != null)
                .noneMatch(s -> s.getNickname().equals(nickname));
    }

    private void checkIfMessageIsCommand(UserChatModel sender, TextMessage message) throws IOException {
        if (!sender.isAdmin()) {
            return;
        }

        if (message.getPayload().startsWith("/mute")) {
            String nick = message.getPayload().split(" ")[1];
            Optional<UserChatModel> userToMuteOptional = findUserByNick(nick);

            UserChatModel userToMute;
            userToMute = userToMuteOptional.get();

            if (!userList.contains(userToMute)) {
                //if(!userToMuteOptional.isPresent()){

                sender.getSession().sendMessage(new TextMessage("Podany nick nie istnieje"));
                System.out.println("Podany nick nie istnieje");
                return;
            }

            userToMute.setEmpty(true);

            sender.getSession().sendMessage(new TextMessage("Zmutowales popranie"));
            userToMute.getSession().sendMessage(new TextMessage("Zostales zmutowany!@!@!@"));
        }
    }

    private void addMessageToArchive(String message) {
        if (lastTenMessages.size() >= 10) {
            lastTenMessages.pollFirst();
        }

        lastTenMessages.addLast(message);
    }

    private void printHistory(UserChatModel sender) {

        try {
            sender.sendMessage("Ostatnie 10 wiadomości:");
            for (String s : lastTenMessages) {
                sender.sendMessage(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isRaisedMaxMessages(UserChatModel userChatModel){
        if(userChatModel.getCounter() == 0) {
            userChatModel.setTime(System.currentTimeMillis());
            userChatModel.setCounter(userChatModel.getCounter() + 1);
        }
        else if(System.currentTimeMillis() - userChatModel.getTime() < 60000 ){
            if(userChatModel.getCounter() < 14)
                userChatModel.setCounter(userChatModel.getCounter() + 1);
            else {
                userChatModel.setKickedTime(LocalTime.now().plusMinutes(1));
                userChatModel.setCounter(0);
                return true;
            }
        }
        else{
            userChatModel.setCounter(0);
            userChatModel.setTime(System.currentTimeMillis());
        }
        return false;
    }
}

/* Another version og printHistory:
    private void sendMessageArchiveToUser(UserChatModel sender) throws IOException {
        for (String lastTenMessage : lastTenMessages) {
            sender.getSession().sendMessage(new TextMessage(lastTenMessage));
        }
    }
*/


