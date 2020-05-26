package Helper.Responses;

public class ErrorMessage {
    //region Error Message
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    //endregion

    public ErrorMessage(String errorMessage){
        setErrorMessage(errorMessage);
    }
}
