package it.vibwear.app.adapters;

import it.lampwireless.vibwear.app.R;
import it.vibwear.app.services.ChatNotificationService;
import it.vibwear.app.utils.VibWearUtil;
import it.vibwear.app.fragments.ChatDetailFragment;
import it.vibwear.app.utils.ChatPreference;
import it.vibwear.app.utils.NotificationPreference;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class ChatServiceItem extends ServiceItem {
	public final String IMO_PACKAGE_NAME = "com.imo.android.imoim";
    private NotificationPreference notificationPref;

    protected final String VIBWEAR_PACKAGE_NAME = "it.lampwireless.vibwear.app";

	public ChatServiceItem(Activity activity) {
		super(activity);
		this.switchPref = new ChatPreference(activity);
        this.notificationPref = new NotificationPreference(activity);

	}
	
	public void setIconView(ImageView icon) {
		this.iconWidget = icon;
		this.iconWidget.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                switchPref.switchState();

                iconWidget.setImageResource(switchPref.getImage());

                setLocalizedText();

            }
        });
		
		showUserIconSettings();
	}
	
	public void setTextView(TextView text) {
		this.textWidget = text;
		this.textWidget.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				FragmentManager fm = activity.getFragmentManager();
				FragmentTransaction ft = fm.beginTransaction();
				ChatDetailFragment chatFrag = new ChatDetailFragment();
				ft.replace(R.id.servicesLayout, chatFrag, "chatDetail");
				ft.addToBackStack(null);
				ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
				ft.commit();
				
			}
		});

		showUserTextSettings();
	}

	public void showUserTextSettings() {
		setLocalizedText();
	}
	
	@Override
	public boolean consume(Intent intent) {
		Bundle extraInfo = intent.getExtras();
		
		String sourcePackageName = extraInfo.getString("sourcePackageName");
		
		if(sourcePackageName.equals(IMO_PACKAGE_NAME)) {
			return switchPref.getState();
		} else {
			if(isBlockedNotification(sourcePackageName)) {
				return false;
			} else {
				return super.consume(intent);
			}
		}
		
	}

	private boolean isBlockedNotification(String packageName) {
        if(packageName.equalsIgnoreCase(VIBWEAR_PACKAGE_NAME))
            return true;

        ArrayList<Notification> blockedNotifications = notificationPref.getBlackList();

        for(Notification notification : blockedNotifications) {
            if(notification.getPackageName().equalsIgnoreCase(packageName))
                return true;
        }

        return false;

    }

    private void setLocalizedText() {
        String lang = Locale.getDefault().getLanguage();

        if(lang == "en") {
            if (isAccessibilityOn()) {
				textWidget.setText(VibWearUtil.getChatSummarySpanText(switchPref.getLabel()));
            } else {
				textWidget.setText(VibWearUtil.getChatSummarySpanText(activity.getResources().getString(R.string.chatServiceDesc)));
				if (switchPref.getState()) {
					switchPref.switchState();
				}
            }
        } else {
            if (isAccessibilityOn()) {
				textWidget.setText(switchPref.getLabel());
            } else {
				textWidget.setText(activity.getResources().getString(R.string.chatServiceDesc));
				if (switchPref.getState()) {
					switchPref.switchState();
				}
            }
        }
    }

	private boolean isAccessibilityOn() {
		return (ChatNotificationService.isAccessibilitySettingsOn(activity)) &&
				(ChatNotificationService.isVibwearAccessibilityServiceOn(activity));
	}

}
