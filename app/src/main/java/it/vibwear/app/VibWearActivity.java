package it.vibwear.app;

import it.lampwireless.vibwear.app.R;
import it.vibwear.app.fragments.LocationFragment;
import it.vibwear.app.fragments.ServicesFragment;
import it.vibwear.app.fragments.AlarmFragment.AlarmListner;
import it.vibwear.app.fragments.LocationFragment.OnLocationChangeListener;
import it.vibwear.app.fragments.SettingsDetailFragment;
import it.vibwear.app.fragments.StopNotificationDialog;
import it.vibwear.app.handlers.StopDialogHandler;
import it.vibwear.app.receivers.StopNotificationReceiver;
import it.vibwear.app.scanner.ScannerFragment;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import com.mbientlab.metawear.api.GATT;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class VibWearActivity extends ModuleActivity implements OnLocationChangeListener, SettingsDetailFragment.OnSettingsChangeListener, AlarmListner {
	private static final String VERSION = "1.5.4";
	private static final long SIGNAL_START_DELAY = 10000;
	private static final long SIGNAL_SCHEDULE_TIME = 15000;
	private static final long BATTERY_START_DELAY = 60000;
	private static final long BATTERY_SCHEDULE_TIME = 60000;
	private final int VIBWEAR_NOTIFICATION_ID = 9571;
    private final int DISMISS_DIALOG_MSG = 0;
    private final int DISMISS_DIALOG_TIMEOUT = 5000;
	private LocationFragment locationFrag;
	private ServicesFragment servicesFrag;
	private Timer signalTimer;
	private Timer batteryTimer;
	private PowerManager powerMgr;
	private Notification.Builder mBuilder;
	protected ProgressDialog progress;

	IntentFilter intentFilter;

	private BroadcastReceiver intentReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			intent.putExtra("standBy", isStandBy());

			if(isDeviceConnected() && servicesFrag.consumeIntent(intent)) {
				vibrate(ModuleActivity.NOTIFY_VIB_MODE, intent);
				updateNotificationTextWith(intent);
                //showStopNotificationDialog(intent);
			}

			servicesFrag.update(intent);

		}
			
	};
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        getActionBar().setDisplayHomeAsUpEnabled(true);

		initializeView(savedInstanceState);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
 
        case R.id.menu_test:
            if (isDeviceConnected())
				vibrate(ModuleActivity.NOTIFY_VIB_MODE, null);
            
            break;
 
        case R.id.menu_about:
        	AlertDialog.Builder builder=new AlertDialog.Builder(this);
        	builder.setIcon(R.drawable.ic_launcher);
        	builder.setTitle(R.string.menu_about);
        	if(firmwareVersion != null) {
            	builder.setMessage("VibWear v. " + VERSION + "\nFirmware v. " + firmwareVersion);
        	} else {
        		builder.setMessage("VibWear v. " + VERSION);
        	}
        	builder.setCancelable(true);
        	builder.create();
        	builder.show();
        }
 
		return true;
    }
        
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(intentReceiver, intentFilter);
        startScheduledTimers();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(isFinishing())
			showNotificationIcon(false);
		unregisterReceiver(intentReceiver);
	}

	@Override
	protected void onPause() {
		super.onPause();
		cancelScheduledTimers();
	}

	@Override
	public void onBackPressed() {
		if (isDeviceConnected()) {
			if(getFragmentManager().getBackStackEntryCount() == 0) {
				moveTaskToBack(true);
				return;
			}
		} else {
            if(isReconnectTaskRunning()) {
                moveTaskToBack(true);
                return;
            }
        }
		super.onBackPressed();
	}
	
    @Override
    public void invalidateOptionsMenu() {
    	super.invalidateOptionsMenu();

		if (isDeviceConnected()) {
			locationFrag.updateConnectionImageResource(true);
			if(progress != null) progress.dismiss();
		} else {
			locationFrag.updateConnectionImageResource(false);
		}
    }
    
	@Override
	public void onLocationChange() {
		if(isDeviceConnected()) {
			unbindDevice();
            locationFrag.updateConnectionImageResource(false);
		} else {
            if(isReconnectTaskRunning()) {
                stopReconnectTaskAndUnbindDevice();
            }

            if(!startBluetoothAdapter())
                startDeviceScanner();
		}

	}

	@Override
	public void onDeviceSelected(BluetoothDevice device, String name) {
		super.onDeviceSelected(device, name);
		progress = new ProgressDialog(this);
		progress.setTitle(R.string.progressTitle);
		progress.setMessage(getResources().getString(R.string.progressMsg));
		progress.show();
	}

	@Override
	public void onSignalRequest() {
		if(isDeviceConnected()) {
            Toast.makeText(this,
                    getString(R.string.signal_level_msg,
                            (locationFrag.getCurrentSignalLevel() * 2)), Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public void onBatteryRequest() {
		if (isDeviceConnected()) {
            Toast.makeText(this,
                    getString(R.string.battery_level_msg,
                            locationFrag.getCurrentBatteryLevel()), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void updateSignalLevel(int rssiPercent) {
		locationFrag.updateSignalImageResource(rssiPercent);
	}

	@Override
	protected void updateBatteryLevel(String batteryLevel) {
		locationFrag.updateBatteryLevelImageResource(batteryLevel);
	}
	
	@Override
	public void onLowSignal() {
		//vibrate(ModuleActivity.LOW_SIGNAL_VIB_MODE, null);
	}

    @Override
    public void onLowBattery() {
        SharedPreferences settings = getSharedPreferences(SettingsDetailFragment.LOW_BATTERY_PREFS_NAME,
                Context.MODE_PRIVATE);

		if (settings.getBoolean(SettingsDetailFragment.NOTIFY_ME_KEY, false)) {
            vibrate(ModuleActivity.LOW_BATTERY_VIB_MODE, null);
//            requestUserAttention();
        }
    }

    @Override
    public void onBoardNameChange(String boardName) {
        if(isDeviceConnected()) {
            settingsController.setDeviceName(boardName);
            deviceName = boardName;
        }
    }

	protected void initializeView(Bundle savedInstanceState) {
		if(savedInstanceState != null) {
			locationFrag = (LocationFragment) getFragmentManager().getFragment(savedInstanceState, "locationFragment");
			servicesFrag = (ServicesFragment) getFragmentManager().getFragment(savedInstanceState, "servicesFragment");
		} else {
			FragmentManager fm = getFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			locationFrag = new LocationFragment();
			servicesFrag = new ServicesFragment();

			ft.add(R.id.locationLayout, locationFrag, "locationFragment");
			ft.add(R.id.servicesLayout, servicesFrag, "servicesFragment");
			ft.commit();

		}
		
		powerMgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mBuilder = new Notification.Builder(this);

		intentFilter = new IntentFilter();
		intentFilter.addAction(ServicesFragment.CALL_VIB_ACTION);
		intentFilter.addAction(ServicesFragment.SMS_VIB_ACTION);
		intentFilter.addAction(ServicesFragment.ALARM_VIB_ACTION);
		intentFilter.addAction(ServicesFragment.CHAT_VIB_ACTION);
		intentFilter.addAction(ServicesFragment.AUDIO_VIB_ACTION);

	}
	
	private void startScheduledTimers() {
		if (signalTimer == null) {
			signalTimer = new Timer();
			signalTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    requestSignalLevel();
                }
            }, SIGNAL_START_DELAY, SIGNAL_SCHEDULE_TIME);
		}

		if (batteryTimer == null) {
			batteryTimer = new Timer();
			batteryTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    requestBatteryLevel();
                }
            }, BATTERY_START_DELAY, BATTERY_SCHEDULE_TIME);
		}
		
	}
	
	private void cancelScheduledTimers() {

        if (batteryTimer != null) {
            batteryTimer.cancel();
        }

        if (signalTimer != null) {
            signalTimer.cancel();
        }

		batteryTimer = null;
		signalTimer = null;
	}

	public void onTimeAlarmChanged() {
		servicesFrag.refresh();
	}
	
	private boolean isStandBy() {
		return !powerMgr.isScreenOn();
	}

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        getFragmentManager().putFragment(outState, "locationFragment", locationFrag);
        getFragmentManager().putFragment(outState, "servicesFragment", servicesFrag);

    }

    private void requestUserAttention() {
        Intent intent = new Intent(getBaseContext(), VibWearActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplication().startActivity(intent);
    }

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(isDeviceConnected())
			showNotificationIcon(!hasFocus);
	}

	protected void showNotificationIcon(boolean show) {
		Random generator = new Random();
		if(show) {
			mBuilder.setSmallIcon(R.drawable.ic_launcher)
					.setTicker("Vibwear app listening")
					.setContentTitle("VibWear")
					.setContentText("Tap to show.");
			// Creates an explicit intent for an Activity in your app
			Intent startIntent = new Intent(this, VibWearActivity.class);

			PendingIntent startPendingIntent =
					PendingIntent.getActivity(this, 0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			mBuilder.setContentIntent(startPendingIntent);
			mBuilder.setOngoing(true);
			mwService.startForeground(VIBWEAR_NOTIFICATION_ID, mBuilder.build());
		} else {
			mwService.stopForeground(true);
		}
	}

	protected void updateNotificationTextWith(Intent intent) {
		Bundle extraInfo = intent.getExtras();

		String sourcePackageName = extraInfo.getString("sourcePackageName");

        if(sourcePackageName != null) {
            Drawable packageIcon = null;

            try {
                packageIcon = getPackageManager().getApplicationIcon(sourcePackageName);
            } catch (PackageManager.NameNotFoundException e) {
                //e.printStackTrace();
            }

            Intent stopIntent = new Intent(this, StopNotificationReceiver.class);
            stopIntent.putExtra("sourcePackageName", sourcePackageName);
            PendingIntent stopPendingIntent = PendingIntent.getActivity(this, 0, stopIntent, 0);

            //mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC)
            mBuilder.setContentText(getResources().getString(R.string.stop_notification_msg));
            mBuilder.addAction(R.drawable.ic_menu_reset,
                    getResources().getString(R.string.stop_notification_btn_confirm), stopPendingIntent);
            mBuilder.setOngoing(true);
            mwService.startForeground(VIBWEAR_NOTIFICATION_ID, mBuilder.build());
        }

	}

//    protected void showStopNotificationDialog(Intent intent) {
//        String sourcePackageName = intent.getExtras().getString("sourcePackageName");
//
//        if(sourcePackageName != null) {
//            DialogFragment stopNotificationDialog = new StopNotificationDialog();
//            stopNotificationDialog.setArguments(intent.getExtras());
//            stopNotificationDialog.show(getFragmentManager(), "stopNotificationDialog");
//
//            StopDialogHandler stopDialogHandler = new StopDialogHandler(stopNotificationDialog);
//            stopDialogHandler.sendEmptyMessageDelayed(DISMISS_DIALOG_MSG, DISMISS_DIALOG_TIMEOUT);
//        }
//    }

    protected void startDeviceScanner() {
        FragmentManager fm = getFragmentManager();
        ScannerFragment dialog = ScannerFragment.getInstance(VibWearActivity.this,
                new UUID[]{GATT.GATTService.METAWEAR.uuid()}, true);
        dialog.show(fm, "scan_fragment");
    }

}
