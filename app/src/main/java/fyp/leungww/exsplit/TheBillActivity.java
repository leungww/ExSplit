package fyp.leungww.exsplit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TheBillActivity extends ActionBarActivity {
    private TripDBAdapter tripDBAdapter;
    private BillDBAdapter billDBAdapter;
    private TravellerDBAdapter travellerDBAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_the_bill);
        Toolbar toolbar= (Toolbar)findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();
        long bill_id = i.getLongExtra(HomeFragment.BILL_ID,-1);

        billDBAdapter = new BillDBAdapter(this);
        tripDBAdapter = new TripDBAdapter(this);
        travellerDBAdapter = new TravellerDBAdapter(this);

        SharedPreferences sharedPreferences = getSharedPreferences(ExSplitApplication.SHARED_PREF_FILE_USER_INFO, Context.MODE_PRIVATE);
        long user_id = sharedPreferences.getLong(ExSplitApplication.SHARED_PREF_KEY_USER_ID,-1);

        Bill bill = billDBAdapter.getBill(bill_id);
        if(bill != null && user_id > 0) {
            String currencyCode = bill.getCurrency();
            Currency currency = Currency.getInstance(currencyCode);
            String currencyCodeSymbol = currencyCode + " " +currency.getSymbol();
            Trip trip = tripDBAdapter.getTrip(bill.getTrip());
            if(trip != null){
                Traveller user = travellerDBAdapter.getTraveller(user_id);
                user.setName(getString(R.string.you));
                List<Long> travellers_id = trip.getTravellers();
                travellers_id.remove(user.get_id());
                Map<Long, Traveller> travellers = new HashMap<>();
                for(Long traveller_id:travellers_id){
                    Traveller traveller = travellerDBAdapter.getTraveller(traveller_id);
                    travellers.put(traveller_id, traveller);
                }
                travellers.put(user.get_id(),user);
                TextView thebill_description = (TextView) findViewById(R.id.thebill_description);
                thebill_description.setText(bill.getDescription());
                TextView thebill_date = (TextView) findViewById(R.id.thebill_date);
                String date = new SimpleDateFormat("dd MMM yyyy").format(bill.getCreatedDate().getTime());
                thebill_date.setText(date);
                TextView thebill_category = (TextView) findViewById(R.id.thebill_category);
                thebill_category.setText(bill.getCategory());
                TextView thebill_trip = (TextView) findViewById(R.id.thebill_trip);
                thebill_trip.setText(getString(R.string.shared_between_travellers_in)+" "+trip.getName());

                LinearLayout thebill_item_list = (LinearLayout) findViewById(R.id.thebill_item_list);
                LayoutInflater inflater = getLayoutInflater();
                for(Bill.Item item:bill.getItems()){
                    View item_row_the_bill = inflater.inflate(R.layout.item_row_the_bill, null);
                    TextView item_name = (TextView) item_row_the_bill.findViewById(R.id.item_name);
                    item_name.setText(item.getName());
                    TextView item_currency_price = (TextView) item_row_the_bill.findViewById(R.id.item_currency_price);
                    item_currency_price.setText(currencyCodeSymbol+item.getPrice());
                    TextView item_amounts = (TextView) item_row_the_bill.findViewById(R.id.item_amounts);
                    List<String> amounts_string = new ArrayList<>();
                    for(Map.Entry<Long, Double> entry:item.getAmounts().entrySet()){
                        String traveller_name = travellers.get(entry.getKey()).getName();
                        double amount = entry.getValue();
                        amounts_string.add(traveller_name+" ("+amount+")");
                    }
                    item_amounts.setText(TextUtils.join(", ",amounts_string));
                    thebill_item_list.addView(item_row_the_bill);
                }

                TextView thebill_total_string = (TextView) findViewById(R.id.thebill_total_string);
                thebill_total_string.setText(getString(R.string.total)+" ("+currencyCodeSymbol+")");
                TextView thebill_total = (TextView) findViewById(R.id.thebill_total);
                thebill_total.setText(bill.getTotal()+"");
                TextView thebill_subtotal_string = (TextView) findViewById(R.id.thebill_subtotal_string);
                thebill_subtotal_string.setText(getString(R.string.your_subtotal)+" ("+currencyCodeSymbol+")");
                TextView thebill_subtotal = (TextView) findViewById(R.id.thebill_subtotal);
                double subtotal = billDBAdapter.getAmountNeeded(bill_id, user_id);
                thebill_subtotal.setText(subtotal+"");
                TextView thebill_paid_out_string = (TextView) findViewById(R.id.thebill_paid_out_string);
                thebill_paid_out_string.setText(getString(R.string.your_paid_out)+" ("+currencyCodeSymbol+")");
                TextView thebill_paid_out = (TextView) findViewById(R.id.thebill_paid_out);
                double paid_out = billDBAdapter.getAmountPaid(bill_id, user_id);
                thebill_paid_out.setText(paid_out+"");
            }
        }else{
            //TODO: error toast, close activity
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
