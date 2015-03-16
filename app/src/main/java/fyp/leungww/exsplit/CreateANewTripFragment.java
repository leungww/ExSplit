package fyp.leungww.exsplit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AppEventsLogger;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class CreateANewTripFragment extends Fragment {
    public static final String FROM_DATE_FRAGMENT_TAG="fromDatePicker";
    public static final String TO_DATE_FRAGMENT_TAG="toDatePicker";
    public static final List<String> FB_READ_PERMISSIONS = new ArrayList<String>() {
        {
            add("user_friends");
            add("public_profile");
        }
    };
    public static final int ADD_TRAVELLER_ACTIVITY = 1;
    public static final String CANADA_CURRENCY_CODE="CAD";
    public static final String EUROPE_CURRENCY_CODE="EUR";
    public static final String UK_CURRENCY_CODE="GBP";
    public static final String US_CURRENCY_CODE="USD";
    public static final String ACTIVITY_CATEGORY="Trip";
    private static final String ACTIVITY_DESCRIPTION="Created new trip: ";
    private static final int ACTIVITY_IS_SYSTEM_GENERATED=1;

    private EditText newtrip_name;
    private boolean isSessionOpenedForAddTraveller;
    private LinearLayout newtrip_traveller_list;
    private LayoutInflater inflater;
    private static Button newtrip_from, newtrip_to;
    private static String fromDate, toDate;

    private UiLifecycleHelper uiHelper;
    private GraphUser theUser;
    private List<GraphUser> selection;
    private CheckBox newtrip_canada, newtrip_europe, newtrip_uk, newtrip_us;

    private TravellerDBAdapter travellerDBAdapter;
    private AccountDBAdapter accountDBAdapter;
    private TripDBAdapter tripDBAdapter;
    private ActivityDBAdapter activityDBAdapter;

    public CreateANewTripFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(getActivity(), new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                onSessionStateChange(session, state, exception);
            }
        });
        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.inflater = inflater;
        View view = inflater.inflate(R.layout.fragment_create_a_new_trip, container, false);
        newtrip_name = (EditText) view.findViewById(R.id.newtrip_name);
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(newtrip_name.getWindowToken(), 0);
        newtrip_from = (Button) view.findViewById(R.id.newtrip_from);
        newtrip_from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);
            }
        });
        newtrip_to = (Button) view.findViewById(R.id.newtrip_to);
        newtrip_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);
            }
        });
        newtrip_canada = (CheckBox) view.findViewById(R.id.newtrip_canada);
        newtrip_europe = (CheckBox) view.findViewById(R.id.newtrip_europe);
        newtrip_uk = (CheckBox) view.findViewById(R.id.newtrip_uk);
        newtrip_us = (CheckBox) view.findViewById(R.id.newtrip_us);
        Button newtrip_add_travellers = (Button) view.findViewById(R.id.newtrip_add_travellers);
        newtrip_add_travellers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAddTraveller();
            }
        });
        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            // Get the user's data
            makeMeRequest(session);
        }
        newtrip_traveller_list = (LinearLayout) view.findViewById(R.id.newtrip_traveller_list);
        Button newtrip_save = (Button) view.findViewById(R.id.newtrip_save);
        newtrip_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTrip();
            }
        });
        travellerDBAdapter = new TravellerDBAdapter(getActivity());
        accountDBAdapter = new AccountDBAdapter(getActivity());
        tripDBAdapter = new TripDBAdapter(getActivity());
        activityDBAdapter = new ActivityDBAdapter(getActivity());
        return view;
    }

    private void saveTrip(){
        List<String> errors = new ArrayList<>();
        String name = newtrip_name.getText().toString().trim();
        if(name.length() == 0){
            errors.add("Trip name is empty");
        }
        if(fromDate == null){
            errors.add("Start date is not selected");
        }
        if (toDate == null){
            errors.add("End date is not selected");
        }
        if(fromDate != null && toDate != null && fromDate.compareTo(toDate) >=0){
            errors.add("Start date is after or same as end date");
        }
        if (!newtrip_canada.isChecked() && !newtrip_europe.isChecked() && !newtrip_uk.isChecked() && !newtrip_us.isChecked()){
            errors.add("No country is checked");
        }
        if(theUser == null || selection == null || selection.size() == 0){
            errors.add("No friend is selected");
        }

        if(errors.isEmpty()){
            List<GraphUser> allTravellers = new ArrayList<>(selection);
            allTravellers.add(theUser);
            List<Long> travellers_id = new ArrayList<>();
            try{
                for(GraphUser traveller:allTravellers) {
                    //Save traveller information in database
                    long traveller_id = travellerDBAdapter.insert(traveller.getName(), traveller.getId());
                    travellers_id.add(traveller_id);
                    if (traveller_id < 0) {
                        throw new SQLException("Errors occurred in saving travellers' information to database. Please try again.");
                    }
                    //Create different currency accounts for the traveller
                    if (newtrip_canada.isChecked()) {
                        long account_id = accountDBAdapter.insert(traveller_id, CANADA_CURRENCY_CODE);
                        if (account_id < 0) {
                            throw new SQLException("Errors occurred in creating travellers' accounts in database. Please try again.");
                        }
                    }
                    if (newtrip_europe.isChecked()) {
                        long account_id = accountDBAdapter.insert(traveller_id, EUROPE_CURRENCY_CODE);
                        if (account_id < 0) {
                            throw new SQLException("Errors occurred in creating travellers' accounts in database. Please try again.");
                        }
                    }
                    if (newtrip_uk.isChecked()) {
                        long account_id = accountDBAdapter.insert(traveller_id, UK_CURRENCY_CODE);
                        if (account_id < 0) {
                            throw new SQLException("Errors occurred in creating travellers' accounts in database. Please try again.");
                        }
                    }
                    if (newtrip_us.isChecked()) {
                        long account_id = accountDBAdapter.insert(traveller_id, US_CURRENCY_CODE);
                        if (account_id < 0) {
                            throw new SQLException("Errors occurred in creating travellers' accounts in database. Please try again.");
                        }
                    }
                }
                List<String> countries = new ArrayList<>();
                if(newtrip_canada.isChecked()){
                    countries.add(CANADA_CURRENCY_CODE);
                }
                if(newtrip_europe.isChecked()){
                    countries.add(EUROPE_CURRENCY_CODE);
                }
                if(newtrip_uk.isChecked()){
                    countries.add(UK_CURRENCY_CODE);
                }
                if(newtrip_us.isChecked()){
                    countries.add(US_CURRENCY_CODE);
                }
                //Create trip
                long trip_id = tripDBAdapter.insertAll(name, fromDate, toDate, TextUtils.join(",", countries),travellers_id);

                //Create activity
                String createdDate = new SimpleDateFormat(Trip.DATE_FORMAT).format(new Date());
                activityDBAdapter.insertAll(travellers_id, createdDate, ACTIVITY_CATEGORY,
                        ACTIVITY_DESCRIPTION+name, ACTIVITY_IS_SYSTEM_GENERATED, trip_id);
                Toast toast = Toast.makeText(getActivity(), "Trip "+name+" has been created", Toast.LENGTH_LONG);
                toast.show();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                Fragment newFragment = new AddANewBillStep1Fragment();
                transaction.replace(R.id.container, newFragment);
                transaction.commit();
                ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(getString(R.string.add_a_new_bill));
            }catch(SQLException e){
                Toast toast = Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT);
                toast.show();
            }

        }else{
            Toast toast = Toast.makeText(getActivity(), TextUtils.join("\n", errors), Toast.LENGTH_SHORT);
            toast.show();
        }

    }

    private void onClickAddTraveller(){
        startAddTravellerActivity();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Update the display every time we are started.
        displaySelectedFriends(Activity.RESULT_OK);
    }

    @Override
    public void onResume() {
        super.onResume();
        Session session = Session.getActiveSession();
        if (session != null && (session.isOpened() || session.isClosed()) ) {
            onSessionStateChange(session, session.getState(), null);
        }

        // Call the 'activateApp' method to log an app event for use in analytics and advertising reporting.  Do so in the onResume methods of the primary Activities that an app may be launched into.
        AppEventsLogger.activateApp(getActivity());
        uiHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Call the 'deactivateApp' method to log an app event for use in analytics and advertising reporting.  Do so in the onPause methods of the primary Activities that an app may be launched into.
        AppEventsLogger.deactivateApp(getActivity());
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ADD_TRAVELLER_ACTIVITY:
                displaySelectedFriends(resultCode);
                break;
            default:
                Session.getActiveSession().onActivityResult(getActivity(), requestCode, resultCode, data);
                break;
        }
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    private boolean ensureOpenSession() {
        if (Session.getActiveSession() == null || !Session.getActiveSession().isOpened()) {
            Session.openActiveSession(getActivity(), true, FB_READ_PERMISSIONS, new Session.StatusCallback() {
                @Override
                public void call(Session session, SessionState state, Exception exception) {
                    onSessionStateChange(session, state, exception);
                }
            });
            return false;
        }
        return true;
    }

    private boolean hasNecessaryPermissions(Session session) {
        if (session != null && session.getPermissions() != null) {
            for (String requestedPerm : FB_READ_PERMISSIONS) {
                if (!session.getPermissions().contains(requestedPerm)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private List<String> getMissingPermissions(Session session) {
        List<String> missingPerms = new ArrayList<String>(FB_READ_PERMISSIONS);
        if (session != null && session.getPermissions() != null) {
            for (String requestedPerm : FB_READ_PERMISSIONS) {
                if (session.getPermissions().contains(requestedPerm)) {
                    missingPerms.remove(requestedPerm);
                }
            }
        }
        return missingPerms;
    }

    private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
        if (session != null && session.isOpened()) {
            makeMeRequest(session);
        }
        if (state.isOpened() && !hasNecessaryPermissions(session)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.friend_picker_permission_alert);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    session.requestNewReadPermissions(new Session.NewPermissionsRequest(CreateANewTripFragment.this, getMissingPermissions(session)));
                }
            });
            builder.setNegativeButton(R.string.cancel,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getActivity().finish();
                }
            });
            builder.show();
        } else if (isSessionOpenedForAddTraveller && state.isOpened()) {
            isSessionOpenedForAddTraveller = false;
            startAddTravellerActivity();
        }
    }

    private void displaySelectedFriends(int resultCode) {
        newtrip_traveller_list.removeAllViews();
        if(resultCode == Activity.RESULT_OK){
            ExSplitApplication application = (ExSplitApplication) getActivity().getApplication();
            selection = application.getSelectedTravellers();
            if (selection != null && theUser != null) {
                for (GraphUser traveller : selection) {
                    View traveller_row = inflater.inflate(R.layout.traveller_row, null);
                    ProfilePictureView fb_profile_picture = (ProfilePictureView) traveller_row.findViewById(R.id.fb_profile_picture);
                    TextView fb_profile_username = (TextView) traveller_row.findViewById(R.id.fb_profile_username);
                    fb_profile_picture.setProfileId(traveller.getId());
                    fb_profile_username.setText(traveller.getName());
                    newtrip_traveller_list.addView(traveller_row);
                }
                View traveller_row = inflater.inflate(R.layout.traveller_row, null);
                ProfilePictureView fb_profile_picture = (ProfilePictureView) traveller_row.findViewById(R.id.fb_profile_picture);
                TextView fb_profile_username = (TextView) traveller_row.findViewById(R.id.fb_profile_username);
                fb_profile_picture.setProfileId(theUser.getId());
                fb_profile_username.setText(theUser.getName() + " (" + getString(R.string.you) + ")");
                newtrip_traveller_list.addView(traveller_row);
            }
        }
    }

    private void makeMeRequest(final Session session) {
        // Make an API call to get user data and define a new callback to handle the response.
        Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                // If the response is successful
                if (session == Session.getActiveSession()) {
                    if (user != null) {
                        theUser = user;
                        long _id = travellerDBAdapter.insert(theUser.getName(), theUser.getId());
                        SharedPreferences sharedPreferences=getActivity().getSharedPreferences(ExSplitApplication.SHARED_PREF_FILE_USER_INFO, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor=sharedPreferences.edit();
                        editor.putLong(ExSplitApplication.SHARED_PREF_KEY_USER_ID, _id);
                        editor.commit();
                    }
                }
                if (response.getError() != null) {
                    // Handle errors, will do so later.
                }
            }
        });
        request.executeAsync();
    }

    private void startAddTravellerActivity() {
        if (ensureOpenSession()) {
            Intent intent = new Intent(getActivity(), AddTravellerActivity.class);
            //TODO: find userId
            AddTravellerActivity.populateParameters(intent, null, true, true);
            startActivityForResult(intent, ADD_TRAVELLER_ACTIVITY);
        } else {
            isSessionOpenedForAddTraveller = true;
        }
    }

    public void showDatePickerDialog(View v) {
        switch(v.getId()){
            case R.id.newtrip_from:
                new FromDatePickerFragment().show(getActivity().getSupportFragmentManager(), FROM_DATE_FRAGMENT_TAG);
                break;
            case R.id.newtrip_to:
                new ToDatePickerFragment().show(getActivity().getSupportFragmentManager(), TO_DATE_FRAGMENT_TAG);
                break;
        }
    }

    public static class FromDatePickerFragment extends DatePickerFragment{
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            newtrip_from.setBackgroundColor(Color.WHITE);
            Calendar from = Calendar.getInstance();
            from.set(year, month, day);
            String date = new SimpleDateFormat("dd-MMM-yyyy").format(from.getTime());
            fromDate = new SimpleDateFormat(Trip.DATE_FORMAT).format(from.getTime());
            newtrip_from.setText(date);
        }
    }

    public static class ToDatePickerFragment extends DatePickerFragment{
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            newtrip_to.setBackgroundColor(Color.WHITE);
            Calendar to = Calendar.getInstance();
            to.set(year, month, day);
            String date = new SimpleDateFormat("dd-MMM-yyyy").format(to.getTime());
            toDate = new SimpleDateFormat(Trip.DATE_FORMAT).format(to.getTime());
            newtrip_to.setText(date);
        }
    }


}

