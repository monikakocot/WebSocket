package pl.akademiakodu.superchat.models;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalTime;
import java.util.Objects;

@Data // gettery, settery, equals, toString itp.. https://projectlombok.org/features/all
@NoArgsConstructor
public class UserChatModel {

    private WebSocketSession session; //session of User
    private String nickname;
    private boolean isAdmin;
    private boolean isEmpty;

    private boolean isBanned = false;
    private LocalTime kickedTime;
    private long time;
    private int counter;


    /*public UserChatModel(WebSocketSession session) {
        this.session = session;
    }*/

    public UserChatModel(WebSocketSession session) {
        this.session = session;
        this.counter = 0;
        this.kickedTime = LocalTime.now().minusMinutes(1);
    }

    public UserChatModel(WebSocketSession session, String nickname) {
        this.session = session;
        this.nickname = nickname;
        this.counter = 0;
        this.kickedTime = LocalTime.now().minusMinutes(1);
    }

    public void sendMessage (String message) throws IOException{
        session.sendMessage(new TextMessage(message));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserChatModel userModel = (UserChatModel) o;
        return Objects.equals(session.getId(), userModel.session.getId()) &&
                Objects.equals(nickname, userModel.nickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(session.getId(), nickname);
    }
}
