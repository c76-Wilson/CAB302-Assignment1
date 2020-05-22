package Helper.Requests;

import jdk.jfr.Timespan;

import java.sql.Date;

public class RemoveFromScheduleRequest extends Request{
    //region Billboard Name
    private String billboardName;

    public String getBillboardName() {
        return billboardName;
    }

    public void setBillboardName(String billboardName) {
        this.billboardName = billboardName;
    }
    //endregion

    //region Time
    private Date scheduleTime;

    public Date getScheduleTime() { return scheduleTime; }

    public void setScheduleTime(Date scheduleTime) {
        this.scheduleTime = scheduleTime;
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

    public RemoveFromScheduleRequest(String billboardName, Date scheduleTime, String sessionToken) {
        this.setBillboardName(billboardName);
        this.setScheduleTime(scheduleTime);
        this.setSessionToken(sessionToken);
    }
}
