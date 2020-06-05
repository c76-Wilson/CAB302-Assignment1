package Helper.Requests;

public class ViewScheduleRequest extends Request{
    //region Session Token
    private String sessionToken;

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
    //endregion

    public ViewScheduleRequest(String sessionToken) {
        this.setSessionToken(sessionToken);
    }
}
