package fyp.leungww.exsplit;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class NavigationDrawerFragment extends Fragment implements DrawerAdapter.ClickListener{
    public static final String PREF_FILENAME="testpref";
    public static final String KEY_USER_LEARNED_DRAWER="user_learned_drawer";
    public static final int[] ICONS={R.drawable.ic_home_white_48dp, R.drawable.ic_note_add_white_48dp, R.drawable.ic_event_white_48dp};

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;
    private View containerView;
    private RecyclerView recyclerView;
    private DrawerAdapter adapter;
    private LinearLayout drawer_user;
    private ProfilePictureView profilePictureView;
    private TextView userNameView;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
        onSessionStateChange(session, state, exception);
        }
    };
    private UiLifecycleHelper uiHelper;

    private boolean mUserLearnedDrawer;
    private boolean mFromSavedInstanceState;

    public NavigationDrawerFragment() {
        // Required empty public constructor
    }

    public List<DrawerInfo> createData() {
        List<DrawerInfo> data=new ArrayList<>();
        String[] app_bar_titles = getResources().getStringArray(R.array.app_bar_titles);
        for(int i=0;i<app_bar_titles.length && i<ICONS.length;i++){
            DrawerInfo myDrawerInfo=new DrawerInfo();
            myDrawerInfo.setIconId(ICONS[i]);
            myDrawerInfo.setTitle(app_bar_titles[i]);
            data.add(myDrawerInfo);
        }
        return data;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserLearnedDrawer=Boolean.valueOf(readFromPreferences(getActivity(),KEY_USER_LEARNED_DRAWER,"false"));
        if(savedInstanceState==null){
            mFromSavedInstanceState=true;
        }
        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        recyclerView= (RecyclerView) view.findViewById(R.id.drawer_list);
        adapter=new DrawerAdapter(getActivity(),createData());
        adapter.setMyClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        // Find the user's profile picture custom view
        profilePictureView = (ProfilePictureView) view.findViewById(R.id.fb_profile_picture);
        profilePictureView.setCropped(true);
        // Find the user's name view
        userNameView = (TextView) view.findViewById(R.id.fb_username);
        drawer_user = (LinearLayout) view.findViewById(R.id.drawer_user);
        drawer_user.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Fragment newFragment = new UserProfileFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container, newFragment);
                transaction.commit();
                mToolbar.setTitle(getString(R.string.user_profile));
                mDrawerLayout.closeDrawer(containerView);
            }
        });
        setUpUserProfile();
        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            // Get the user's data
            makeMeRequest(session);
        }
        return view;
    }

    public void setUp(int fragmentId, DrawerLayout drawerLayout, final Toolbar toolbar) {
        containerView=getActivity().findViewById(fragmentId);
        mDrawerLayout=drawerLayout;
        mToolbar=toolbar;
        mDrawerToggle=new ActionBarDrawerToggle(getActivity(),drawerLayout,toolbar,R.string.drawer_open,R.string.drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if(!mUserLearnedDrawer){
                    mUserLearnedDrawer=true;
                    saveToPreferences(getActivity(),KEY_USER_LEARNED_DRAWER, mUserLearnedDrawer+"");
                }
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                // Darken the toolbar a bit when navigation drawer is sliding
                if(slideOffset < 0.6) {
                    toolbar.setAlpha(1 - slideOffset);
                }

            }
        };
        if(!mUserLearnedDrawer && !mFromSavedInstanceState){
            mDrawerLayout.openDrawer(containerView);
        }
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
    }

    private void setUpUserProfile(){
        SharedPreferences sharedPreferences=getActivity().getSharedPreferences(ExSplitApplication.SHARED_PREF_FILE_USER_INFO, Context.MODE_PRIVATE);
        long user_id = sharedPreferences.getLong(ExSplitApplication.SHARED_PREF_KEY_USER_ID,-1);
        if(user_id > 0){
            TravellerDBAdapter travellerDBAdapter = new TravellerDBAdapter(getActivity());
            Traveller user = travellerDBAdapter.getTraveller(user_id);
            profilePictureView.setProfileId(user.getFbUserId());
            userNameView.setText(user.getName());
        }
    }

    public static void saveToPreferences(Context context, String preferenceName, String preferenceValue){
        SharedPreferences sharedPreferences=context.getSharedPreferences(PREF_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString(preferenceName, preferenceValue);
        editor.apply();
    }

    public static String readFromPreferences(Context context, String preferenceName, String defaultValue){
        SharedPreferences sharedPreferences=context.getSharedPreferences(PREF_FILENAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(preferenceName,defaultValue);
    }

    @Override
    public void itemClicked(View view, int position) {
        Fragment newFragment;
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        switch (position) {
            case 0:
                newFragment = new HomeFragment();
                transaction.replace(R.id.container, newFragment);
                transaction.commit();
                break;
            case 1:
                newFragment = new AddANewBillStep1Fragment();
                transaction.replace(R.id.container, newFragment);
                transaction.commit();
                break;
            case 2:
                newFragment = new CreateANewTripFragment();
                transaction.replace(R.id.container, newFragment);
                transaction.commit();
                break;
        }
        String[] app_bar_titles = getResources().getStringArray(R.array.app_bar_titles);
        mToolbar.setTitle(app_bar_titles[position]);
        mDrawerLayout.closeDrawer(containerView);
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (session != null && session.isOpened()) {
            //Log.i(TAG, "Logged in to Facebook");
            makeMeRequest(session);
        } else if (state.isClosed()) {
            if (session == null || !session.isOpened()){
                profilePictureView.setProfileId(null);
                userNameView.setText(getString(R.string.username));
            }
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
        uiHelper.onActivityResult(requestCode, resultCode, data);
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    private void makeMeRequest(final Session session) {
        // Make an API call to get user data and define a new callback to handle the response.
        Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                // If the response is successful
                if (session == Session.getActiveSession()) {
                    if (user != null) {
                        profilePictureView.setProfileId(user.getId());
                        userNameView.setText(user.getName());
                        setUpUserProfile();
                    }
                }
                if (response.getError() != null) {
                    // Handle errors, will do so later.
                }
            }
        });
        request.executeAsync();
    }

}

class DrawerInfo {
    private int iconId;
    private String title;

    public void setIconId(int iconId){
        this.iconId = iconId;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public int getIconId(){
        return iconId;
    }

    public String getTitle(){
        return title;
    }
}

class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.MyViewHolder> {
    private LayoutInflater inflater;
    private List<DrawerInfo> data= Collections.emptyList();
    private Context context;
    private ClickListener myClickListener;

    public DrawerAdapter(Context context, List<DrawerInfo> data) {
        this.inflater=LayoutInflater.from(context);
        this.data=data;
        this.context=context;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.drawer_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        DrawerInfo myDrawerInfo=data.get(position);
        holder.title.setText(myDrawerInfo.getTitle());
        holder.icon.setImageResource(myDrawerInfo.getIconId());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setMyClickListener(ClickListener myClickListener){
        this.myClickListener=myClickListener;
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView title;
        ImageView icon;

        public MyViewHolder(View itemView) {
            super(itemView);
            title= (TextView) itemView.findViewById(R.id.drawer_title);
            icon= (ImageView) itemView.findViewById(R.id.drawer_icon);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(myClickListener!=null){
                myClickListener.itemClicked(v,getPosition());
            }
        }
    }

    public interface ClickListener{
        public void itemClicked(View view, int position);
    }
}

