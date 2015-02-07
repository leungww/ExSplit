package fyp.leungww.exsplit;



import android.app.Application;

import com.facebook.model.GraphUser;

import java.util.List;

public class ExSplitApplication extends Application {
    private List<GraphUser> selectedTravellers;
    private long user_id = -1;

    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public List<GraphUser> getSelectedTravellers() {
        return selectedTravellers;
    }

    public void setSelectedTravellers(List<GraphUser> selectedTravellers) {
        this.selectedTravellers = selectedTravellers;
    }
}
