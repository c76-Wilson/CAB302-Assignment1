package Helper.Requests;

// LoginRequest contains a username and password - server should return error or a valid session token
public class LoginRequest extends Request{
    //region Username
    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public LoginRequest(String userName, String hashedPassword){
        this.setUserName(userName);
        this.setHashedPassword(hashedPassword);
    }
}
