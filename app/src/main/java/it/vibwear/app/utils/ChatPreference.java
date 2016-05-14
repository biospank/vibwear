package it.vibwear.app.utils;

import it.lampwireless.vibwear.app.R;
import it.vibwear.app.services.ChatNotificationService;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.provider.Settings;

public class ChatPreference implements VibrationPreference, SwitchPreference {
    public static final String CHAT_PREFS_NAME = "CHAT_DETAILS";
    public static final String CHAT_VIB_TIME = "Chat_vib_time";
	public static final String CHAT_KEY_PREF = "pref_key_chat";
    
	protected Context context;

    public ChatPreference(Context context) {
        super();
        this.context = context;
    }
 
	public int getVibrationTime() {
		SharedPreferences settings = context.getSharedPreferences(CHAT_PREFS_NAME,
                Context.MODE_PRIVATE);
        return settings.getInt(CHAT_VIB_TIME, DEFAULT_VIB_TIME);
        
	}

	public void setVibrationTime(int progress) {
		Editor editor;
		SharedPreferences settings = context.getSharedPreferences(CHAT_PREFS_NAME,
                Context.MODE_PRIVATE);
        editor = settings.edit();
        
        editor.putInt(CHAT_VIB_TIME, progress);
        
        editor.commit();
		
	}

	@Override
	public boolean switchState() {
		Editor editor;
		boolean active = false;
		SharedPreferences settings = context.getSharedPreferences(CHAT_PREFS_NAME,
                Context.MODE_PRIVATE);
        editor = settings.edit();
        
		if(settings.getBoolean(CHAT_KEY_PREF, false)) {
			editor.putBoolean(CHAT_KEY_PREF, false);
			active = false;

		} else {
			
			if (isAccessibilityOn()) {

                if(ChatNotificationService.hasAccessibilityConflicts(context)) {
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.accessibility_dialog_title)
                            .setMessage(R.string.accessibility_conflicts_dialog_msg)
                            .setIcon(R.drawable.ic_launcher)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                                }})

                            .setNegativeButton(android.R.string.no, null).show();

                } else {
                    editor.putBoolean(CHAT_KEY_PREF, true);
                    active = true;
                }

            } else {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.accessibility_dialog_title)
                        .setMessage(R.string.accessibility_dialog_msg)
                        .setIcon(R.drawable.ic_launcher)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                            }})

                        .setNegativeButton(android.R.string.no, null).show();

	        }

		}

		editor.apply();
		
		return active;
		
	}

	public void setState(boolean state) {
		Editor editor;
		SharedPreferences settings = context.getSharedPreferences(CHAT_PREFS_NAME,
                Context.MODE_PRIVATE);
        editor = settings.edit();
        editor.putBoolean(CHAT_KEY_PREF, state);
		editor.apply();
		
	}

	@Override
	public boolean getState() {
		SharedPreferences settings = context.getSharedPreferences(CHAT_PREFS_NAME,
                Context.MODE_PRIVATE);
		return settings.getBoolean(CHAT_KEY_PREF, false);
		
	}
	
	@Override
	public String getLabel() {
		SharedPreferences settings = context.getSharedPreferences(CHAT_PREFS_NAME,
                Context.MODE_PRIVATE);
		if(settings.getBoolean(CHAT_KEY_PREF, false))
			return context.getString(R.string.activeChatServiceDesc);
		else
			return context.getString(R.string.chatServiceDesc);
	}
	
	@Override
	public int getImage() {
		SharedPreferences settings = context.getSharedPreferences(CHAT_PREFS_NAME,
                Context.MODE_PRIVATE);
		if(settings.getBoolean(CHAT_KEY_PREF, false))
			return R.drawable.ic_chat_active;
		else
			return R.drawable.ic_chat;
	}

    private boolean isAccessibilityOn() {
        return (ChatNotificationService.isAccessibilitySettingsOn(context)) &&
                (ChatNotificationService.isVibwearAccessibilityServiceOn(context));
    }

}
