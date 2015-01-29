package fyp.leungww.exsplit;


import android.app.Application;

import com.facebook.model.GraphUser;

import java.util.List;

public class ExSplitApplication extends Application{
    private List<GraphUser> selectedTravellers;

    public List<GraphUser> getSelectedTravellers() {
        return selectedTravellers;
    }

    public void setSelectedTravellers(List<GraphUser> selectedTravellers) {
        this.selectedTravellers = selectedTravellers;
    }
}
