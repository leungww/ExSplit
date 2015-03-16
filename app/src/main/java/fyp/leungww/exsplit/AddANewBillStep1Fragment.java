package fyp.leungww.exsplit;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;


public class AddANewBillStep1Fragment extends Fragment implements ItemAdapter.ClickListener{
    public static final int ADD_ITEM_REQUEST_CODE=1;
    public static final int EDIT_ITEM_REQUEST_CODE=2;
    public static final int STEP2_REQUEST_CODE=3;
    public static final int SELECT_PHOTO_REQUEST_CODE=4;
    public static final String ITEM_NAME="itemName";
    public static final String ITEM_PRICE="itemPrice";
    public static final String ITEM_AMOUNTS="itemAmounts";
    public static final String ITEM_POSITION="itemPosition";
    public static final String DATE_FRAGMENT_TAG="createDatePicker";
    public static final String BILL_PARCELABLE="billParcelable";
    public static final String BILL_STEP1_COMPLETED_PARCELABLE="billStep1CompletedParcelable";
    public static final String ACTIVITY_CATEGORY="Bill";
    private static final int ACTIVITY_IS_SYSTEM_GENERATED=0;
    private static Button newbill_date;
    private static String createdDate;

    private EditText newbill_description;
    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private Spinner newbill_category, newbill_currency, newbill_trip;
    private Button newbill_add_item, newbill_select_photo, newbill_next;

    private List<Trip> trips;
    private List<List<Traveller>> travellers;
    private List<String> currencyCodes;

    private Traveller user;
    //private List<Traveller> travellersSelected;
    //private String currencyCodeSelected;
    private List<Item> items = new ArrayList<>();

    private TravellerDBAdapter travellerDBAdapter;
    private TripDBAdapter tripDBAdapter;
    private AccountDBAdapter accountDBAdapter;
    private ActivityDBAdapter activityDBAdapter;
    private BillDBAdapter billDBAdapter;
    private OweDBAdapter oweDBAdapter;

    private long startTime;

