/*
 * Copyright (C) 2014 CodefireX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.cfx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codefirex.utils.CFXConstants;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.android.internal.util.cm.QSUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.SubSettings;

public abstract class ActionSettings extends SettingsPreferenceFragment {
	private static final String TAG = ActionSettings.class.getSimpleName();
	private static final int REQUEST_CODE = 5150;

    private ArrayList<ActionPreference> mPrefHolder = new ArrayList<ActionPreference>();
	private String mHolderKey;

	private CharSequence[] mItem_entries;
	private CharSequence[] mItem_values;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionsList();
	}	

	@Override
	public void onStart() {
		super.onStart();
		for (ActionPreference pref : mPrefHolder) {
			pref.updateResources();
		}
	}

	protected boolean usesExtendedActionsList() {
		return false;
	}

	protected boolean onActionPolicyEnforced(ArrayList<ActionPreference> prefs, ActionPreference targetPref) {
		return false;
	}

	protected void onNotifyPolicyViolation(String action){}

    // populate holder list once everything is added and removed
	protected void onPreferenceScreenLoaded() {
		final PreferenceScreen prefScreen = getPreferenceScreen();
		for (int i = 0; i < prefScreen.getPreferenceCount(); i++) {
			Preference pref = prefScreen.getPreference(i);
			if (pref instanceof PreferenceCategory) {
				PreferenceCategory cat = (PreferenceCategory) pref;
				for (int j = 0; j < cat.getPreferenceCount(); j++) {
					Preference child = cat.getPreference(j);
					if (child instanceof ActionPreference) {
						mPrefHolder.add((ActionPreference) child);
						Log.i(TAG, child.getKey());
					}
				}
			} else if (pref instanceof ActionPreference) {
				mPrefHolder.add((ActionPreference) pref);
				Log.i(TAG, pref.getKey() + " added to button settings");
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				String myFlatComponent = data.getStringExtra("result");
				String myLabel = data.getStringExtra("label");
				for (ActionPreference pref : mPrefHolder) {
					if (pref.getKey().equals(mHolderKey)) {
						final String action = pref.getAction();
						if (!onActionPolicyEnforced(mPrefHolder, pref)) {
							pref.updateAction(myFlatComponent, myLabel);
						} else {
							new Handler().postDelayed(new Runnable() {
								@Override
								public void run() {
									onNotifyPolicyViolation(action);												
								}											
							}, 100);
						}
						break;
					}
				}
			}
		}
	}	

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		if (preference instanceof ActionPreference) {
			String key = preference.getKey();
			for (ActionPreference pref : mPrefHolder) {
				if (key.equals(pref.getKey())) {
					mHolderKey = key;
					callActionDialog();
					break;
				}
			}
		}
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    
	private void callActionDialog() {
		final CharSequence[] item_entries = mItem_entries;
		final CharSequence[] item_values = mItem_values;
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Set action")
				.setNegativeButton(
						getResources().getString(
								com.android.internal.R.string.cancel),
						new Dialog.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						})
				.setItems(item_entries, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String pressed = (String) item_values[which];
						if (pressed.equals("app")) {
							((SubSettings) getActivity())
									.registerActivityResultReceiverFragment(ActionSettings.this);
							Intent intent = new Intent().setClassName(
									"org.codefirex.cfxtools",
									"org.codefirex.cfxtools.PackageBrowser");
							getActivity().startActivityForResult(intent,
									REQUEST_CODE);
						} else {
							for (ActionPreference pref : mPrefHolder) {
								if (pref.getKey().equals(mHolderKey)) {
									final String action = pref.getAction();
									if (!onActionPolicyEnforced(mPrefHolder, pref)) {
									    pref.updateAction(pressed);
									} else {
										new Handler().postDelayed(new Runnable() {
											@Override
											public void run() {
												onNotifyPolicyViolation(action);												
											}											
										}, 100);
									}
									break;
								}
							}
						}
					}
				}).create().show();
	}

	private void setActionsList() {
		List<String> temp_entries = Arrays.asList(getResources()
				.getStringArray(R.array.action_dialog_entries));
		List<String> temp_values = Arrays.asList(getResources().getStringArray(
				R.array.action_dialog_values));

		ArrayList<String> item_entries = new ArrayList<String>();
		ArrayList<String> item_values = new ArrayList<String>();

		for (String s : temp_entries) {
			item_entries.add(s);
		}

		for (String s : temp_values) {
			item_values.add(s);
		}

		if (!usesExtendedActionsList()) {
			int i = item_values.indexOf("task_home");
			item_entries.remove(i);
			item_values.remove(i);

			i = item_values.indexOf("task_back");
			item_entries.remove(i);
			item_values.remove(i);			
		}

		if (!QSUtils.deviceSupportsMobileData(getActivity())) {
			int i = item_values.indexOf(CFXConstants.SYSTEMUI_TASK_WIFIAP);
			item_entries.remove(i);
			item_values.remove(i);
		}

		if (!QSUtils.deviceSupportsBluetooth()) {
			int i = item_values.indexOf(CFXConstants.SYSTEMUI_TASK_BT);
			item_entries.remove(i);
			item_values.remove(i);
		}

		if (!QSUtils.deviceSupportsTorch(getActivity())) {
			int i = item_values.indexOf(CFXConstants.SYSTEMUI_TASK_TORCH);
			item_entries.remove(i);
			item_values.remove(i);
		}

		mItem_entries = new CharSequence[item_entries.size()];
		mItem_values = new CharSequence[item_values.size()];

		int i = 0;
		for (String s : item_entries) {
			mItem_entries[i] = s;
			i++;
		}
		i = 0;
		for (String s : item_values) {
			mItem_values[i] = s;
			i++;
		}
	}
}
