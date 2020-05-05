package Helper.Requests;

//CreateEditBillboardRequest submits token, billboard name and contents, and creates it if it does not exist, and edits if it does
public class CreateEditBillboardRequest {
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

    //region Billboard Contents
    private String billboardContents;

    public String billboardContents() {
        return billboardContents;
    }

    public void setBillboardContents(String billboardContents) {
        this.billboardContents = billboardContents;
    }
    //endregion

    public CreateEditBillboardRequest(String sessionToken, String billboardName, String billboardContents) {
        this.setSessionToken(sessionToken);
        this.setBillboardName(billboardName);
        this.setBillboardContents(billboardContents);
    }
}
