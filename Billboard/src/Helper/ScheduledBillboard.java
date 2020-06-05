package Helper;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

public class ScheduledBillboard implements Serializable {
    //region Name
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }
    //endregion

    //region Creator Name
    private String creatorName;

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
    //endregion

    //region Schedule Time
    private LocalDateTime scheduleTime;

    public LocalDateTime getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(LocalDateTime scheduleTime) { this.scheduleTime = scheduleTime; }
    //endregion

    //region Schedule Duration
    private Duration scheduleDuration;

    public Duration getScheduleDuration() {
        return scheduleDuration;
    }

    public void setScheduleDuration(Duration scheduleDuration) { this.scheduleDuration = scheduleDuration; }
    //endregion

    public ScheduledBillboard(String name, String creatorName, LocalDateTime scheduleTime, Duration duration){
        setName(name);
        setCreatorName(creatorName);
        setScheduleTime(scheduleTime);
        setScheduleDuration(duration);
    }
}
