package fyp.leungww.exsplit;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.ProfilePictureView;

import java.util.Currency;
import java.util.List;
import java.util.Map;


public class UserProfileFragment extends Fragment {
    private LayoutInflater inflater;
    private ProfilePictureView userprofile_user_picture;
    private TextView userprofile_username;
    private LoginButton authButton;
    private LinearLayout userprofile_account_balances, userprofile_friends_owe, userprofile_owe_friends;

    private TravellerDBAdapter travellerDBAdapter;
    private AccountDBAdapter accountDBAdapter;
    private OweDBAdapter oweDBAdapter;
    private Traveller user;

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
    private UiLifecycleHelper uiHelper;

    public UserProfileFragment() {
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
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        this.inflater = inflater;
        userprofile_user_picture = (ProfilePictureView) view.findViewById(R.id.userprofile_user_picture);
        userprofile_username = (TextView) view.findViewById(R.id.userprofile_username);
        authButton = (LoginButton) view.findViewById(R.id.authButton);
        authButton.setFragment(this);
        authButton.setReadPermissions(CreateANewTripFragment.FB_READ_PERMISSIONS);
        userprofile_account_balances = (LinearLayout) view.findViewById(R.id.userprofile_account_balances);
        userprofile_friends_owe = (LinearLayout) view.findViewById(R.id.userprofile_friends_owe);
        userprofile_owe_friends = (LinearLayout) view.findViewById(R.id.userprofile_owe_friends);

        travellerDBAdapter = new TravellerDBAdapter(getActivity());
        accountDBAdapter = new AccountDBAdapter(getActivity());
        oweDBAdapter = new OweDBAdapter(getActivity());
        SharedPreferences sharedPreferences=getActivity().getSharedPreferences(ExSplitApplication.SHARED_PREF_FILE_USER_INFO, Context.MODE_PRIVATE);
        long user_id = sharedPreferences.getLong(ExSplitApplication.SHARED_PREF_KEY_USER_ID,-1);
        if(user_id>0){
            user = travellerDBAdapter.getTraveller(user_id);
            userprofile_user_picture.setProfileId(user.getFbUserId());
            userprofile_username.setText(user.getName());
            user.setName(getString(R.string.you));
            setUpAccountBalances(user_id);
            setUpFriendsOwe();
            setUpOweFriends();
        }

        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            // Get the user's data
            makeMeRequest(session);
        }
        return view;
    }

    private void setUpAccountBalances(long user_id){
        if(user == null){
            user = travellerDBAdapter.getTraveller(user_id);
            user.setName(getString(R.string.you));
        }
        List<Account> accounts = accountDBAdapter.getAccounts(user.get_id());
        userprofile_account_balances.removeAllViews();
        for(Account account:accounts){
            View account_balance_label = inflater.inflate(R.layout.account_balance_label, null);
            TextView account_currency = (TextView) account_balance_label.findViewById(R.id.account_currency);
            String currencyCode = account.getCurrency();
            Currency currency = Currency.getInstance(currencyCode);
            account_currency.setText(currencyCode + " " + currency.getSymbol());
            int backgroundColor;
            switch (currencyCode) {
                case CreateANewTripFragment.CANADA_CURRENCY_CODE:
                    backgroundColor = getResources().getColor(R.color.canadaColor);
                    break;
                case CreateANewTripFragment.EUROPE_CURRENCY_CODE:
                    backgroundColor = getResources().getColor(R.color.franceColor);
                    break;
                case CreateANewTripFragment.UK_CURRENCY_CODE:
                    backgroundColor = getResources().getColor(R.color.ukColor);
                    break;
                case CreateANewTripFragment.US_CURRENCY_CODE:
                    backgroundColor = getResources().getColor(R.color.usColor);
                    break;
                default:
                    backgroundColor = Color.WHITE;
                    break;
            }
            account_balance_label.setBackgroundColor(backgroundColor);
            TextView account_balance = (TextView) account_balance_label.findViewById(R.id.account_balance);
            account_balance.setText(account.getBalance()+"");
            userprofile_account_balances.addView(account_balance_label);
        }
    }

    private void setUpFriendsOwe(){
        userprofile_friends_owe.removeAllViews();
        Map<Long, Owe> credits = oweDBAdapter.getCredits(user.get_id());

        for(Map.Entry<Long,Owe> entry : credits.entrySet()){
            View owe_row = inflater.inflate(R.layout.owe_row, null);
            ProfilePictureView owe_friend_picture = (ProfilePictureView) owe_row.findViewById(R.id.owe_friend_picture);
            TextView owe_friend_name = (TextView) owe_row.findViewById(R.id.owe_friend_name);
            Traveller traveller = travellerDBAdapter.getTraveller(entry.getKey());
            owe_friend_picture.setProfileId(traveller.getFbUserId());
            owe_friend_name.setText(traveller.getName());
            TextView owe_friend_amounts = (TextView) owe_row.findViewById(R.id.owe_friend_amounts);
            owe_friend_amounts.setText(entry.getValue().toString_totalAmounts());
            userprofile_friends_owe.addView(owe_row);
        }
    }

    private void setUpOweFriends(){
        userprofile_owe_friends.removeAllViews();
        Map<Long, Owe> debts = oweDBAdapter.getDebts(user.get_id());

        for(Map.Entry<Long,Owe> entry : debts.entrySet()){
            View owe_row = inflater.inflate(R.layout.owe_row, null);
            ProfilePictureView owe_friend_picture = (ProfilePictureView) owe_row.findViewById(R.id.owe_friend_picture);
            TextView owe_friend_name = (TextView) owe_row.findViewById(R.id.owe_friend_name);
            Traveller traveller = travellerDBAdapter.getTraveller(entry.getKey());
            owe_friend_picture.setProfileId(traveller.getFbUserId());
            owe_friend_name.setText(traveller.getName());
            TextView owe_friend_amounts = (TextView) owe_row.findViewById(R.id.owe_friend_amounts);
            owe_friend_amounts.setText(entry.getValue().toString_totalAmounts());
            userprofile_owe_friends.addView(owe_row);
        }
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (session != null && session.isOpened()) {
            //Log.i(TAG, "Logged in to Facebook");
            makeMeRequest(session);
        } else if (state.isClosed()) {
            if (session == null || !session.isOpened()){
                SharedPreferences sharedPreferences=getActivity().getSharedPreferences(ExSplitApplication.SHARED_PREF_FILE_USER_INFO, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.remove(ExSplitApplication.SHARED_PREF_KEY_USER_ID);
                editor.commit();
                userprofile_user_picture.setProfileId(null);
                userprofile_username.setText(getString(R.string.username));
                user = null;
                userprofile_account_balances.removeAllViews();
                userprofile_friends_owe.removeAllViews();
                userprofile_owe_friends.removeAllViews();
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
            public void onCompleted(GraphUser graphUser, Response response) {
                // If the response is successful
                if (session == Session.getActiveSession()) {
                    if (graphUser != null) {
                        userprofile_user_picture.setProfileId(graphUser.getId());
                        userprofile_username.setText(graphUser.getName());

                        SharedPreferences sharedPreferences=getActivity().getSharedPreferences(ExSplitApplication.SHARED_PREF_FILE_USER_INFO, Context.MODE_PRIVATE);
                        long user_id = sharedPreferences.getLong(ExSplitApplication.SHARED_PREF_KEY_USER_ID,-1);
                        if(user_id<0){
                            long _id = travellerDBAdapter.insert(graphUser.getName(), graphUser.getId());
                            SharedPreferences.Editor editor=sharedPreferences.edit();
                            editor.putLong(ExSplitApplication.SHARED_PREF_KEY_USER_ID, _id);
                            editor.commit();
                            setUpAccountBalances(_id);
                            setUpFriendsOwe();
                            setUpOweFriends();
                        }
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
