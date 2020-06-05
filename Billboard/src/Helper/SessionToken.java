package Helper;

import java.io.Serializable;
import java.time.LocalDateTime;

public class SessionToken implements Serializable {
    //region User
    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) { this.userName = userName; }
    //endregion

    //region Error Message
    private String sessionToken;

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
    //endregion

    //region Error Message
    private LocalDateTime lastUsed;

    public LocalDateTime getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(LocalDateTime lastUsed) {
        this.lastUsed = lastUsed;
    }
    //endregion

    public SessionToken(String userName, String sessionToken){
        setUserName(userName);
        setSessionToken(sessionToken);
        setLastUsed(LocalDateTime.now());
    }
}
