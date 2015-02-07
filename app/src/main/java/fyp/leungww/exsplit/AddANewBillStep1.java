package fyp.leungww.exsplit;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Currency;
import java.util.List;


public class AddANewBillStep1 extends Fragment {
    public static final String DATE_FRAGMENT_TAG="createDatePicker";
    public static final List<Currency> CURRENCIES = Arrays.asList(Currency.getInstance("CAD"), Currency.getInstance("EUR"), Currency.getInstance("GBP"), Currency.getInstance("USD"));
    private static Button newbill_date;
    private static String createDate;

    private Spinner newbill_currency;
    private Spinner newbill_trip;

    private List<Trip> trips;
    private Traveller user;

    private TravellerDBAdapter travellerDBAdapter;
    private AccountDBAdapter accountDBAdapter;
    private TripDBAdapter tripDBAdapter;
    private ActivityDBAdapter activityDBAdapter;


    public AddANewBillStep1() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_a_new_bill_step1, container, false);
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

        newbill_currency = (Spinner) view.findViewById(R.id.newbill_currency);
        ArrayAdapter<Currency> currencyAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, CURRENCIES);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        newbill_currency.setAdapter(currencyAdapter);

        newbill_trip = (Spinner) view.findViewById(R.id.newbill_trip);
        ExSplitApplication application = (ExSplitApplication) getActivity().getApplication();
        long user_id = application.getUser_id();
        if(user_id > 0){
            user = travellerDBAdapter.getTraveller(user_id);
            trips = tripDBAdapter.getTrips(user_id);
            List<String> tripDetails = new ArrayList<>();
            for(Trip trip:trips){
                String details = trip.getName()+" - ";
                details += TextUtils.join(", ", trip.getTravellers());
                tripDetails.add(details);
            }
            ArrayAdapter<String> tripAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, tripDetails);
            tripAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            newbill_trip.setAdapter(tripAdapter);
        }

        return view;
    }

    public static class CreateDatePickerFragment extends DatePickerFragment{
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            newbill_date.setBackgroundColor(Color.WHITE);
            Calendar create = Calendar.getInstance();
            create.set(year, month, day);
            String date = new SimpleDateFormat("dd-MMM-yyyy").format(create.getTime());
            createDate = new SimpleDateFormat("dd-MM-yyyy").format(create.getTime());
            newbill_date.setText(date);
        }
    }


}
