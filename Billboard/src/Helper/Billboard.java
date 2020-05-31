package Helper;

import java.io.Serializable;

public class Billboard implements Serializable {
    //region Name
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }
    //endregion

    //region XML
    private String xml;

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) { this.xml = xml; }
    //endregion

    //region Creator Name
    private String creatorName;

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
    //endregion

    //region ScheduleID
    private Integer scheduleID;

    public Integer getScheduleID() {
        return scheduleID;
    }

    public void setScheduleID(Integer scheduleID) { this.scheduleID = scheduleID; }
    //endregion

    public Billboard(String name, String xml, String creatorName){
        setName(name);
        setXml(xml);
        setCreatorName(creatorName);
    }

    public Billboard(String name, String xml, String creatorName, Integer scheduleID){
        setName(name);
        setXml(xml);
        setCreatorName(creatorName);
        setScheduleID(scheduleID);
    }
}
