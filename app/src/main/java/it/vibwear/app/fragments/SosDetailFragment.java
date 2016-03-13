package it.vibwear.app.fragments;

import java.util.List;

import it.lampwireless.vibwear.app.R;
import it.vibwear.app.adapters.Contact;
import it.vibwear.app.adapters.ContactListAdapter;
import it.vibwear.app.utils.SosPreference;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

public class SosDetailFragment extends Fragment {
	// Declare
	static final int PICK_CONTACT=1;
    protected Context context;
	protected SosPreference sosPreference;
	protected ContactListAdapter adapter;
	
	protected EditText etSosMsg;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		context = container.getContext();

		View sosDetail = inflater.inflate(R.layout.fragment_sos_detail, container, false);
		ImageButton btSosContacts = (ImageButton) sosDetail.findViewById(R.id.bt_sos_contacts);

		btSosContacts.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(intent, PICK_CONTACT);
			}
		});

		ListView lvSosContacts = (ListView) sosDetail.findViewById(R.id.lv_sos_contacts);

		sosPreference = new SosPreference(context);

		List<Contact> contacts = sosPreference.getContacts();

		adapter = new ContactListAdapter(container.getContext(), contacts);

		lvSosContacts.setAdapter(adapter);

		etSosMsg = (EditText) sosDetail.findViewById(R.id.et_sos_msg);

		etSosMsg.setText(sosPreference.getSosMessage());

		CheckBox cbMyPosition = (CheckBox) sosDetail.findViewById(R.id.cb_my_position);

		cbMyPosition.setChecked(sosPreference.getMyPositionPref());

		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
			cbMyPosition.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					sosPreference.setMyPositionPref(isChecked);
				}
			});
		} else {
			cbMyPosition.setEnabled(false);
		}

        setListViewHeightBasedOnChildren(lvSosContacts);

        unsetParentScrollFor(lvSosContacts);

        return sosDetail;
	}

	@Override
	public void onDestroyView() {
		sosPreference.setSosMessage(etSosMsg.getText().toString());
		super.onDestroyView();

	}

	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {
		case (PICK_CONTACT):
			if (resultCode == Activity.RESULT_OK) {

				Uri contactData = data.getData();
				Cursor c = context.getContentResolver().query(contactData, null, null, null, null);
				if (c.moveToFirst()) {

					String id = c
							.getString(c
									.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

					if(!adapter.hasContact(new Contact(id))) {
						Contact contact = new Contact();
						contact.setId(id);
	
						String hasPhone = c
								.getString(c
										.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
	
						if (hasPhone.equalsIgnoreCase("1")) {
							Cursor phones = context.getContentResolver()
									.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
											null,
											ContactsContract.CommonDataKinds.Phone.CONTACT_ID
													+ " = " + id, null, null);
							phones.moveToFirst();

							String name = c
									.getString(c
											.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
							contact.setName(name);
							
							String cNumber = phones.getString(phones.getColumnIndex("data1"));
//							Log.d("Contacts", "number is:" + cNumber);
							
							contact.setPhone(cNumber);

							adapter.add(contact);
						} else {
							Toast.makeText(context, R.string.contact_with_number_msg, Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
			break;
		}
	}

    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView  ****/
    public static void setListViewHeightBasedOnChildren(ListView listView) {
//        ListAdapter listAdapter = listView.getAdapter();
//        if (listAdapter == null)
//            return;
//
//        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
//        int totalHeight = 0;
//        View view = null;
//        for (int i = 0; i < listAdapter.getCount(); i++) {
//            view = listAdapter.getView(i, view, listView);
//            if (i == 0)
//                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
//
//            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
//            totalHeight += view.getMeasuredHeight();
//        }
//        ViewGroup.LayoutParams params = listView.getLayoutParams();
//        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
//        listView.setLayoutParams(params);
//        listView.requestLayout();

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = 300;
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    private void unsetParentScrollFor(ListView listView) {
        listView.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
    }

}
