package Helper.Requests;

import Helper.UserPermissions;

import java.util.EnumSet;

public class GetUserPermissionsRequest extends Request{
    //region User Name
    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public GetUserPermissionsRequest(String userName, String sessionToken) {
        this.setUserName(userName);
        this.setSessionToken(sessionToken);
    }
}
