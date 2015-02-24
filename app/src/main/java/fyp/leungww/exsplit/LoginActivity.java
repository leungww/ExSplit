package fyp.leungww.exsplit;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;


public class LoginActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            // Add the fragment on initial activity setup
            LoginFragment loginFragment = new LoginFragment();
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, loginFragment).commit();
        } else {
            // Or set the fragment from restored state info
            getSupportFragmentManager().findFragmentById(android.R.id.content);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
