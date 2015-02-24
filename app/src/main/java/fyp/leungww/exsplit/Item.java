package fyp.leungww.exsplit;


import java.util.HashMap;
import java.util.Map;

public class Item {
    private String name;
    private String currencyCodeSymbol;
    private double price;
    private String waySplit;
    private Map<Long, Double> amounts;
    private String amounts_string;

    public Item(String name, String currencyCodeSymbol, double price, String waySplit, Map<Long, Double> amounts, String amounts_string) {
        this.name = name;
        this.currencyCodeSymbol = currencyCodeSymbol;
        this.waySplit = waySplit;
        this.price = price;
        this.amounts = amounts;
        this.amounts_string = amounts_string;
    }

    public String getName() {
        return name;
    }

    public String getWaySplit() {
        return waySplit;
    }

    public double getPrice() {
        return price;
    }

    public Map<Long, Double> getAmounts(){
        return amounts;
    }

    public String getAmounts_string(){
        return amounts_string;
    }

    public String getCurrencyCodeSymbol() {
        return currencyCodeSymbol;
    }

}
