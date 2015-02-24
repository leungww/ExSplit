package fyp.leungww.exsplit;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class Bill {
    private long _id;
    private long trip;
    private String description;
    private Date createdDate;
    private String category;
    private String currency;
    private double total;

    public Bill(long _id, long trip, String description, String createdDate, String category, String currency, double total) throws ParseException {
        this._id = _id;
        this.trip = trip;
        this.description = description;
        this.createdDate = new SimpleDateFormat(Trip.DATE_FORMAT).parse(createdDate);
        this.category = category;
        this.currency = currency;
        this.total = total;
    }

    public Bill(String description, Date createdDate, String category, String currency, double total) {
        this.description = description;
        this.createdDate = createdDate;

        this.category = category;
        this.currency = currency;
        this.total = total;
    }

    private Map<Long, Double> amountNeeded;
    private Map<Long, Double> amountPaid;

    public long get_id() {
        return _id;
    }

    public long getTrip() {
        return trip;
    }

    public String getDescription() {
        return description;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public String getCategory() {
        return category;
    }

    public String getCurrency() {
        return currency;
    }

    public double getTotal() {
        return total;
    }

    public Map<Long, Double> getAmountNeeded() {
        return amountNeeded;
    }

    public void setAmountNeeded(Map<Long, Double> amountNeeded) {
        this.amountNeeded = amountNeeded;
    }

    public Map<Long, Double> getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(Map<Long, Double> amountPaid) {
        this.amountPaid = amountPaid;
    }

}
