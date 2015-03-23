package fyp.leungww.exsplit;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class AddItemActivity extends ActionBarActivity {
    public static final String ITEM_PARCELABLE="item_parcelable";
    public static final int EVEN_SPLIT_ROUNDING_DP=2;

    private RadioGroup newitem_split_way;
    private LinearLayout newitem_split_details;
    private Button newitem_add;
    private EditText newitem_name, newitem_price;
    private View even_split_helper_row;

    private List<CheckBox> checkBoxes;
    private List<EditText> editTexts;
    private List<View> even_split_rows;
    private List<View> by_amount_rows;

    private List<Long> travellers_id;
    private List<String> travellers_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        Toolbar toolbar= (Toolbar)findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        BillParcelable billParcelable = intent.getParcelableExtra(AddANewBillStep1Fragment.BILL_PARCELABLE);
        String currencyCodeSymbol = billParcelable.getCurrencyCodeSymbol();
        travellers_id = billParcelable.getTravellers_id();
        travellers_name = billParcelable.getTravellers_name();

        newitem_name = (EditText) findViewById(R.id.newitem_name);
        TextView newitem_currency = (TextView) findViewById(R.id.newitem_currency);
        newitem_currency.setText(getString(R.string.price)+" ("+currencyCodeSymbol+")");
        newitem_price = (EditText) findViewById(R.id.newitem_price);

        newitem_split_details = (LinearLayout) findViewById(R.id.newitem_split_details);
        newitem_split_way = (RadioGroup) findViewById(R.id.newitem_split_way);
        LayoutInflater inflater = getLayoutInflater();
        checkBoxes = new ArrayList<>();
        editTexts = new ArrayList<>();
        even_split_rows = new ArrayList<>();
        by_amount_rows = new ArrayList<>();
        for(String name:travellers_name){
            View even_split_row = inflater.inflate(R.layout.even_split_row, null);
            CheckBox checkBox = (CheckBox) even_split_row.findViewById(R.id.traveller_name);
            checkBoxes.add(checkBox);
            checkBox.setText(name);
            even_split_rows.add(even_split_row);

            View by_amount_row = inflater.inflate(R.layout.by_amount_row, null);
            TextView textView = (TextView) by_amount_row.findViewById(R.id.traveller_name);
            textView.setText(name);
            EditText traveller_amount = (EditText) by_amount_row.findViewById(R.id.traveller_amount);
            editTexts.add(traveller_amount);
            by_amount_rows.add(by_amount_row);
        }
        even_split_helper_row= inflater.inflate(R.layout.even_split_helper_row, null);
        Button even_split_select_all = (Button) even_split_helper_row.findViewById(R.id.even_split_select_all);
        even_split_select_all.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                for(CheckBox checkBox:checkBoxes){
                    checkBox.setChecked(true);
                }
            }
        });
        Button even_split_deselect_all = (Button) even_split_helper_row.findViewById(R.id.even_split_deselect_all);
        even_split_deselect_all.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                for(CheckBox checkBox:checkBoxes){
                    checkBox.setChecked(false);
                }
            }
        });
        newitem_split_way.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                newitem_split_details.removeAllViews();
                if(checkedId == R.id.newitem_even_split){
                    newitem_split_details.addView(even_split_helper_row);
                    for(View even_split_row:even_split_rows){
                        newitem_split_details.addView(even_split_row);
                    }
                }else if(checkedId == R.id.newitem_by_amount){
                    for(View by_amount_row:by_amount_rows){
                        newitem_split_details.addView(by_amount_row);
                    }
                }
            }
        });
        newitem_add = (Button) findViewById(R.id.newitem_add);
        newitem_add.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                addItem();
            }
        });
    }

    private void addItem(){
        List<String> errors = new ArrayList<>();
        String name = newitem_name.getText().toString().trim();
        Map<Long,Double> amounts = new HashMap<>();
        String amounts_string = null;
        if(name.length() == 0){
            errors.add("Item name is empty");
        }
        String price_string = newitem_price.getText().toString();
        Double price = null;
        if(price_string.length() == 0){
            errors.add("Price is empty");
        }else{
            try {
                price = Double.parseDouble(price_string);
                if(price == 0){
                    errors.add("Price cannot be 0");
                }
            }catch(NumberFormatException e){
                errors.add("Invalid price");
            }
        }
        int checkedId = newitem_split_way.getCheckedRadioButtonId();
        String waySplit = null;
        if(price != null) {
            if (checkedId == R.id.newitem_even_split) {
                List<String> names = new ArrayList<>();
                waySplit = getString(R.string.even_split);
                List<Long> travellers_even_split = new ArrayList<>();
                for (int index = 0; index < checkBoxes.size(); index++) {
                    if (checkBoxes.get(index).isChecked()) {
                        travellers_even_split.add(travellers_id.get(index));
                        names.add(travellers_name.get(index));
                    }
                }
                if (travellers_even_split.size() == 0) {
                    errors.add("No traveller is selected");
                } else if (travellers_even_split.size() == 1){
                    errors.add("Select at least 2 travellers");
                } else{
                    BigDecimal priceBD = BigDecimal.valueOf(price);
                    BigDecimal sizeBD = BigDecimal.valueOf(travellers_even_split.size());
                    BigDecimal evenAmountBD = priceBD.divide(sizeBD, EVEN_SPLIT_ROUNDING_DP, BigDecimal.ROUND_HALF_UP);
                    double evenAmount = evenAmountBD.doubleValue();
                    BigDecimal lastAmountBD = priceBD.subtract(evenAmountBD.multiply(sizeBD.subtract(BigDecimal.ONE)));
                    double lastAmount = lastAmountBD.doubleValue();
                    if((price > 0 && (evenAmount <= 0 || lastAmount <= 0)) || (price < 0 && (evenAmount >= 0 || lastAmount >= 0))){
                        errors.add("Price cannot be split evenly between selected travellers");
                    }else{
                        for (Long traveller_id : travellers_even_split) {
                            amounts.put(traveller_id, evenAmount);
                        }
                        //Last traveller takes the rounding remains
                        Random random = new Random();
                        amounts.put(travellers_even_split.get(random.nextInt(travellers_even_split.size())), lastAmount);
                        List<String> names_amounts = new ArrayList<>();
                        for(int i=0;i<names.size()-1;i++){
                            names_amounts.add(names.get(i)+" ("+evenAmount+")");
                        }
                        names_amounts.add(names.get(names.size()-1)+" ("+lastAmount+")");
                        amounts_string = TextUtils.join(", ", names_amounts);
                    }
                }
            } else if (checkedId == R.id.newitem_by_amount) {
                waySplit = getString(R.string.by_amount);
                BigDecimal sum = BigDecimal.ZERO;
                List<String> names_amounts = new ArrayList<>();
                for (int index = 0; index < editTexts.size(); index++) {
                    String amount_string = editTexts.get(index).getText().toString();
                    if (amount_string.length() > 0) {
                        try {
                            double amount = Double.parseDouble(amount_string);
                            if (amount != 0) {
                                sum = sum.add(BigDecimal.valueOf(amount));
                                amounts.put(travellers_id.get(index), amount);
                                names_amounts.add(travellers_name.get(index)+" ("+amount+")");
                            }
                        }catch(NumberFormatException e){
                            errors.add("Invalid amount");
                        }
                    }
                }
                amounts_string = TextUtils.join(", ", names_amounts);
                if(price != 0) {
                    if (sum.doubleValue() > price) {
                        errors.add("Sum of all travellers' amounts exceeds the item price");
                    } else if (sum.doubleValue() < price) {
                        errors.add("Sum of all travellers' amounts is less than the item price");
                    }
                }
            } else {
                errors.add("Select one way to share the price");
            }
        }
        if(errors.isEmpty() && waySplit != null && price != null){
            ItemParcelable itemParcelable = new ItemParcelable(name,price,waySplit,amounts,amounts_string);
            Intent resultIntent = new Intent();
            resultIntent.putExtra(ITEM_PARCELABLE,itemParcelable);
            setResult(RESULT_OK, resultIntent);
            finish();
        }else{
            Toast toast = Toast.makeText(this, TextUtils.join("\n", errors), Toast.LENGTH_SHORT);
            toast.show();
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

class ItemParcelable implements Parcelable {
    private String name;
    private Double price;
    private String splitWay;
    private Map<Long,Double> amounts;
    private String amounts_string;

    public ItemParcelable(String name, Double price, String splitWay, Map<Long,Double> amounts, String amounts_string){
        this.name = name;
        this.price = price;
        this.splitWay = splitWay;
        this.amounts = amounts;
        this.amounts_string = amounts_string;
    }

    public String getName(){
        return name;
    }

    public Double getPrice(){
        return price;
    }

    public String getSplitWay(){
        return splitWay;
    }

    public Map<Long,Double> getAmounts(){
        return amounts;
    }

    public String getAmounts_string(){
        return amounts_string;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeDouble(price);
        out.writeString(splitWay);
        out.writeInt(amounts.size());
        for(Map.Entry<Long,Double> entry : amounts.entrySet()){
            out.writeLong(entry.getKey());
            out.writeDouble(entry.getValue());
        }
        out.writeString(amounts_string);
    }

    public static final Parcelable.Creator<ItemParcelable> CREATOR = new Parcelable.Creator<ItemParcelable>() {
        public ItemParcelable createFromParcel(Parcel in) {
            return new ItemParcelable(in);
        }

        public ItemParcelable[] newArray(int size) {
            return new ItemParcelable[size];
        }
    };

    private ItemParcelable(Parcel in) {
        name = in.readString();
        price = in.readDouble();
        splitWay = in.readString();
        int size = in.readInt();
        amounts = new HashMap<>(size);
        for(int i = 0; i < size; i++){
            Long traveller_id = in.readLong();
            Double traveller_amount = in.readDouble();
            amounts.put(traveller_id,traveller_amount);
        }
        amounts_string = in.readString();
    }
}
