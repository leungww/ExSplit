package fyp.leungww.exsplit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.FriendPickerFragment;
import com.facebook.widget.PickerFragment;

import java.util.List;


public class AddTravellerActivity extends FragmentActivity {

    private FriendPickerFragment friendPickerFragment;

    public static void populateParameters(Intent intent, String userId, boolean multiSelect, boolean showTitleBar) {
        intent.putExtra(FriendPickerFragment.USER_ID_BUNDLE_KEY, userId);
        intent.putExtra(FriendPickerFragment.MULTI_SELECT_BUNDLE_KEY, multiSelect);
        intent.putExtra(FriendPickerFragment.SHOW_TITLE_BAR_BUNDLE_KEY, showTitleBar);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_traveller);

        if (savedInstanceState == null) {
            final Bundle args = getIntent().getExtras();
            friendPickerFragment = new FriendPickerFragment(args);
            getSupportFragmentManager().beginTransaction().add(R.id.add_traveller_fragment, friendPickerFragment).commit();
        } else {
            friendPickerFragment = (FriendPickerFragment) getSupportFragmentManager().findFragmentById(R.id.add_traveller_fragment);
        }

        friendPickerFragment.setOnErrorListener(new PickerFragment.OnErrorListener() {
            @Override
            public void onError(PickerFragment<?> fragment, FacebookException error) {
                AddTravellerActivity.this.onError(error);
            }
        });

        friendPickerFragment.setOnDoneButtonClickedListener(new PickerFragment.OnDoneButtonClickedListener() {
            @Override
            public void onDoneButtonClicked(PickerFragment<?> fragment) {
                ExSplitApplication application = (ExSplitApplication) getApplication();
                application.setSelectedTravellers(friendPickerFragment.getSelection());

                setResult(RESULT_OK, null);
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            ExSplitApplication application = (ExSplitApplication) getApplication();
            List<GraphUser> selectedTravellers = application.getSelectedTravellers();
            if (selectedTravellers != null && !selectedTravellers.isEmpty()) {
                friendPickerFragment.setSelection(selectedTravellers);
            }
            // Load data, unless a query has already taken place.
            friendPickerFragment.loadData(false);
        } catch (Exception e) {
            onError(e);
        }
    }

    private void onError(Exception error) {
        String text = getString(R.string.exception, error.getMessage());
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_traveller, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
