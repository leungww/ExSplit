package fyp.leungww.exsplit;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bill {
    private long _id;
    private long trip;
    private String description;
    private Date createdDate;
    private String category;
    private String currency;
    private double total;
    private List<Bill.Item> items;

    public Bill(long _id, long trip, String description, String createdDate, String category, String currency, double total) throws ParseException {
        this._id = _id;
        this.trip = trip;
        this.description = description;
        this.createdDate = new SimpleDateFormat(Trip.DATE_FORMAT).parse(createdDate);
        this.category = category;
        this.currency = currency;
        this.total = total;
        this.items = new ArrayList<>();
    }

    public String getDescription() {
        return description;
    }

    public Date getCreatedDate(){
        return createdDate;
    }

    public String getCategory(){
        return category;
    }

    public long getTrip(){
        return trip;
    }

    public String getCurrency(){
        return currency;
    }

    public double getTotal(){
        return total;
    }

    public List<Bill.Item> getItems(){
        return items;
    }

    public void addItems(List<String> names, List<Double> prices, List<Map<Long, Double>> amountsList){
        for(int index=0;index<names.size();index++){
            Bill.Item item = new Bill.Item(names.get(index), prices.get(index), amountsList.get(index));
            items.add(item);
        }

    }

    class Item {
        private String name;
        private double price;
        private Map<Long, Double> amounts;

        public Item(String name, double price, Map<Long, Double> amounts){
            this.name = name;
            this.price = price;
            this.amounts = amounts;
        }

        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }

        public Map<Long, Double> getAmounts() {
            return amounts;
        }
    }

}