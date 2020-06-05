package Helper.Requests;

public class GetBillboardRequest extends Request{
    //region Session Token
    private String sessionToken;

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
    //endregion

    //region Billboard Name
    private String billboardName;

    public String getBillboardName() {
        return billboardName;
    }

    public void setBillboardName(String billboardName) {
        this.billboardName = billboardName;
    }
    //endregion

    public GetBillboardRequest(String sessionToken, String billboardName){
        this.setSessionToken(sessionToken);
        this.setBillboardName(billboardName);
    }
}
