package Helper.Requests;

public class ListUsersRequest extends Request{
    //region Session Token
    private String sessionToken;

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
    //endregion

    public ListUsersRequest(String sessionToken) {
        this.setSessionToken(sessionToken);
    }
}
