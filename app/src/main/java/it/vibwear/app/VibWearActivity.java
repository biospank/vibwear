package it.vibwear.app;

import it.lampwireless.vibwear.app.R;
import it.vibwear.app.adapters.Contact;
import it.vibwear.app.fragments.DfuProgressFragment;
import it.vibwear.app.fragments.KillerAppDialogFragment;
import it.vibwear.app.fragments.LocationFragment;
import it.vibwear.app.fragments.ServicesFragment;
import it.vibwear.app.fragments.AlarmFragment.AlarmListner;
import it.vibwear.app.fragments.LocationFragment.OnLocationChangeListener;
import it.vibwear.app.fragments.SettingsDetailFragment;
import it.vibwear.app.notifications.PermanentNotification;
import it.vibwear.app.notifications.TemporaryNotification;
import it.vibwear.app.scanner.ScannerFragment;
import it.vibwear.app.utils.AppManager;
import it.vibwear.app.utils.GACServiceManager;
import it.vibwear.app.utils.SosPreference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.telephony.SmsManager;
import com.mbientlab.metawear.AsyncOperation;
import com.mbientlab.metawear.MetaWearBoard;

public class VibWearActivity extends ModuleActivity implements
        ScannerFragment.OnDeviceSelectedListener, OnLocationChangeListener,
        SettingsDetailFragment.OnSettingsChangeListener, AlarmListner {

	private static final String VERSION = "1.7.3";
	private static final long SIGNAL_START_DELAY = 10000;
	private static final long SIGNAL_SCHEDULE_TIME = 15000;
	private static final long BATTERY_START_DELAY = 60000;
	private static final long BATTERY_SCHEDULE_TIME = 60000;
	private LocationFragment locationFrag;
	private ServicesFragment servicesFrag;
	private Timer signalTimer;
	private Timer batteryTimer;
	private PowerManager powerMgr;
    private PermanentNotification pNotification;
	protected ProgressDialog progress;
	private KillerAppDialogFragment killerAppDialog;
    private GACServiceManager gacServiceManager;
    private Notification.Builder mBuilder;

	IntentFilter intentFilter;

	private BroadcastReceiver intentReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			intent.putExtra("standBy", isStandBy());

			if(isDeviceConnected() && servicesFrag.consumeIntent(intent)) {
				vibrate(ModuleActivity.NOTIFY_VIB_MODE, intent);
                showTemporaryNotification(intent);
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

		showKillerAppWarning();

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

			String firmwareVersion = mwConnectionFragment.getFirmwareVersion();

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
		if(isFinishing()) {
			if(killerAppDialog != null)
				killerAppDialog.setFirstRun(this, true);

			showPermanentNotification(false);
            gacServiceManager.disconnect();
		}

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
	
    public void updateUi() {
		if (isDeviceConnected()) {
            locationFrag.updateConnectionImageResource(true);
            if (progress != null)
                progress.dismiss();

			showPermanentNotification(true);
        } else {
			locationFrag.updateConnectionImageResource(false);
		}
    }
    
	@Override
	public void onLocationChange() {
		if(isDeviceConnected()) {
			mwConnectionFragment.unbindDevice();
            locationFrag.updateConnectionImageResource(false);
		} else {
            if(isReconnectTaskRunning()) {
                stopReconnectTaskAndUnbindDevice();
            }

            if(mwConnectionFragment.startBluetoothAdapter())
                startDeviceScanner();
		}

	}

	@Override
	public void onDeviceSelected(BluetoothDevice device, String name) {
		mwConnectionFragment.onDeviceSelected(device, name);
		progress = new ProgressDialog(this);
        progress.setTitle(R.string.progressTitle);
		progress.setMessage(getResources().getString(R.string.progressMsg));
		progress.show();
	}

	@Override
	public void onDialogCanceled() {

	}

	@Override
	public void onSignalRequest() {
        //restartMwService();
        //showPermanentNotification(true);
		if(isDeviceConnected()) {
            Toast.makeText(this,
                    getString(R.string.signal_level_msg,
                            (locationFrag.getCurrentSignalLevel() * 2)), Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public void onBatteryRequest() {
        //Intent intent = new Intent();
        //intent.putExtra("sourcePackageName", "com.viber.voip");
        //showTemporaryNotification(intent);
		if (isDeviceConnected()) {
            Toast.makeText(this,
					getString(R.string.battery_level_msg,
                            locationFrag.getCurrentBatteryLevel()), Toast.LENGTH_SHORT).show();
		}
	}

    public void updateSignalLevel(int rssiPercent) {
		locationFrag.updateSignalImageResource(rssiPercent);
	}

	public void updateBatteryLevel(String batteryLevel) {
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
		mwConnectionFragment.changeBoardName(boardName);
        mwConnectionFragment.setDeviceName(boardName);
    }

	@Override
	public void onFirmwareUpdate() {
        getMwBoard().checkForFirmwareUpdate().onComplete(new AsyncOperation.CompletionHandler<Boolean>() {
            @Override
            public void success(Boolean result) {
                AlertDialog.Builder builder = new AlertDialog.Builder(VibWearActivity.this);

                if (!result) {
                    setupDfuDialog(builder, R.string.message_dfu_latest);
                } else {
                    setupDfuDialog(builder, R.string.message_dfu_accept);
				}

				builder.create();
				builder.show();
			}
		});
	}

	public String getDeviceName() {
        return mwConnectionFragment.getDeviceName();
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

		intentFilter = new IntentFilter();
		intentFilter.addAction(ServicesFragment.CALL_VIB_ACTION);
		intentFilter.addAction(ServicesFragment.SMS_VIB_ACTION);
		intentFilter.addAction(ServicesFragment.ALARM_VIB_ACTION);
		intentFilter.addAction(ServicesFragment.CHAT_VIB_ACTION);
        intentFilter.addAction(ServicesFragment.AUDIO_VIB_ACTION);

        connectGoogleApiClient();

    }
	
	private void startScheduledTimers() {
		if (signalTimer == null) {
			signalTimer = new Timer();
			signalTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    mwConnectionFragment.requestSignalLevel();
                }
            }, SIGNAL_START_DELAY, SIGNAL_SCHEDULE_TIME);
		}

		if (batteryTimer == null) {
			batteryTimer = new Timer();
			batteryTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    mwConnectionFragment.requestBatteryLevel();
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

        FragmentManager fm = getFragmentManager();

        fm.putFragment(outState, "locationFragment", locationFrag);
        fm.putFragment(outState, "servicesFragment", servicesFrag);

    }

    private void requestUserAttention() {
        Intent intent = new Intent(getBaseContext(), VibWearActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplication().startActivity(intent);
    }

	//@Override
	//public void onWindowFocusChanged(boolean hasFocus) {
	//	super.onWindowFocusChanged(hasFocus);
	//	if(isDeviceConnected())
	//		showNotification(!hasFocus);
	//}

	public void showPermanentNotification(boolean show) {
		if(show) {
            if(pNotification == null)
                pNotification = new PermanentNotification(this);

            pNotification.show();
		} else {
			if(pNotification != null)
				pNotification.cancel();
		}
	}

	protected void showKillerAppWarning() {
		if(killerAppDialog == null)
			killerAppDialog = KillerAppDialogFragment.newInstance(getApplicationContext());

		if (killerAppDialog.isFirstRun(this)) {
			ArrayList<String> killerApps = AppManager.findKillerApps(this);

			if ((!killerApps.isEmpty()) && (!killerAppDialog.isHideMe(this))) {

				Bundle bundle = new Bundle();
				bundle.putStringArray("killer.apps", killerApps.toArray(new String[0]));

				killerAppDialog.setArguments(bundle);
				killerAppDialog.show(getFragmentManager(), null);

			}

			killerAppDialog.setFirstRun(this, false);

		}

	}

    public void sendTextMessage() {
		Intent sosIntent = new Intent();
		sosIntent.setAction(ServicesFragment.SOS_VIB_ACTION);

		if(servicesFrag.consumeIntent(sosIntent)) {

			SosPreference sosPreference = new SosPreference(getApplicationContext());
			List<Contact> contacts = sosPreference.getContacts();
			SmsManager smsManager = SmsManager.getDefault();

			String msg = sosPreference.getSosMessage();

			if (msg.isEmpty()) {
                if (sosPreference.getMyPositionPref()) {
                    msg = gacServiceManager.getLocationUrlMap();
                } else {
                    msg = getString(R.string.sos_default_msg);
                }
            } else {
                if (sosPreference.getMyPositionPref()) {
                    msg += " " + gacServiceManager.getLocationUrlMap();
                }
            }

			for (Contact contact : contacts) {
				smsManager.sendTextMessage(contact.getPhone(), null, msg, null, null);
			}

		}
    }

    public void startServiceUpdates() {
        servicesFrag.getSosService().startLocationUpdates();
    }

    public void stopServiceUpdates() {
        servicesFrag.getSosService().stopLocationUpdates();
    }

    protected void showTemporaryNotification(Intent intent) {
        Bundle extraInfo = intent.getExtras();

        String sourcePackageName = extraInfo.getString("sourcePackageName");

        if(sourcePackageName != null)
            new TemporaryNotification(this, sourcePackageName).show();
        
	}

	protected void startDeviceScanner() {
        FragmentManager fm = getFragmentManager();
        ScannerFragment dialog = ScannerFragment.getInstance(VibWearActivity.this,
				new UUID[]{MetaWearBoard.METAWEAR_SERVICE_UUID}, true);
        dialog.show(fm, "scan_fragment");
    }

	private void setupDfuDialog(AlertDialog.Builder builder, int msgResId) {
		builder.setTitle(R.string.title_dfu_dialog)
				.setPositiveButton(R.string.label_dfu_dialog_confirm, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						initiateDfu();
					}
				})
				.setNegativeButton(R.string.label_dfu_dialog_cancel, null)
				.setCancelable(false)
				.setMessage(msgResId);
	}

	private void initiateDfu() {
		final String DFU_PROGRESS_FRAGMENT_TAG= "dfu_progress_popup";
		DfuProgressFragment dfuProgressDialog= new DfuProgressFragment();
		dfuProgressDialog.show(getFragmentManager(), DFU_PROGRESS_FRAGMENT_TAG);

        runOnUiThread(new Runnable() {
            final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            final Notification.Builder checkpointNotifyBuilder = new Notification.Builder(VibWearActivity.this).setSmallIcon(android.R.drawable.stat_sys_upload)
                    .setOnlyAlertOnce(true).setOngoing(true).setProgress(0, 0, true);
            final Notification.Builder progressNotifyBuilder = new Notification.Builder(VibWearActivity.this).setSmallIcon(android.R.drawable.stat_sys_upload)
                    .setOnlyAlertOnce(true).setOngoing(true).setContentTitle(getString(R.string.notify_dfu_uploading));
            final int NOTIFICATION_ID = 1024;

            @Override
            public void run() {
                getMwBoard().updateFirmware(new MetaWearBoard.DfuProgressHandler() {
                    @Override
                    public void reachedCheckpoint(State dfuState) {
                        switch (dfuState) {
                            case INITIALIZING:
                                checkpointNotifyBuilder.setContentTitle(getString(R.string.notify_dfu_bootloader));
                                break;
                            case STARTING:
                                checkpointNotifyBuilder.setContentTitle(getString(R.string.notify_dfu_starting));
                                break;
                            case VALIDATING:
                                checkpointNotifyBuilder.setContentTitle(getString(R.string.notify_dfu_validating));
                                break;
                            case DISCONNECTING:
                                checkpointNotifyBuilder.setContentTitle(getString(R.string.notify_dfu_disconnecting));
                                break;
                        }

                        manager.notify(NOTIFICATION_ID, checkpointNotifyBuilder.build());
                    }

                    @Override
                    public void receivedUploadProgress(int progress) {
                        progressNotifyBuilder.setContentText(String.format("%d%%", progress)).setProgress(100, progress, false);
                        manager.notify(NOTIFICATION_ID, progressNotifyBuilder.build());
                        ((DfuProgressFragment) getFragmentManager().findFragmentByTag(DFU_PROGRESS_FRAGMENT_TAG)).updateProgress(progress);
                    }
                }).onComplete(new AsyncOperation.CompletionHandler<Void>() {
                    final Notification.Builder builder = new Notification.Builder(VibWearActivity.this).setOnlyAlertOnce(true)
                            .setOngoing(false).setAutoCancel(true);

                    @Override
                    public void success(Void result) {
                        ((DialogFragment) getFragmentManager().findFragmentByTag(DFU_PROGRESS_FRAGMENT_TAG)).dismiss();
                        builder.setContentTitle(getString(R.string.notify_dfu_success)).setSmallIcon(android.R.drawable.stat_sys_upload_done);
                        manager.notify(NOTIFICATION_ID, builder.build());

                        Toast.makeText(VibWearActivity.this, R.string.message_dfu_success, Toast.LENGTH_LONG).show();
                        resetConnectionStateHandler();
                    }

                    @Override
                    public void failure(Throwable error) {
                        Log.e("MetaWearApp", "Firmware update failed", error);

                        Throwable cause = error.getCause() == null ? error : error.getCause();
                        ((DialogFragment) getFragmentManager().findFragmentByTag(DFU_PROGRESS_FRAGMENT_TAG)).dismiss();
                        builder.setContentTitle(getString(R.string.notify_dfu_fail)).setSmallIcon(android.R.drawable.ic_dialog_alert)
                                .setContentText(cause.getLocalizedMessage());
                        manager.notify(NOTIFICATION_ID, builder.build());

                        Toast.makeText(VibWearActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
						resetConnectionStateHandler();
					}
				});
			}
		});
	}

    private void connectGoogleApiClient() {
        gacServiceManager = GACServiceManager.getInstance(this);
        gacServiceManager.connect();

    }
}
