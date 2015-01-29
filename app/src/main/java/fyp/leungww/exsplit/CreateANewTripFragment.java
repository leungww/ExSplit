package fyp.leungww.exsplit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class CreateANewTripFragment extends Fragment {
    public static final String FROM_DATE_FRAGMENT_TAG="fromDatePicker";
    public static final String TO_DATE_FRAGMENT_TAG="toDatePicker";
    public static final int ADD_TRAVELLER_REQUEST_CODE = 0;
    private static final String TAG ="CreateANewTripFragment";

    private EditText newtrip_name;
    private static Button newtrip_from, newtrip_to;
    private static Calendar fromDate, toDate;

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
        onSessionStateChange(session, state, exception);
        }
    };
    private UiLifecycleHelper uiHelper;
    private Traveller traveller;

    public CreateANewTripFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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
        Button newtrip_add_travellers = (Button) view.findViewById(R.id.newtrip_add_travellers);
        newtrip_add_travellers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Session session = Session.getActiveSession();
                if (session != null && session.isOpened()) {
                    Intent intent = new Intent();
                    intent.setData(AddTravellerActivity.FRIEND_PICKER);
                    intent.setClass(getActivity(), AddTravellerActivity.class);
                    startActivityForResult(intent, ADD_TRAVELLER_REQUEST_CODE);
                }else{
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                }
            }
        });

        ListView listView = (ListView) view.findViewById(R.id.travellers_list);
        traveller = new Traveller();

        listView.setAdapter(new TravellerAdapter(getActivity(), R.id.travellers_list, Arrays.asList(traveller)));

        if (savedInstanceState != null) {
            // Restore the state for each list element
            traveller.restoreState(savedInstanceState);
        }
        return view;
    }

    private class TravellerAdapter extends ArrayAdapter<Traveller> {
        private List<Traveller> travellers;

        public TravellerAdapter(Context context, int resourceId, List<Traveller> travellers) {
            super(context, resourceId, travellers);
            this.travellers = travellers;
            // Set up as an observer for list item changes to
            // refresh the view.
            for (int i = 0; i < travellers.size(); i++) {
                travellers.get(i).setAdapter(this);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.traveller_row, parent, false);
            }

            Traveller traveller = travellers.get(position);
            if (traveller != null) {
                //view.setOnClickListener(traveller.getOnClickListener());
                //traveller.setButtonListener();
                TextView travellers = (TextView) view.findViewById(R.id.travellers);
                if (travellers != null) {
                    travellers.setText(traveller.getTravellers());
                }
            }
            return view;
        }
    }

    private class Traveller {
        private static final String FRIENDS_KEY = "friends";
        private String travellers;
        private BaseAdapter adapter;
        private List<GraphUser> selectedTravellers;

        public void setAdapter(BaseAdapter adapter) {
            this.adapter = adapter;
        }

        public String getTravellers() {
            return travellers;
        }

        public void setTravellers(String travellers) {
            this.travellers = travellers;
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }

        private void setTravellers() {
            String text = null;
            if (selectedTravellers != null) {
                if (selectedTravellers.size() == 1) {
                    text = String.format(getResources().getString(R.string.single_user_selected),
                            selectedTravellers.get(0).getName());
                } else if (selectedTravellers.size() == 2) {
                    text = String.format(getResources().getString(R.string.two_users_selected),
                            selectedTravellers.get(0).getName(), selectedTravellers.get(1).getName());
                } else if (selectedTravellers.size() > 2) {
                    text = String.format(getResources().getString(R.string.multiple_users_selected),
                            selectedTravellers.get(0).getName(), (selectedTravellers.size() - 1));
                }
            }
            if (text == null) {
                text = getResources().getString(R.string.none);
            }
            setTravellers(text);
        }

        protected void onActivityResult(Intent data) {
            selectedTravellers = ((ExSplitApplication) getActivity().getApplication()).getSelectedTravellers();
            setTravellers();
            adapter.notifyDataSetChanged();
        }

        private byte[] getByteArray(List<GraphUser> users) {
            // convert the list of GraphUsers to a list of String where each element is
            // the JSON representation of the GraphUser so it can be stored in a Bundle
            List<String> usersAsString = new ArrayList<>(users.size());

            for (GraphUser user : users) {
                usersAsString.add(user.getInnerJSONObject().toString());
            }
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                new ObjectOutputStream(outputStream).writeObject(usersAsString);
                return outputStream.toByteArray();
            } catch (IOException e) {
                Log.e(TAG, "Unable to serialize users.", e);
            }
            return null;
        }

        protected void onSaveInstanceState(Bundle bundle) {
            if (selectedTravellers != null) {
                bundle.putByteArray(FRIENDS_KEY, getByteArray(selectedTravellers));
            }
        }

        protected boolean restoreState(Bundle savedState) {
            byte[] bytes = savedState.getByteArray(FRIENDS_KEY);
            if (bytes != null) {
                selectedTravellers = restoreByteArray(bytes);
                setTravellers();
                return true;
            }
            return false;
        }

        private List<GraphUser> restoreByteArray(byte[] bytes) {
            try {
                List<String> usersAsString = (List<String>) (new ObjectInputStream(new ByteArrayInputStream(bytes))).readObject();
                if (usersAsString != null) {
                    List<GraphUser> users = new ArrayList<GraphUser>(usersAsString.size());
                    for (String user : usersAsString) {
                        GraphUser graphUser = GraphObject.Factory.create(new JSONObject(user), GraphUser.class);
                        users.add(graphUser);
                    }
                    return users;
                }
            } catch (ClassNotFoundException | IOException | JSONException e) {
                Log.e(TAG, "Unable to deserialize users.", e);
            }
            return null;
        }
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (session != null && session.isOpened()) {
            //Log.i(TAG, "Logged in to Facebook");
        } else if (state.isClosed()) {
            //Log.i(TAG, "Logged out from Facebook");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // For scenarios where the main activity is launched and user session is not null, the session state change notification may not be triggered. Trigger it if it's open/closed.
        Session session = Session.getActiveSession();
        if (session != null && (session.isOpened() || session.isClosed()) ) {
            onSessionStateChange(session, session.getState(), null);
        }
        uiHelper.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_TRAVELLER_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                traveller.onActivityResult(data);
            }
        }
//        else {
//            uiHelper.onActivityResult(travellerBases, resultCode, data, nativeDialogCallback);
//        }
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        traveller.onSaveInstanceState(bundle);
        uiHelper.onSaveInstanceState(bundle);
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
            newtrip_from.setTextColor(getResources().getColor(R.color.primaryColor));
            fromDate = Calendar.getInstance();
            fromDate.set(year, month, day);
            String date = new SimpleDateFormat("dd-MMM-yyyy").format(fromDate.getTime());
            newtrip_from.setText(date);
        }
    }

    public static class ToDatePickerFragment extends DatePickerFragment{
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            newtrip_to.setTextColor(getResources().getColor(R.color.primaryColor));
            toDate = Calendar.getInstance();
            toDate.set(year, month, day);
            String date = new SimpleDateFormat("dd-MMM-yyyy").format(toDate.getTime());
            newtrip_to.setText(date);
        }
    }
}

