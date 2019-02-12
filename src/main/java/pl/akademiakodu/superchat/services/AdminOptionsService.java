package pl.akademiakodu.superchat.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import pl.akademiakodu.superchat.models.AdminChatModel;
import pl.akademiakodu.superchat.models.UserChatModel;

import java.io.IOException;
import java.util.Optional;

@Service
public class AdminOptionsService {

    @Autowired
    UserList userList;

    public void checkIfMessageIsCommand(UserChatModel sender, TextMessage message) throws IOException {
        if (!isAdmin(sender)) {
            return;
        }

        if (message.getPayload().startsWith("/mute")) {
            String nick = message.getPayload().split(" ")[1];

            if(userList.findUserByNickname(nick).isPresent()){

                Optional<UserChatModel> userToMuteOptional = userList.findUserByNickname(nick);
                UserChatModel userToMute;
                userToMute = userToMuteOptional.get();

                userToMute.setBanned(true);
                sender.getSession().sendMessage(new TextMessage("Zmutowałeś poprawnie użytkownika: " + nick));
                userToMute.getSession().sendMessage(new TextMessage("Zostałeś zmutowany!@!@!@"));

            }

            else if (!userList.getUserList().contains(userList.findUserByNickname(nick))) {
                //if(!userToMuteOptional.isPresent()){

                sender.getSession().sendMessage(new TextMessage("Podany nick nie istnieje"));
                System.out.println("Podany nick nie istnieje");
                return;
            }
        }

        if (message.getPayload().startsWith("/unmute")) {
            String nick = message.getPayload().split(" ")[1];

            if(userList.findUserByNickname(nick).isPresent()){

                Optional<UserChatModel> userToMuteOptional = userList.findUserByNickname(nick);
                UserChatModel userToMute;
                userToMute = userToMuteOptional.get();

                userToMute.setBanned(false);
                sender.getSession().sendMessage(new TextMessage("Odblokowałeś poprawnie użytkownika: " + nick));
                userToMute.getSession().sendMessage(new TextMessage("Zostałes odblokowany!@!@!@"));

            }

            else if (!userList.getUserList().contains(userList.findUserByNickname(nick))) {
                //if(!userToMuteOptional.isPresent()){

                sender.getSession().sendMessage(new TextMessage("Podany nick nie istnieje"));
                System.out.println("Podany nick nie istnieje");
                return;
            }
        }
    }

    public boolean isAdmin(UserChatModel userChatModel) {
        if (userChatModel instanceof AdminChatModel){
            return true;
        }
        return false;
    }

}
