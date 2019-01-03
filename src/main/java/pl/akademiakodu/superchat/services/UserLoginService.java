package pl.akademiakodu.superchat.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import pl.akademiakodu.superchat.models.AdminChatModel;
import pl.akademiakodu.superchat.models.UserChatModel;

import java.io.IOException;

@Service
public class UserLoginService {

    @Autowired
    UserOptionsService userOptionsService;
    @Autowired
    UserList userList;

    public boolean checkFirstMessage(WebSocketSession session, TextMessage message, UserChatModel sender) throws IOException {

        if (sender.getNickname() == null) {

            if (!userOptionsService.isNickFree(message.getPayload()) || message.getPayload().equals("ADMIN")) {
                sender.sendMessage("Ten nick jest zajęty");
                return true;

            } else if (message.getPayload().equals("amanda")) {
                sender.setNickname(message.getPayload());
                sender.sendMessage("Wpisz hasło");
                return true;

            } else {

                sender.setNickname(message.getPayload());
                sender.sendMessage("Ustawiono Twój nick");
                return true;
            }
        } else if (sender.getNickname().equals("amanda")) {

            if (message.getPayload().equals("password")) {
                AdminChatModel adminChatModel = new AdminChatModel(sender.getSession(), "ADMIN", message.getPayload());
                sender.setAdmin(true);
                userList.remove(sender);
                userList.add(adminChatModel);
                sender.getSession().sendMessage(new TextMessage("Zostałeś adminem"));
                return true;
            } else {
                sender.getSession().sendMessage(new TextMessage("Złe hasło"));
                sender.getSession().sendMessage(new TextMessage("Podaj swój nick"));
                userList.remove(sender);
                System.out.println("usunięto usera");
                userList.add(new UserChatModel(session));
                return true;
            }
        }
        return false;
    }
}
