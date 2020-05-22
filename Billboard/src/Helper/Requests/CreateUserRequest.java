package Helper.Requests;

import Helper.UserPermissions;

import java.sql.Date;
import java.util.EnumSet;

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
    private EnumSet<UserPermissions> permissions;

    public EnumSet<UserPermissions> getPermissions() { return permissions; }

    public void setPermissions(EnumSet<UserPermissions> permissions) {
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

    public CreateUserRequest(String userName, EnumSet<UserPermissions> permissions, String hashedPassword, String sessionToken) {
        this.setUserName(userName);
        this.setPermissions(permissions);
        this.setHashedPassword(hashedPassword);
        this.setSessionToken(sessionToken);
    }
}
