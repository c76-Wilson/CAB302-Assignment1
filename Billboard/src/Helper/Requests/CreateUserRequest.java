package Helper.Requests;

import java.util.LinkedList;

public class CreateUserRequest extends Request{
    //region User Name
    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    //endregion

    //region Permissions
    private LinkedList<String> permissions;

    public LinkedList<String> getPermissions() { return permissions; }

    public void setPermissions(LinkedList<String> permissions) {
        this.permissions = permissions;
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

    public CreateUserRequest(String userName, LinkedList<String> permissions, String hashedPassword, String sessionToken) {
        this.setUserName(userName);
        this.setPermissions(permissions);
        this.setHashedPassword(hashedPassword);
        this.setSessionToken(sessionToken);
    }
}
