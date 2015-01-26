package fyp.leungww.exsplit;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CreateANewTripFragment extends Fragment {
    public static final String FROM_DATE_FRAGMENT_TAG="fromDatePicker";
    public static final String TO_DATE_FRAGMENT_TAG="toDatePicker";
    private EditText newtrip_name;
    private static Button newtrip_from, newtrip_to;
    private Button newtrip_add_travellers;
    private static Calendar fromDate, toDate;

    public CreateANewTripFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        newtrip_add_travellers = (Button) view.findViewById(R.id.newtrip_add_travellers);
        newtrip_add_travellers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Session session = Session.getActiveSession();
                if (session != null && session.isOpened()) {
                    Log.i("friend picker","Ready to attach");
                }else{
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                }
            }
        });
        return view;
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

