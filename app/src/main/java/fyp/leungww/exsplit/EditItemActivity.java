package fyp.leungww.exsplit;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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


public class EditItemActivity extends ActionBarActivity {

    public static final String ITEM_PARCELABLE="item_parcelable";

    private RadioGroup edititem_split_way;
    private LinearLayout edititem_split_details;
    private Button edititem_update, edititem_delete;
    private EditText edititem_name, edititem_price;

    private List<CheckBox> checkBoxes;
    private List<EditText> editTexts;
    private List<View> even_split_rows;
    private List<View> by_amount_rows;

    private List<Long> travellers_id;
    private List<String> travellers_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);
        Toolbar toolbar= (Toolbar)findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();
        BillParcelable billParcelable = (BillParcelable) i.getParcelableExtra(AddANewBillStep1Fragment.BILL_PARCELABLE);
        String currencyCodeSymbol = billParcelable.getCurrencyCodeSymbol();
        travellers_id = billParcelable.getTravellers_id();
        travellers_name = billParcelable.getTravellers_name();
        Map<Long, Double> item_amounts = (HashMap<Long, Double>)i.getSerializableExtra(AddANewBillStep1Fragment.ITEM_AMOUNTS);
        String item_name = i.getStringExtra(AddANewBillStep1Fragment.ITEM_NAME);
        double item_price = i.getDoubleExtra(AddANewBillStep1Fragment.ITEM_PRICE,0);
        final int item_position = i.getIntExtra(AddANewBillStep1Fragment.ITEM_POSITION,0);

        edititem_name = (EditText) findViewById(R.id.edititem_name);
        edititem_name.setText(item_name);
        TextView edititem_currency = (TextView) findViewById(R.id.edititem_currency);
        edititem_currency.setText(getString(R.string.price)+" ("+currencyCodeSymbol+")");
        edititem_price = (EditText) findViewById(R.id.edititem_price);
        edititem_price.setText(item_price+"");
        edititem_split_details = (LinearLayout) findViewById(R.id.edititem_split_details);
        edititem_split_way = (RadioGroup) findViewById(R.id.edititem_split_way);

        editTexts = new ArrayList<>();
        checkBoxes = new ArrayList<>();
        even_split_rows = new ArrayList<>();
        by_amount_rows = new ArrayList<>();
        LayoutInflater inflater = getLayoutInflater();
        for(String name:travellers_name){
            View even_split_row = inflater.inflate(R.layout.even_split_row, null);
            CheckBox checkBox = (CheckBox) even_split_row.findViewById(R.id.traveller_name);
            checkBoxes.add(checkBox);
            checkBox.setText(name);
            even_split_rows.add(even_split_row);
        }
        for(int index=0;index<travellers_id.size();index++){
            View by_amount_row = inflater.inflate(R.layout.by_amount_row, null);
            TextView textView = (TextView) by_amount_row.findViewById(R.id.traveller_name);
            textView.setText(travellers_name.get(index));
            EditText editText = (EditText) by_amount_row.findViewById(R.id.traveller_amount);
            if(item_amounts.containsKey(travellers_id.get(index))){
                editText.setText(item_amounts.get(travellers_id.get(index))+"");
            }
            editTexts.add(editText);
            by_amount_rows.add(by_amount_row);
            edititem_split_details.addView(by_amount_row);
        }

        edititem_split_way.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                edititem_split_details.removeAllViews();
                if(checkedId == R.id.edititem_even_split){
                    for(View even_split_row:even_split_rows){
                        edititem_split_details.addView(even_split_row);
                    }
                }else if(checkedId == R.id.edititem_by_amount){
                    for(View by_amount_row:by_amount_rows){
                        edititem_split_details.addView(by_amount_row);
                    }
                }
            }
        });
        edititem_update = (Button) findViewById(R.id.edititem_update);
        edititem_update.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                updateItem(item_position);
            }
        });

        edititem_delete = (Button) findViewById(R.id.edititem_delete);
        edititem_delete.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(AddANewBillStep1Fragment.ITEM_POSITION, item_position);
                setResult(RESULT_FIRST_USER, intent);
                finish();
            }
        });
    }

    private void updateItem(int item_position){
        List<String> errors = new ArrayList<>();
        String name = edititem_name.getText().toString().trim();
        Map<Long,Double> amounts = new HashMap<>();
        String amounts_string = null;
        if(name.length() == 0){
            errors.add("Item name is empty");
        }
        String price_string = edititem_price.getText().toString();
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
        int checkedId = edititem_split_way.getCheckedRadioButtonId();
        String waySplit = null;
        if(price != null) {
            if (checkedId == R.id.edititem_even_split) {
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
                    BigDecimal evenAmountBD = priceBD.divide(sizeBD, AddItemActivity.EVEN_SPLIT_ROUNDING_DP, BigDecimal.ROUND_HALF_UP);
                    double evenAmount = evenAmountBD.doubleValue();
                    BigDecimal lastAmountBD = priceBD.subtract(evenAmountBD.multiply(sizeBD.subtract(BigDecimal.ONE)));
                    double lastAmount = lastAmountBD.doubleValue();
                    if(evenAmount == 0 || lastAmount == 0){
                        errors.add("Price cannot be split evenly between selected travellers");
                    }else{
                        for (Long traveller_id : travellers_even_split) {
                            amounts.put(traveller_id, evenAmount);
                        }
                        //Last traveller takes the rounding remains
                        amounts.put(travellers_even_split.get(travellers_even_split.size()-1), lastAmount);
                        List<String> names_amounts = new ArrayList<>();
                        for(int i=0;i<names.size()-1;i++){
                            names_amounts.add(names.get(i)+" ("+evenAmount+")");
                        }
                        names_amounts.add(names.get(names.size()-1)+" ("+lastAmount+")");
                        amounts_string = TextUtils.join(", ", names_amounts);
                    }
                }
            } else if (checkedId == R.id.edititem_by_amount) {
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
            resultIntent.putExtra(AddANewBillStep1Fragment.ITEM_POSITION, item_position);
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