    public AddANewBillStep1Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*Time time = new Time();
        time.setToNow();
        startTime = time.toMillis(false);
        Log.i("Add a new bill", "start");*/
        startTime = Calendar.getInstance().getTimeInMillis();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_a_new_bill_step1, container, false);
        recyclerView= (RecyclerView) view.findViewById(R.id.item_list);
        adapter=new ItemAdapter(getActivity());
        adapter.setMyClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        newbill_date = (Button) view.findViewById(R.id.newbill_date);
        newbill_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CreateDatePickerFragment().show(getActivity().getSupportFragmentManager(), DATE_FRAGMENT_TAG);
            }
        });

        travellerDBAdapter = new TravellerDBAdapter(getActivity());
        accountDBAdapter = new AccountDBAdapter(getActivity());
        tripDBAdapter = new TripDBAdapter(getActivity());
        activityDBAdapter = new ActivityDBAdapter(getActivity());
        billDBAdapter = new BillDBAdapter(getActivity());
        oweDBAdapter = new OweDBAdapter(getActivity());

        newbill_description = (EditText) view.findViewById(R.id.newbill_description);
        newbill_category = (Spinner) view.findViewById(R.id.newbill_category);
        newbill_currency = (Spinner) view.findViewById(R.id.newbill_currency);

        newbill_trip = (Spinner) view.findViewById(R.id.newbill_trip);
        SharedPreferences sharedPreferences=getActivity().getSharedPreferences(ExSplitApplication.SHARED_PREF_FILE_USER_INFO, Context.MODE_PRIVATE);
        long user_id = sharedPreferences.getLong(ExSplitApplication.SHARED_PREF_KEY_USER_ID,-1);
        if(user_id > 0){
            user = travellerDBAdapter.getTraveller(user_id);
            user.setName(getString(R.string.you));
            trips = tripDBAdapter.getTrips(user_id);
            Collections.reverse(trips);
            travellers = new ArrayList<>();
            List<String> tripDetails = new ArrayList<>();
            for(Trip trip:trips){
                String details = trip.getName()+" - ";
                List<Long> travellers_id = trip.getTravellers();
                travellers_id.remove(user.get_id());
                List<String> travellers_name = new ArrayList<>();
                List<Traveller> travellerList = new ArrayList<>();
                for(Long traveller_id:travellers_id){
                    Traveller traveller = travellerDBAdapter.getTraveller(traveller_id);
                    travellers_name.add(traveller.getName());
                    travellerList.add(traveller);
                }
                travellerList.add(user);
                details += TextUtils.join(", ", travellers_name);
                details += " and You";
                travellers.add(travellerList);
                tripDetails.add(details);
            }
            ArrayAdapter<String> tripAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, tripDetails);
            tripAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            newbill_trip.setAdapter(tripAdapter);
            newbill_trip.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    currencyCodes = trips.get(position).getCountries();
                    List<String> currencyCodesSymbols = new ArrayList<>();
                    for (String currencyCode : currencyCodes) {
                        Currency currency = Currency.getInstance(currencyCode);
                        currencyCodesSymbols.add(currencyCode + " " + currency.getSymbol());
                    }
                    ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, currencyCodesSymbols);
                    currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    newbill_currency.setAdapter(currencyAdapter);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            newbill_add_item = (Button) view.findViewById(R.id.newbill_add_item);
            newbill_add_item.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), AddItemActivity.class);
                    intent.putExtra(BILL_PARCELABLE, getBillParcelable());
                    startActivityForResult(intent,ADD_ITEM_REQUEST_CODE);
                }
            });
            newbill_select_photo = (Button) view.findViewById(R.id.newbill_select_photo);
            newbill_select_photo.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), SelectPhotoActivity.class);
                    intent.putExtra(BILL_PARCELABLE, getBillParcelable());
                    startActivityForResult(intent,SELECT_PHOTO_REQUEST_CODE);
                }
            });
            newbill_next = (Button) view.findViewById(R.id.newbill_next);
            newbill_next.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    nextStep();
                }
            });
        }

        return view;
    }

    private BillParcelable getBillParcelable(){
        String currencyCodeSymbol = (String) newbill_currency.getSelectedItem();
        int position = newbill_trip.getSelectedItemPosition();
        List<Traveller> travellerList = travellers.get(position);
        List<Long> travellers_id = new ArrayList<>();
        List<String> travellers_name = new ArrayList<>();
        for(Traveller traveller:travellerList){
            travellers_id.add(traveller.get_id());
            travellers_name.add(traveller.getName());
        }
        BillParcelable billParcelable = new BillParcelable(currencyCodeSymbol, travellers_id, travellers_name);
        return billParcelable;
    }

    private void nextStep(){
        List<String> errors = new ArrayList<>();
        String description = newbill_description.getText().toString().trim();
        if(description.length() == 0){
            errors.add("Description is empty");
        }
        if(createdDate == null){
            errors.add("Date is not selected");
        }
        if(items.isEmpty()){
            errors.add("No item is added");
        }
        if(errors.isEmpty()){
            try {
                Intent intent = new Intent(getActivity(), AddANewBillStep2Activity.class);
                intent.putExtra(BILL_STEP1_COMPLETED_PARCELABLE, getBillStep1CompletedParcelable());
                startActivityForResult(intent, STEP2_REQUEST_CODE);
            }catch (IllegalArgumentException e){
                Toast toast = Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT);
                toast.show();
            }
        }else{
            Toast toast = Toast.makeText(getActivity(), TextUtils.join("\n", errors), Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private BillStep1CompletedParcelable getBillStep1CompletedParcelable() throws IllegalArgumentException{
        String currencyCodeSymbol = (String) newbill_currency.getSelectedItem();
        int position_trip = newbill_trip.getSelectedItemPosition();
        List<Traveller> travellerList = travellers.get(position_trip);
        List<Long> travellers_id = new ArrayList<>();
        List<String> travellers_name = new ArrayList<>();
        for(Traveller traveller:travellerList){
            travellers_id.add(traveller.get_id());
            travellers_name.add(traveller.getName());
        }

        List<Double> travellers_amount = new ArrayList<>(Collections.nCopies(travellers_id.size(), 0.0));
        BigDecimal totalBD = BigDecimal.ZERO;
        for(Item item:items){
            Map<Long, Double> amounts = item.getAmounts();
            double item_price = item.getPrice();
            BigDecimal item_priceBD = BigDecimal.valueOf(item_price);
            totalBD = totalBD.add(item_priceBD);
            for(int index=0;index<travellers_id.size();index++){
                long traveller_id = travellers_id.get(index);
                if(amounts.containsKey(traveller_id)){
                    Double traveller_amount = travellers_amount.get(index);
                    BigDecimal traveller_amountBD = BigDecimal.valueOf(traveller_amount);
                    Double item_amount = amounts.get(traveller_id);
                    BigDecimal item_amountBD = BigDecimal.valueOf(item_amount);
                    BigDecimal subtotalBD = traveller_amountBD.add(item_amountBD);
                    travellers_amount.set(index,subtotalBD.doubleValue());
                }
            }
        }
        if(totalBD.doubleValue() <= 0){
            throw new IllegalArgumentException("Total price cannot be negative");
        }
        if(Collections.min(travellers_amount) < 0){
            throw new IllegalArgumentException("Amount needed from each traveller cannot be negative");
        }
        BillStep1CompletedParcelable billStep1CompletedParcelable = new BillStep1CompletedParcelable(currencyCodeSymbol, travellers_id, travellers_name, travellers_amount, totalBD.doubleValue());
        return billStep1CompletedParcelable;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (ADD_ITEM_REQUEST_CODE) : {
                if (resultCode == android.app.Activity.RESULT_OK) {
                    //Remove all other trips in the newbill_trip spinner except the selected one
                    removeUnselectedTrips();
                    //Remove all other currencies in the newbill_currency spinner except the selected one
                    removeUnselectedCurrencies();

                    ItemParcelable itemParcelable = data.getParcelableExtra(AddItemActivity.ITEM_PARCELABLE);
                    String item_name = itemParcelable.getName();
                    Double item_price = itemParcelable.getPrice();
                    String item_split_way = itemParcelable.getSplitWay();
                    Map<Long, Double> item_amounts = itemParcelable.getAmounts();
                    String currencyCodeSymbol = (String) newbill_currency.getSelectedItem();
                    String amounts_string = itemParcelable.getAmounts_string();
                    Item item = new Item(item_name, currencyCodeSymbol, item_price, item_split_way, item_amounts, amounts_string);
                    items.add(item);
                    adapter.add(item);
                }
                break;
            }
            case (EDIT_ITEM_REQUEST_CODE) : {
                if (resultCode == android.app.Activity.RESULT_OK) {
                    int item_position = data.getIntExtra(ITEM_POSITION,0);
                    ItemParcelable itemParcelable = (ItemParcelable) data.getParcelableExtra(AddItemActivity.ITEM_PARCELABLE);
                    String item_name = itemParcelable.getName();
                    Double item_price = itemParcelable.getPrice();
                    String item_split_way = itemParcelable.getSplitWay();
                    Map<Long, Double> item_amounts = itemParcelable.getAmounts();
                    String currencyCodeSymbol = (String) newbill_currency.getSelectedItem();
                    String amounts_string = itemParcelable.getAmounts_string();
                    Item item = new Item(item_name, currencyCodeSymbol, item_price, item_split_way, item_amounts, amounts_string);
                    items.set(item_position,item);
                    adapter.set(item_position,item);
                }else if (resultCode == Activity.RESULT_FIRST_USER) {
                    int item_position = data.getIntExtra(ITEM_POSITION,0);
                    items.remove(item_position);
                    adapter.remove(item_position);
                }
                break;
            }
            case (STEP2_REQUEST_CODE) : {
                if (resultCode == android.app.Activity.RESULT_OK) {
                    CompletedBillParcelable completedBillParcelable = data.getParcelableExtra(AddANewBillStep2Activity.COMPLETED_BILL_PARCELABLE);
                    String description = newbill_description.getText().toString().trim();
                    String category = (String) newbill_category.getSelectedItem();
                    long trip_id = trips.get(newbill_trip.getSelectedItemPosition()).getId();
                    String currency = currencyCodes.get(newbill_currency.getSelectedItemPosition());
                    double total = completedBillParcelable.getTotal();

                    List<Long> travellers_id = completedBillParcelable.getTravellers_id();
                    List<Double> amounts_needed = completedBillParcelable.getAmounts_needed();
                    List<Double> amounts_paid = completedBillParcelable.getAmounts_paid();

                    List<Long> debtors_id = completedBillParcelable.getDebtors_id();
                    List<Long> creditors_id = completedBillParcelable.getCreditors_id();
                    List<Double> amounts_owed = completedBillParcelable.getAmounts_owed();

                    try {
                        long bill_id = billDBAdapter.insert(description, createdDate, category,
                                trip_id, currency, total, items, travellers_id, amounts_needed, amounts_paid);
                        activityDBAdapter.insertAll(travellers_id, createdDate, ACTIVITY_CATEGORY,
                                description, ACTIVITY_IS_SYSTEM_GENERATED, bill_id);
                        accountDBAdapter.updateBalances(travellers_id, currency, amounts_paid);
                        if(debtors_id.size() > 0 && creditors_id.size() > 0 && amounts_owed.size() > 0) {
                            ListIterator<Long> debtors_idIterator = debtors_id.listIterator();
                            ListIterator<Long> creditors_idIterator = creditors_id.listIterator();
                            ListIterator<Double> amounts_owedIterator = amounts_owed.listIterator();
                            List<Debt> debtsToDelete = new ArrayList<>();
                            while(debtors_idIterator.hasNext() && creditors_idIterator.hasNext() && amounts_owedIterator.hasNext()){
                                Long debtor = debtors_idIterator.next();
                                Long creditor = creditors_idIterator.next();
                                Double amountOwed = amounts_owedIterator.next();
                                //Try to cancel out creditor's debts to debtor
                                Queue<Debt> queue = oweDBAdapter.getDebts(currency, creditor, debtor);
                                while(!queue.isEmpty()){
                                    Debt debt=queue.peek();
                                    BigDecimal amountDebtBD = BigDecimal.valueOf(debt.getAmountOwed());
                                    BigDecimal amountOwedBD = BigDecimal.valueOf(amountOwed);
                                    if(amountDebtBD.doubleValue() < amountOwedBD.doubleValue()) {
                                        amountOwedBD = amountOwedBD.subtract(amountDebtBD);
                                        amounts_owedIterator.set(amountOwedBD.doubleValue());
                                        debtsToDelete.add(queue.poll());
                                    }else if (amountDebtBD.doubleValue() == amountOwedBD.doubleValue()) {
                                        debtsToDelete.add(queue.poll());
                                        debtors_idIterator.remove();
                                        creditors_idIterator.remove();
                                        amounts_owedIterator.remove();
                                        queue.clear();
                                    }else{
                                        amountDebtBD = amountDebtBD.subtract(amountOwedBD);
                                        debt.setAmountOwed(amountDebtBD.doubleValue());
                                        oweDBAdapter.updateAmountOwed(debt);
                                        debtors_idIterator.remove();
                                        creditors_idIterator.remove();
                                        amounts_owedIterator.remove();
                                        queue.clear();
                                    }
                                }
                            }
                            if(debtsToDelete.size() > 0){
                                oweDBAdapter.deleteOwes(debtsToDelete);
                            }
                            if(debtors_id.size() > 0 && creditors_id.size() > 0 && amounts_owed.size() > 0) {
                                oweDBAdapter.insert(bill_id, currency, debtors_id, creditors_id, amounts_owed);
                            }
                        }
                        //TODO: build traker
                        long endTime = Calendar.getInstance().getTimeInMillis();
                        long timeSpent = endTime-startTime;
                        Tracker tracker = ((ExSplitApplication) getActivity().getApplication()).getTracker(ExSplitApplication.TrackerName.APP_TRACKER);
                        tracker.send(new HitBuilders.TimingBuilder()
                                .setCategory(getString(R.string.add_a_new_bill))
                                .setValue(timeSpent)
                                .setVariable(description)
                                .build());
                        Log.i("Bill "+description, timeSpent+"ms");

                        Toast toast = Toast.makeText(getActivity(), "Bill "+description+" has been added", Toast.LENGTH_LONG);
                        toast.show();
                        android.support.v4.app.FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        Fragment newFragment = new HomeFragment();
                        transaction.replace(R.id.container, newFragment);
                        transaction.commit();
                        ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(getString(R.string.home));
                    } catch (SQLException e) {
                        Toast toast = Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
                break;
            }
            case (SELECT_PHOTO_REQUEST_CODE) : {
                if (resultCode == android.app.Activity.RESULT_OK) {
                    //Remove all other trips in the newbill_trip spinner except the selected one
                    removeUnselectedTrips();
                    //Remove all other currencies in the newbill_currency spinner except the selected one
                    removeUnselectedCurrencies();

                    ItemsParcelable itemsParcelable = data.getParcelableExtra(SelectPhotoActivity.ITEMS_PARCELABLE);
                    List<Long> travellers_id = itemsParcelable.getTravellers_id();
                    List<String> items_name_evenSplit = itemsParcelable.getItems_name_evenSplit();
                    String currencyCodeSymbol = (String) newbill_currency.getSelectedItem();
                    List<Double> items_price_evenSplit = itemsParcelable.getItems_price_evenSplit();
                    List<String> items_amounts_string_evenSplit = itemsParcelable.getItems_amounts_string_evenSplit();
                    List<List<Double>> items_amounts_evenSplit = itemsParcelable.getItems_amounts_evenSplit();
                    for(int index=0;index<items_name_evenSplit.size();index++){
                        String item_name = items_name_evenSplit.get(index);
                        Double item_price = items_price_evenSplit.get(index);
                        String item_split_way = getString(R.string.even_split);
                        String amounts_string = items_amounts_string_evenSplit.get(index);
                        List<Double> item_amountsList = items_amounts_evenSplit.get(index);
                        Map<Long, Double> item_amounts = new HashMap<>();
                        for(int i=0;i<item_amountsList.size();i++){
                            item_amounts.put(travellers_id.get(i),item_amountsList.get(i));
                        }
                        Item item = new Item(item_name, currencyCodeSymbol, item_price, item_split_way, item_amounts, amounts_string);
                        items.add(item);
                        adapter.add(item);
                    }

                    List<String> items_name_byAmount = itemsParcelable.getItems_name_byAmount();
                    List<Double> items_price_byAmount = itemsParcelable.getItems_price_byAmount();
                    for(int index=0;index<items_name_byAmount.size();index++){
                        String item_name = items_name_byAmount.get(index);
                        Double item_price = items_price_byAmount.get(index);
                        String item_split_way = getString(R.string.by_amount);
                        String amounts_string = user.getName()+" ("+item_price+")";
                        Map<Long, Double> item_amounts = new HashMap<>();
                        item_amounts.put(user.get_id(),item_price);
                        Item item = new Item(item_name, currencyCodeSymbol, item_price, item_split_way, item_amounts, amounts_string);
                        items.add(item);
                        adapter.add(item);
                    }

                }
                break;
            }
        }
    }

    private void removeUnselectedTrips(){
        if(trips.size()>1){
            int position = newbill_trip.getSelectedItemPosition();
            Trip trip = trips.get(position);
            trips.clear();
            trips.add(trip);
            List<Traveller> travellerList1 = travellers.get(position);
            travellers.clear();
            travellers.add(travellerList1);
            String details = (String) newbill_trip.getSelectedItem();
            List<String> tripDetails = new ArrayList<>();
            tripDetails.add(details);
            ArrayAdapter<String> tripAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, tripDetails);
            tripAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            newbill_trip.setAdapter(tripAdapter);
        }
    }

    private void removeUnselectedCurrencies(){
        if(currencyCodes.size()>1){
            int position = newbill_currency.getSelectedItemPosition();
            String currencyCode = currencyCodes.get(position);
            currencyCodes.clear();
            currencyCodes.add(currencyCode);
            String currencyCodesSymbol = (String) newbill_currency.getSelectedItem();
            List<String> currencyCodesSymbols = new ArrayList<>();
            currencyCodesSymbols.add(currencyCodesSymbol);
            ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, currencyCodesSymbols);
            currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            newbill_currency.setAdapter(currencyAdapter);
        }
    }

    @Override
    public void itemClicked(View view, int position) {
        String currencyCodeSymbol = (String) newbill_currency.getSelectedItem();
        int position_trip = newbill_trip.getSelectedItemPosition();
        List<Traveller> travellerList = travellers.get(position_trip);
        List<Long> travellers_id = new ArrayList<>();
        List<String> travellers_name = new ArrayList<>();
        for(Traveller traveller:travellerList){
            travellers_id.add(traveller.get_id());
            travellers_name.add(traveller.getName());
        }
        Item item = items.get(position);
        BillParcelable billParcelable = new BillParcelable(currencyCodeSymbol, travellers_id, travellers_name);
        Intent intent = new Intent(getActivity(),EditItemActivity.class);
        intent.putExtra(BILL_PARCELABLE, billParcelable);
        intent.putExtra(ITEM_NAME, item.getName());
        intent.putExtra(ITEM_PRICE, item.getPrice());
        intent.putExtra(ITEM_AMOUNTS, (HashMap) item.getAmounts());
        intent.putExtra(ITEM_POSITION, position);
        startActivityForResult(intent,EDIT_ITEM_REQUEST_CODE);
    }

    public static class CreateDatePickerFragment extends DatePickerFragment{
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            newbill_date.setBackgroundColor(Color.WHITE);
            Calendar create = Calendar.getInstance();
            create.set(year, month, day);
            String date = new SimpleDateFormat("dd-MMM-yyyy").format(create.getTime());
            createdDate = new SimpleDateFormat(Trip.DATE_FORMAT).format(create.getTime());
            newbill_date.setText(date);
        }
    }
}

