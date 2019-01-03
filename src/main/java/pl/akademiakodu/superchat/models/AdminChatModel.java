package pl.akademiakodu.superchat.models;


import org.springframework.web.socket.WebSocketSession;
import java.io.IOException;

public class AdminChatModel extends UserChatModel {

   /* @Autowired*
    UserListService userListService;*/

    private String password;

    public AdminChatModel(WebSocketSession session, String nickname, String password) {
        super(session, nickname);
        this.password = password;
    }
    public AdminChatModel() {
    }

    @Override
    public void sendMessage(String message) throws IOException {
        super.sendMessage(message);
    }

    //GETTERS, SETTERS

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }



}
