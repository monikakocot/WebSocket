package pl.akademiakodu.superchat.services;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import pl.akademiakodu.superchat.models.UserChatModel;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserList {

    List<UserChatModel> userList = new ArrayList<>();
    private Deque<String> lastTenMessages = new ArrayDeque<>();


    public List<UserChatModel> getUserList() {
        return userList;
    }

    public Deque<String> getLastTenMessages() {
        return lastTenMessages;
    }

    public void add(UserChatModel userChatModel){
        userList.add(userChatModel);
    }

    public Optional<UserChatModel> findUserBySessionId(WebSocketSession session){
        return userList.stream().filter(s -> s.getSession().equals(session)).findAny();
    }

    public Optional<UserChatModel> findUserByNickname(String nickname){
        return userList.stream()
                //.filter(s -> s.getNickname().equals(nickname)).findAny();
                .filter(s -> s.getNickname() != null && s.getNickname().equals(nickname))
                .findFirst();
    }

    public void remove(UserChatModel userChatModel){
        userList.remove(userChatModel);
    }

    public List<String> getNickNames(){
        return userList.stream().map(s -> s.getNickname()).collect(Collectors.toList());
    }
}