class BillParcelable implements Parcelable {
    private String currencyCodeSymbol;
    private List<Long> travellers_id;
    private List<String> travellers_name;

    public BillParcelable(String currencyCodeSymbol, List<Long> travellers_id, List<String> travellers_name){
        this.currencyCodeSymbol = currencyCodeSymbol;
        this.travellers_id = travellers_id;
        this.travellers_name = travellers_name;
    }

    public String getCurrencyCodeSymbol(){
        return currencyCodeSymbol;
    }

    public List<Long> getTravellers_id(){
        return travellers_id;
    }

    public List<String> getTravellers_name(){
        return travellers_name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(currencyCodeSymbol);
        out.writeList(travellers_id);
        out.writeList(travellers_name);
    }

    public static final Parcelable.Creator<BillParcelable> CREATOR = new Parcelable.Creator<BillParcelable>() {
        public BillParcelable createFromParcel(Parcel in) {
            return new BillParcelable(in);
        }

        public BillParcelable[] newArray(int size) {
            return new BillParcelable[size];
        }
    };

    private BillParcelable(Parcel in) {
        currencyCodeSymbol = in.readString();
        travellers_id = new ArrayList<>();
        travellers_id = in.readArrayList(Long.class.getClassLoader());
        travellers_name = new ArrayList<>();
        travellers_name = in.readArrayList(String.class.getClassLoader());
    }
}

