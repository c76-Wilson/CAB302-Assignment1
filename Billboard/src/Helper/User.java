package Helper;

public class User {
    //region Name
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }
    //endregion

    //region Billboards Created
    private Integer billboardsCreated;

    public Integer getBillboardsCreated() {
        return billboardsCreated;
    }

    public void setBillboardsCreated(Integer billboardsCreated) { this.billboardsCreated = billboardsCreated; }
    //endregion

    public User(String name, Integer billboardsCreated){
        setName(name);
        setBillboardsCreated(billboardsCreated);
    }
}
