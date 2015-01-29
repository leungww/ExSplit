package fyp.leungww.exsplit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.FacebookException;
import com.facebook.widget.FriendPickerFragment;
import com.facebook.widget.PickerFragment;


public class AddTravellerActivity extends FragmentActivity {
    public static final Uri FRIEND_PICKER = Uri.parse("picker://friend");

    private FriendPickerFragment friendPickerFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_traveller);

        Bundle args = getIntent().getExtras();
        Fragment fragment;
        Uri intentUri = getIntent().getData();

        if (FRIEND_PICKER.equals(intentUri)) {
            if (savedInstanceState == null) {
                friendPickerFragment = new FriendPickerFragment(args);
            } else {
                friendPickerFragment = (FriendPickerFragment) getSupportFragmentManager().findFragmentById(R.id.add_traveller_fragment);
            }
            // Set the listener to handle errors
            friendPickerFragment.setOnErrorListener(new PickerFragment.OnErrorListener() {
                @Override
                public void onError(PickerFragment<?> fragment, FacebookException error) {
                    AddTravellerActivity.this.onError(error);
                }
            });
            // Set the listener to handle button clicks
            friendPickerFragment.setOnDoneButtonClickedListener(new PickerFragment.OnDoneButtonClickedListener() {
                @Override
                public void onDoneButtonClicked(PickerFragment<?> fragment) {
                    finishActivity();
                }
            });
            fragment = friendPickerFragment;
        } else {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.add_traveller_fragment, fragment).commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FRIEND_PICKER.equals(getIntent().getData())) {
            try {
                friendPickerFragment.loadData(false);
            } catch (Exception ex) {
                onError(ex);
            }
        }
    }

    private void onError(Exception error) {
        onError(error.getLocalizedMessage(), false);
    }

    private void onError(String error, final boolean finishActivity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.error_dialog_title).setMessage(error).setPositiveButton(R.string.error_dialog_button_text,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            if (finishActivity) {
                finishActivity();
            }
            }
        });
        builder.show();
    }

    private void finishActivity() {
        ExSplitApplication app = (ExSplitApplication) getApplication();
        if (FRIEND_PICKER.equals(getIntent().getData())) {
            if (friendPickerFragment != null) {
                app.setSelectedTravellers(friendPickerFragment.getSelection());
            }
        }
        setResult(RESULT_OK, null);
        finish();
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
