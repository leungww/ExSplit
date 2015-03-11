package fyp.leungww.exsplit;



public class Activity {
    private long _id;
    private long traveller;
    private String createdDate;
    private String category;
    private String description;
    private boolean isSystemGenerated;
    private long objectId;

    public Activity(long _id, long traveller, String createdDate, String category, String description, int isSystemGenerated, long objectId) {
        this._id = _id;
        this.traveller = traveller;
        this.createdDate = createdDate;
        this.category = category;
        this.description = description;
        if(isSystemGenerated == 0){
            this.isSystemGenerated = false;
        }else{
            this.isSystemGenerated = true;
        }
        this.objectId = objectId;
    }

    public long get_id() {
        return _id;
    }

    public long getTraveller() {
        return traveller;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSystemGenerated() {
        return isSystemGenerated;
    }

    public long getObjectId() {
        return objectId;
    }

}
