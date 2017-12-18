package demo.netty;

import org.msgpack.annotation.Message;

import java.io.Serializable;

/**
 * Created by xupingxia on 17-10-23.
 */
@Message
public class UserInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userName;
    private int userID;

    public UserInfo() {
    }

    public UserInfo buildUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public UserInfo buildUserID(int userID) {
        this.userID = userID;
        return this;
    }

    public UserInfo(String userName, int userID) {
        this.userName = userName;
        this.userID = userID;
    }

    public final String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public final int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }
}
