package fyp.leungww.exsplit;



import android.app.Application;

import com.facebook.model.GraphUser;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;
import java.util.List;

public class ExSplitApplication extends Application {
    public static final String SHARED_PREF_FILE_USER_INFO="user";
    public static final String SHARED_PREF_KEY_USER_ID="user_id";
    public static final String PROPERTY_ID="UA-60772369-1";
    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
    }
    private List<GraphUser> selectedTravellers;

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<>();

    synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker tracker = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID)
                    : analytics.newTracker(R.xml.global_tracker);
                    /*: (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker)
                    : analytics.newTracker(R.xml.ecommerce_tracker);*/
            mTrackers.put(trackerId, tracker);
        }
        return mTrackers.get(trackerId);
    }

    public List<GraphUser> getSelectedTravellers() {
        return selectedTravellers;
    }

    public void setSelectedTravellers(List<GraphUser> selectedTravellers) {
        this.selectedTravellers = selectedTravellers;
    }
}
