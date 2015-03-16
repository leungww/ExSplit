package fyp.leungww.exsplit;


import android.text.TextUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Owe {
    private long traveller_id;
    private List<Long> bills;
    private List<Double> amounts;
    private Map<String, Double> totalAmounts;

    public Owe(long traveller_id){
        this.traveller_id = traveller_id;
        this.bills = new ArrayList<>();
        this.amounts = new ArrayList<>();
        this.totalAmounts = new HashMap<>();
    }

    public void addBill(long bill_id, double amount, String currency){
        bills.add(bill_id);
        amounts.add(amount);
        if(totalAmounts.containsKey(currency)){
            BigDecimal amountBD = BigDecimal.valueOf(amount);
            double prevAmount = totalAmounts.get(currency);
            BigDecimal prevAmountBD = BigDecimal.valueOf(prevAmount);
            amountBD = prevAmountBD.add(amountBD);
            totalAmounts.put(currency, amountBD.doubleValue());
        }else{
            totalAmounts.put(currency, amount);
        }
    }

    public String toString_totalAmounts(){
        List<String> strings = new ArrayList<>();
        for(Map.Entry<String, Double> entry:totalAmounts.entrySet()){
            String currencyCode = entry.getKey();
            Currency currency = Currency.getInstance(currencyCode);
            strings.add(currencyCode+" "+currency.getSymbol()+entry.getValue());
        }
        return TextUtils.join(", ", strings);
    }

}
