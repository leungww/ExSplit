package fyp.leungww.exsplit;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Activity {
    private long _id;
    private long traveller;
    private Date createdDate;
    private String category;
    private String description;
    private boolean isSystemGenerated;
    private long objectId;

    public Activity(long _id, long traveller, String createdDate, String category, String description, int isSystemGenerated, long objectId) throws ParseException {
        this._id = _id;
        this.traveller = traveller;
        this.createdDate = new SimpleDateFormat("dd-MM-yyyy").parse(createdDate);
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

    public Date getCreatedDate() {
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
