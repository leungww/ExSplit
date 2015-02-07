package fyp.leungww.exsplit;


public class Traveller {

    private long _id;
    private String name;
    private String fbUserId;

    public Traveller(long _id, String name, String fbUserId) {
        this._id = _id;
        this.name = name;
        this.fbUserId = fbUserId;
    }

    public long get_id() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public String getFbUserId() {
        return fbUserId;
    }
}
