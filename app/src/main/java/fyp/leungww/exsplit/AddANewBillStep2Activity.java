package fyp.leungww.exsplit;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;


public class AddANewBillStep2Activity extends ActionBarActivity {
    public static final String COMPLETED_BILL_PARCELABLE="completedBillParcelable";

    private LinearLayout newbill_split_details;
    private Button newbill_save;
    private TextView newbill_total;
    private List<EditText> editTexts;

    private List<Long> travellers_id;
    private List<String> travellers_name;
    private List<Double> travellers_amount;
    private double total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_a_new_bill_step2);
        Toolbar toolbar= (Toolbar)findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        BillStep1CompletedParcelable billStep1CompletedParcelable = (BillStep1CompletedParcelable) intent.getParcelableExtra(AddANewBillStep1Fragment.BILL_STEP1_COMPLETED_PARCELABLE);
        String currencyCodeSymbol = billStep1CompletedParcelable.getCurrencyCodeSymbol();
        total = billStep1CompletedParcelable.getTotal();
        travellers_id = billStep1CompletedParcelable.getTravellers_id();
        travellers_name = billStep1CompletedParcelable.getTravellers_name();
        travellers_amount = billStep1CompletedParcelable.getTravellers_amount();

        TextView newbill_total_string = (TextView) findViewById(R.id.newbill_total_string);
        newbill_total_string.setText(getString(R.string.total)+" ("+currencyCodeSymbol+")");
        newbill_total = (TextView) findViewById(R.id.newbill_total);
        newbill_total.setText(total+"");
        newbill_split_details = (LinearLayout) findViewById(R.id.newbill_split_details);
        editTexts = new ArrayList<>();
        LayoutInflater inflater = getLayoutInflater();
        for(int index=0;index<travellers_id.size();index++){
            View traveller_amounts_row = inflater.inflate(R.layout.traveller_amounts_row, null);
            TextView textView_name = (TextView) traveller_amounts_row.findViewById(R.id.traveller_name);
            textView_name.setText(travellers_name.get(index));
            EditText editText = (EditText) traveller_amounts_row.findViewById(R.id.traveller_paid);
            editText.setText(travellers_amount.get(index)+"");
            TextView textView_needed = (TextView) traveller_amounts_row.findViewById(R.id.traveller_needed);
            textView_needed.setText(getString(R.string.needs)+" "+travellers_amount.get(index));
            editTexts.add(editText);
            newbill_split_details.addView(traveller_amounts_row);
        }

        newbill_save = (Button) findViewById(R.id.newbill_save);
        newbill_save.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                saveBill();
            }
        });
    }

    private void saveBill(){
        List<String> errors = new ArrayList<>();
        List<Double> amounts = new ArrayList<>();
        List<Long> debtors_id = new ArrayList<>();
        List<Long> creditors_id = new ArrayList<>();
        List<Double> amounts_owed = new ArrayList<>();
        PriorityQueue<AmountsDifference> debt = new PriorityQueue<AmountsDifference>(travellers_id.size(),new Comparator<AmountsDifference>(){

            @Override
            public int compare(AmountsDifference lhs, AmountsDifference rhs) {
                return rhs.difference.compareTo(lhs.difference);
            }
        });
        PriorityQueue<AmountsDifference> credit = new PriorityQueue<AmountsDifference>(travellers_id.size(),new Comparator<AmountsDifference>(){

            @Override
            public int compare(AmountsDifference lhs, AmountsDifference rhs) {
                return lhs.difference.compareTo(rhs.difference);
            }
        });
        BigDecimal sum = BigDecimal.ZERO;
        for (int index = 0; index < editTexts.size(); index++) {
            String amount_string = editTexts.get(index).getText().toString();
            double amount;
            if (amount_string.length() > 0) {
                try {
                    amount = Double.parseDouble(amount_string);
                    sum = sum.add(BigDecimal.valueOf(amount));
                }catch(NumberFormatException e){
                    errors.add("Invalid amount");
                    amount = 0.0;
                }
            }else{
                amount = 0.0;
            }
            amounts.add(amount);
            BigDecimal amountBD = BigDecimal.valueOf(amount);
            double needed = travellers_amount.get(index);
            BigDecimal neededBD = BigDecimal.valueOf(needed);
            BigDecimal difference = amountBD.subtract(neededBD);
            if(difference.doubleValue()>0){
                AmountsDifference amountsDifference = new AmountsDifference(travellers_id.get(index), difference);
                credit.add(amountsDifference);
            }else if(difference.doubleValue()<0){
                AmountsDifference amountsDifference = new AmountsDifference(travellers_id.get(index), difference);
                debt.add(amountsDifference);
            }
        }
        if (sum.doubleValue() > total) {
            errors.add("Sum of all travellers' amounts exceeds the total");
        } else if (sum.doubleValue() < total) {
            errors.add("Sum of all travellers' amounts less than the total");
        } else {
            //work out who owes others money and by how much
            while(credit.size() !=0 && debt.size() !=0){
                AmountsDifference debtL = debt.peek();
                AmountsDifference creditL = credit.peek();
                BigDecimal remainDebt = creditL.difference.add(debtL.difference);
                if(remainDebt.doubleValue() == 0){
                    debtors_id.add(debtL.traveller_id);
                    creditors_id.add(creditL.traveller_id);
                    amounts_owed.add(creditL.difference.doubleValue());

                    debt.poll();
                    credit.poll();
                }else if(remainDebt.doubleValue() < 0){
                    debtors_id.add(debtL.traveller_id);
                    creditors_id.add(creditL.traveller_id);
                    amounts_owed.add(creditL.difference.doubleValue());

                    debtL.setDifference(remainDebt);
                    credit.poll();
                }else{
                    debtors_id.add(debtL.traveller_id);
                    creditors_id.add(creditL.traveller_id);
                    amounts_owed.add(debtL.difference.abs().doubleValue());

                    debt.poll();
                    creditL.setDifference(remainDebt);
                }
            }
        }
        if(errors.isEmpty()){
            Intent resultIntent = new Intent();
            CompletedBillParcelable completedBillParcelable = new CompletedBillParcelable(travellers_id, travellers_amount, amounts, total, debtors_id, creditors_id, amounts_owed);
            resultIntent.putExtra(COMPLETED_BILL_PARCELABLE, completedBillParcelable);
            setResult(RESULT_OK, resultIntent);
            finish();
        }else{
            Toast toast = Toast.makeText(this, TextUtils.join("\n", errors), Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private class AmountsDifference {
        long traveller_id;
        BigDecimal difference;

        public AmountsDifference(long traveller_id, BigDecimal difference){
            this.traveller_id = traveller_id;
            this.difference = difference;
        }

        public void setDifference(BigDecimal difference){
            this.difference = difference;
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

class CompletedBillParcelable implements Parcelable {
    private List<Long> travellers_id;
    private List<Double> amounts_needed;
    private List<Double> amounts_paid;
    private Double total;
    private List<Long> debtors_id;
    private List<Long> creditors_id;
    private List<Double> amounts_owed;

    public CompletedBillParcelable(List<Long> travellers_id, List<Double> amounts_needed, List<Double> amounts_paid, Double total,
                                   List<Long> debtors_id, List<Long> creditors_id, List<Double> amounts_owed){
        this.travellers_id = travellers_id;
        this.amounts_needed = amounts_needed;
        this.amounts_paid = amounts_paid;
        this.total = total;
        this.debtors_id = debtors_id;
        this.creditors_id = creditors_id;
        this.amounts_owed = amounts_owed;
    }

    public List<Long> getTravellers_id(){
        return travellers_id;
    }

    public List<Double> getAmounts_needed(){
        return amounts_needed;
    }

    public List<Double> getAmounts_paid(){
        return amounts_paid;
    }

    public Double getTotal() { return total;}

    public List<Long> getDebtors_id(){
        return debtors_id;
    }

    public List<Long> getCreditors_id(){
        return creditors_id;
    }

    public List<Double> getAmounts_owed(){
        return amounts_owed;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeList(travellers_id);
        out.writeList(amounts_needed);
        out.writeList(amounts_paid);
        out.writeDouble(total);
        out.writeList(debtors_id);
        out.writeList(creditors_id);
        out.writeList(amounts_owed);
    }

    public static final Parcelable.Creator<CompletedBillParcelable> CREATOR = new Parcelable.Creator<CompletedBillParcelable>() {
        public CompletedBillParcelable createFromParcel(Parcel in) {
            return new CompletedBillParcelable(in);
        }

        public CompletedBillParcelable[] newArray(int size) {
            return new CompletedBillParcelable[size];
        }
    };

    private CompletedBillParcelable(Parcel in) {
        travellers_id = new ArrayList<>();
        travellers_id = in.readArrayList(Long.class.getClassLoader());
        amounts_needed = new ArrayList<>();
        amounts_needed = in.readArrayList(Double.class.getClassLoader());
        amounts_paid = new ArrayList<>();
        amounts_paid = in.readArrayList(Double.class.getClassLoader());
        total = in.readDouble();
        debtors_id = new ArrayList<>();
        debtors_id = in.readArrayList(Long.class.getClassLoader());
        creditors_id = new ArrayList<>();
        creditors_id = in.readArrayList(Long.class.getClassLoader());
        amounts_owed = new ArrayList<>();
        amounts_owed = in.readArrayList(Double.class.getClassLoader());
    }
}
