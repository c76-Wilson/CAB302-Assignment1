package Helper.Requests;

//DeleteBillboardRequest submits a token and billboard name, and the server deletes that billboard.
public class DeleteBillboardRequest {
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

    public DeleteBillboardRequest(String sessionToken, String billboardName){
        this.setSessionToken(sessionToken);
        this.setBillboardName(billboardName);
    }
}
