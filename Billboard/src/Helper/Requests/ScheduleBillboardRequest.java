package Helper.Requests;

import jdk.jfr.Timespan;

import java.sql.Date;

public class ScheduleBillboardRequest extends Request {
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

    //region Duration
    private Timespan duration;

    public Timespan getDuration() {
        return duration;
    }

    public void setDuration(Timespan duration) {
        this.duration = duration;
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

    public ScheduleBillboardRequest(String billboardName, Date scheduleTime, Timespan duration, String sessionToken) {
        this.setBillboardName(billboardName);
        this.setScheduleTime(scheduleTime);
        this.setDuration(duration);
        this.setSessionToken(sessionToken);
    }
}