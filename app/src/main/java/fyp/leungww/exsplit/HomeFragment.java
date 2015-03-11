package fyp.leungww.exsplit;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {
    public static final String BILL_ID="bill_id";
    private ActivityDBAdapter activityDBAdapter;
    private List<Activity> activities;
    private LinearLayout activity_list;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        activityDBAdapter = new ActivityDBAdapter(getActivity());
        SharedPreferences sharedPreferences=getActivity().getSharedPreferences(ExSplitApplication.SHARED_PREF_FILE_USER_INFO, Context.MODE_PRIVATE);
        long user_id = sharedPreferences.getLong(ExSplitApplication.SHARED_PREF_KEY_USER_ID,-1);
        if(user_id > 0) {
            activity_list = (LinearLayout) view.findViewById(R.id.activity_list);
            activities = activityDBAdapter.getActivities(user_id);
            Collections.sort(activities, new Comparator<Activity>(){

                @Override
                public int compare(Activity lhs, Activity rhs) {
                    return rhs.getCreatedDate().compareTo(lhs.getCreatedDate());
                }
            });
            String prevDate = null;
            for(final Activity activity:activities){
                try {
                    String currDate = activity.getCreatedDate();
                    if(prevDate == null || prevDate.compareTo(currDate) != 0){
                        Date createdDate = new SimpleDateFormat(Trip.DATE_FORMAT).parse(currDate);
                        String date = new SimpleDateFormat("dd MMM yyyy").format(createdDate);
                        TextView activity_createdDate = new TextView(getActivity());
                        activity_createdDate.setText(date);
                        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        llp.setMargins(0, 25, 0, 0);
                        activity_createdDate.setLayoutParams(llp);
                        activity_list.addView(activity_createdDate);
                        prevDate = currDate;
                    }
                    TextView activity_description = new TextView(getActivity());
                    activity_description.setText(activity.getDescription());
                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    llp.setMargins(20, 0, 0, 0); // llp.setMargins(left, top, right, bottom);
                    activity_description.setLayoutParams(llp);
                    if(!activity.isSystemGenerated()){
                        llp.setMargins(20, 5, 0, 0); // llp.setMargins(left, top, right, bottom);
                        activity_description.setTextColor(getResources().getColor(R.color.primaryColor));
                        activity_description.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                        activity_description.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getActivity(),TheBillActivity.class);
                                intent.putExtra(BILL_ID,activity.getObjectId());
                                startActivity(intent);
                            }
                        });
                    }
                    activity_list.addView(activity_description);
                } catch (ParseException e) {

                }

            }
        }
        return view;
    }


}
