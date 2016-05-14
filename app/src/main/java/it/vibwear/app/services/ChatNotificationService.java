package it.vibwear.app.services;

import it.vibwear.app.fragments.ServicesFragment;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class ChatNotificationService extends AccessibilityService {

    static final String vibwearAccessibilityService = "it.lampwireless.vibwear.app/it.vibwear.app.services.ChatNotificationService";

	@Override
	public void onAccessibilityEvent(AccessibilityEvent evt) {
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(ServicesFragment.CHAT_VIB_ACTION);
		broadcastIntent.putExtra("sourcePackageName", evt.getPackageName());
		getApplicationContext().sendBroadcast(broadcastIntent);
		
	}

	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onServiceConnected() {
		AccessibilityServiceInfo info = new AccessibilityServiceInfo();
	    // Set the type of events that this service wants to listen to.  Others
	    // won't be passed to this service.
	    info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED; // AccessibilityEvent.TYPE_VIEW_CLICKED | AccessibilityEvent.TYPE_VIEW_FOCUSED;  

	    // If you only want this service to work with specific applications, set their
	    // package names here.  Otherwise, when the service is activated, it will listen
	    // to events from all applications.
	    
	    // Set the type of feedback your service will provide.
	    info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;

	    // Default services are invoked only if no package-specific ones are present
	    // for the type of AccessibilityEvent generated.  This service *is*
	    // application-specific, so the flag isn't necessary.  If this was a
	    // general-purpose service, it would be worth considering setting the
	    // DEFAULT flag.

	    info.flags = AccessibilityServiceInfo.DEFAULT;

	    info.notificationTimeout = 100;

	    this.setServiceInfo(info);

	}
	
	// To check if service is enabled
	public static boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;

        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (SettingNotFoundException e) {
            return false;
        }

        return (accessibilityEnabled == 1) ? true : false;
    }

    public static boolean isVibwearAccessibilityServiceOn(Context mContext) {
        return findVibwearAccessibilityService(getEnabledAccessibilityServices(mContext));
    }

    public static boolean hasAccessibilityConflicts(Context mContext) {
        String enabledServices = getEnabledAccessibilityServices(mContext);

        String conflictService = findConflictAccessibilityService(enabledServices);

        return (conflictService != null) ? true : false;
    }

    private static String getEnabledAccessibilityServices(Context mContext) {
        return Settings.Secure.getString(
                mContext.getApplicationContext().getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
    }

    private static boolean findVibwearAccessibilityService(String enabledServices) {
        if ((enabledServices != null) && (!enabledServices.isEmpty())) {
            TextUtils.SimpleStringSplitter splitter = getServiceSplitterFor(enabledServices);

            while (splitter.hasNext()) {
                String accessabilityService = splitter.next();

                if (accessabilityService.equalsIgnoreCase(vibwearAccessibilityService)) {
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    private static String findConflictAccessibilityService(String enabledServices) {
        if ((enabledServices != null) && (!enabledServices.isEmpty())) {
            TextUtils.SimpleStringSplitter splitter = getServiceSplitterFor(enabledServices);

            while (splitter.hasNext()) {
                String accessabilityService = splitter.next();

                if (!accessabilityService.equalsIgnoreCase(vibwearAccessibilityService)) {
                    return accessabilityService;
                }
            }

            return null;
        } else {
            return null;
        }
    }

    private static TextUtils.SimpleStringSplitter getServiceSplitterFor(String enabledServices) {
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
        splitter.setString(enabledServices);

        return splitter;
    }
	
}
