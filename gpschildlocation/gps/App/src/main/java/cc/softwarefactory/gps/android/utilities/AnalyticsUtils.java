package cc.softwarefactory.lokki.android.utilities;


import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class AnalyticsUtils {
    private static Tracker tracker;
    private static GoogleAnalytics analytics;

    public static void initAnalytics(Context context) {
        analytics = GoogleAnalytics.getInstance(context);
        int globalTrackerConfigXmlId = context.getResources().getIdentifier("analytics_global_tracker_config", "xml", context.getPackageName());
        if (globalTrackerConfigXmlId == 0) {
            tracker = null;
            return;
        }
        // Google Analytics uses "true" value to disable analytics,
        // whereas Lokki uses "true" to enable analytics (to work well with CheckBoxes)
        analytics.setAppOptOut(!PreferenceUtils.getBoolean(context, PreferenceUtils.KEY_SETTING_ANALYTICS_OPT_IN));
        tracker = analytics.newTracker(globalTrackerConfigXmlId);
        tracker.set("&uid", Utils.getDeviceId());
    }

    public static void screenHit(String screenName) {
        if (tracker == null) {
            return;
        }
        tracker.setScreenName(screenName);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }


    public static void eventHit(String category, String action, String label, long value) {
        if (tracker == null) {
            return;
        }
        HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action);
        if (label != null) {
            eventBuilder = eventBuilder.setLabel(label);
        }
        if (value != -1) {
            eventBuilder = eventBuilder.setValue(value);
        }
        tracker.send(eventBuilder.build());
    }

    public static void eventHit(String category, String action, String label) {
        eventHit(category, action, label, -1);
    }

    public static void eventHit(String category, String action, long value) {
        eventHit(category, action, null, value);
    }

    public static void eventHit(String category, String action) {
        eventHit(category, action, null, -1);
    }

    public static void setAnalyticsOptIn(boolean optInState) {
        // Google Analytics uses "true" value to disable analytics,
        // whereas Lokki uses "true" to enable analytics (to work well with CheckBoxes)
        analytics.setAppOptOut(!optInState);
    }

}
