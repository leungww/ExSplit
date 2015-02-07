package fyp.leungww.exsplit;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Trip {
    private long id;
    private String name;
    private Date fromDate;
    private Date toDate;
    private List<String> countries;
    private List<Long> travellers;

    public Trip(long id, String name, String fromDate, String toDate, String countries) throws ParseException {
        this.id = id;
        this.name = name;
        this.fromDate = new SimpleDateFormat("dd-MM-yyyy").parse(fromDate);
        this.toDate = new SimpleDateFormat("dd-MM-yyyy").parse(toDate);
        this.countries = new ArrayList<>(Arrays.asList(countries.split(",")));
    }

    public void setTravellers(List<Long> travellers) {
        this.travellers = travellers;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public List<String> getCountries() {
        return countries;
    }

    public List<Long> getTravellers() {
        return travellers;
    }

}