class BillStep1CompletedParcelable implements Parcelable {
    private String currencyCodeSymbol;
    private List<Long> travellers_id;
    private List<String> travellers_name;
    private List<Double> travellers_amount;
    private Double total;

    public BillStep1CompletedParcelable(String currencyCodeSymbol, List<Long> travellers_id, List<String> travellers_name, List<Double> travellers_amount, Double total){
        this.currencyCodeSymbol = currencyCodeSymbol;
        this.travellers_id = travellers_id;
        this.travellers_name = travellers_name;
        this.travellers_amount = travellers_amount;
        this.total = total;
    }

    public String getCurrencyCodeSymbol(){
        return currencyCodeSymbol;
    }

    public List<Long> getTravellers_id(){
        return travellers_id;
    }

    public List<String> getTravellers_name(){
        return travellers_name;
    }

    public List<Double> getTravellers_amount(){
        return travellers_amount;
    }

    public Double getTotal() { return total;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(currencyCodeSymbol);
        out.writeList(travellers_id);
        out.writeList(travellers_name);
        out.writeList(travellers_amount);
        out.writeDouble(total);
    }

    public static final Parcelable.Creator<BillStep1CompletedParcelable> CREATOR = new Parcelable.Creator<BillStep1CompletedParcelable>() {
        public BillStep1CompletedParcelable createFromParcel(Parcel in) {
            return new BillStep1CompletedParcelable(in);
        }

        public BillStep1CompletedParcelable[] newArray(int size) {
            return new BillStep1CompletedParcelable[size];
        }
    };

    private BillStep1CompletedParcelable(Parcel in) {
        currencyCodeSymbol = in.readString();
        travellers_id = new ArrayList<>();
        travellers_id = in.readArrayList(Long.class.getClassLoader());
        travellers_name = new ArrayList<>();
        travellers_name = in.readArrayList(String.class.getClassLoader());
        travellers_amount = new ArrayList<>();
        travellers_amount = in.readArrayList(Double.class.getClassLoader());
        total = in.readDouble();
    }
}