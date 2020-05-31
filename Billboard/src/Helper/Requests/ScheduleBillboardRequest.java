package Helper.Requests;

import jdk.jfr.Timespan;

import java.sql.Date;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;

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
    private LocalDateTime scheduleTime;

    public LocalDateTime getScheduleTime() { return scheduleTime; }

    public void setScheduleTime(LocalDateTime scheduleTime) {
        this.scheduleTime = scheduleTime;
    }
    //endregion

    //region Duration
    private Duration duration;

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }
    //endregion

    //region Recurring
    private Duration recurring;

    public Duration getRecurring() {
        return recurring;
    }

    public void setRecurring(Duration recurring) {
        this.recurring = recurring;
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

    public ScheduleBillboardRequest(String billboardName, LocalDateTime scheduleTime, Duration duration, String sessionToken) {
        this.setBillboardName(billboardName);
        this.setScheduleTime(scheduleTime);
        this.setDuration(duration);
        this.setSessionToken(sessionToken);
    }

    public ScheduleBillboardRequest(String billboardName, LocalDateTime scheduleTime, Duration duration, String sessionToken, Duration recurring) {
        this.setBillboardName(billboardName);
        this.setScheduleTime(scheduleTime);
        this.setDuration(duration);
        this.setSessionToken(sessionToken);
        this.setRecurring(recurring);
    }
}
