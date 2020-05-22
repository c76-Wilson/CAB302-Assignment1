package Helper.Requests;

import Helper.UserPermissions;

import java.util.EnumSet;

public class SetUserPermissionsRequest extends Request{
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

    //region Session Token
    private String sessionToken;

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
    //endregion

    public SetUserPermissionsRequest(String userName, EnumSet<UserPermissions> permissions, String sessionToken) {
        this.setUserName(userName);
        this.setPermissions(permissions);
        this.setSessionToken(sessionToken);
    }
}
