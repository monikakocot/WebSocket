package pl.akademiakodu.superchat.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.akademiakodu.superchat.models.UserChatModel;
import java.io.IOException;
import java.time.LocalTime;


@Service
public class UserOptionsService {

    @Autowired
    UserList userList;

    public void sendMessageToAll(String message) throws IOException {
        for (UserChatModel userModel : userList.getUserList()) {
            userModel.sendMessage(message); //send message to every user (here even to person who wrote message)
        }
    }

    public boolean isNickFree(String nickname) {
        return userList.getUserList()
                .stream()
                .filter(s -> s.getNickname() != null)
                .noneMatch(s -> s.getNickname().equals(nickname));
    }


    public void addMessageToArchive(String message) {
        if (userList.getLastTenMessages().size() >= 10) {
            userList.getLastTenMessages().pollFirst();
        }

        userList.getLastTenMessages().addLast(message);
    }

    public void printHistory(UserChatModel sender) {

        try {
            sender.sendMessage("Ostatnie 10 wiadomości:");
            for (String s : userList.getLastTenMessages()) {
                sender.sendMessage(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isRaisedMaxMessages(UserChatModel sender) throws IOException {
        if(sender.getCounter() == 0) {
            sender.setTime(System.currentTimeMillis());
            sender.setCounter(sender.getCounter() + 1);
        }
        else if(System.currentTimeMillis() - sender.getTime() < 60000 ){
            if(sender.getCounter() < 14)
                sender.setCounter(sender.getCounter() + 1);
            else {
                sender.setKickedTime(LocalTime.now().plusMinutes(1));
                sender.setCounter(0);
                sender.sendMessage("Zostałeś zbanowany na 1 minutę");
                sender.setBanned(true);
                //sender.setEmpty(true);
                System.out.println("BAN!");
                return true;
            }
        }
        else{
            sender.setCounter(0);
            sender.setTime(System.currentTimeMillis());
        }
        return false;
    }
}
