package Helper.Requests;

import java.util.LinkedList;

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
    private LinkedList<String> permissions;

    public LinkedList<String> getPermissions() { return permissions; }

    public void setPermissions(LinkedList<String> permissions) {
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

    public SetUserPermissionsRequest(String userName, LinkedList<String> permissions, String sessionToken) {
        this.setUserName(userName);
        this.setPermissions(permissions);
        this.setSessionToken(sessionToken);
    }
}
