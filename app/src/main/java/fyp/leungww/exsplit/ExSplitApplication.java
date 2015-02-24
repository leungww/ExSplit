package fyp.leungww.exsplit;



import android.app.Application;

import com.facebook.model.GraphUser;

import java.util.List;

public class ExSplitApplication extends Application {
    public static final String SHARED_PREF_FILE_USER_INFO="user";
    public static final String SHARED_PREF_KEY_USER_ID="user_id";
    private List<GraphUser> selectedTravellers;

    public List<GraphUser> getSelectedTravellers() {
        return selectedTravellers;
    }

    public void setSelectedTravellers(List<GraphUser> selectedTravellers) {
        this.selectedTravellers = selectedTravellers;
    }
}
