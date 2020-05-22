package Helper.Requests;

import Helper.UserPermissions;

import java.util.EnumSet;

public class SetUserPasswordRequest extends Request{
    //region User Name
    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    //endregion

    //region Hashed Password
    private String hashedPassword;

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }
    //endregion

    //region Session Token
    private String sessionToken;

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
    //endregion

    public SetUserPasswordRequest(String userName, String hashedPassword, String sessionToken) {
        this.setUserName(userName);
        this.setHashedPassword(hashedPassword);
        this.setSessionToken(sessionToken);
    }
}
