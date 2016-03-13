package it.vibwear.app.adapters;

import it.lampwireless.vibwear.app.R;
import it.vibwear.app.utils.GACServiceManager;
import it.vibwear.app.utils.GpsServiceManager;
import it.vibwear.app.utils.VibWearUtil;
import it.vibwear.app.fragments.SosDetailFragment;
import it.vibwear.app.utils.SosPreference;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

public class SosServiceItem extends ServiceItem {
    private GpsServiceManager gpsServiceManager;

	public SosServiceItem(Activity activity) {
		super(activity);
		this.switchPref = new SosPreference(activity);
        this.gpsServiceManager = new GpsServiceManager(activity);
	}
	
	public void setIconView(ImageView icon) {
		this.iconWidget = icon;
        if(isHardwareSupported(PackageManager.FEATURE_TELEPHONY)) {
            this.iconWidget.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    switchPref.switchState();

                    if(isHardwareSupported(PackageManager.FEATURE_LOCATION_GPS)) {
                        if (switchPref.getState()) {
                            if (!gpsServiceManager.isEnabled())
                                showEnableGpsDialog();

                            GACServiceManager.getInstance(activity).startLocationUpdates();

                        } else {
                            GACServiceManager.getInstance(activity).stopLocationUpdates();
                        }
                    }

                    iconWidget.setImageResource(switchPref.getImage());

                    setLocalizedText();

                }
            });
        }

		showUserIconSettings();
	}

    public void setTextView(TextView text) {
		this.textWidget = text;
        if(isHardwareSupported(PackageManager.FEATURE_TELEPHONY)) {
            this.textWidget.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    FragmentManager fm = activity.getFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    SosDetailFragment sosFrag = new SosDetailFragment();
                    ft.replace(R.id.servicesLayout, sosFrag, "sosDetail");
                    ft.addToBackStack(null);
                    ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
                    ft.commit();

                }
            });
        }

		showUserTextSettings();
	}

	public void showUserTextSettings() {

        setLocalizedText();
	
	}

    @Override
    public boolean consume(Intent intent) {
        return switchPref.getState();
    }


    public void startLocationUpdates() {
        if(isHardwareSupported(PackageManager.FEATURE_LOCATION_GPS)) {
            if (switchPref.getState()) {
                GACServiceManager.getInstance(activity).startLocationUpdates();
            }
        }
    }

    public void stopLocationUpdates() {
        if(isHardwareSupported(PackageManager.FEATURE_LOCATION_GPS))
            GACServiceManager.getInstance(activity).stopLocationUpdates();
    }

    private void setLocalizedText() {
        String lang = Locale.getDefault().getLanguage();

        if(lang == "en")
            textWidget.setText(VibWearUtil.getSosSummarySpanText(switchPref.getLabel()));
        else
            textWidget.setText(switchPref.getLabel());
    }

    private void showEnableGpsDialog() {
        AlertDialog.Builder builder=new AlertDialog.Builder(activity);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setTitle(R.string.menu_about);
        builder.setMessage(R.string.sos_activate_gps_msg);
        builder.setPositiveButton(R.string.activate_gps_btn_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                gpsServiceManager.requestActivation();
            }
        });

        builder.setNegativeButton(R.string.activate_gps_btn_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.create().show();
    }

}
