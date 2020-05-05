package Helper.Requests;

//ListBillboardsRequest submits a session token and the server responds with a list of billboards, including name and creator of each
public class ListBillboardsRequest extends Request{
    //region Session Token
    private String sessionToken;

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
    //endregion

    public ListBillboardsRequest(String sessionToken){
        this.setSessionToken(sessionToken);
    }
}
