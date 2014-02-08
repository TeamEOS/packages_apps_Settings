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

import org.codefirex.utils.CFXConstants;
import org.codefirex.utils.CFXUtils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class SystemSettings extends SettingsPreferenceFragment {
	private static final String TAG = "SystemSettings";
	private static final String KEY_DEVICE_SETTINGS = "cfx_device_settings";
	private static final String DEVICE_SETTINGS_CM = "com.cyanogenmod.action.LAUNCH_DEVICE_SETTINGS";
	private static final String DEVICE_SETTINGS_EOS = "org.teameos.action.LAUNCH_DEVICE_SETTINGS";
	private static final String DEVICE_SETTINGS_CFX = "com.cfx.action.LAUNCH_DEVICE_SETTINGS";

	private Preference mDeviceSettings;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.system_settings);

		mDeviceSettings = findPreference(KEY_DEVICE_SETTINGS);
		initDeviceSettings();
	}

	private void initDeviceSettings() {
		ActivityInfo ai = null;
		Intent intent = null;
		String label = null;
		String summary = null;
		// first check if package is implicitly set
		String _package = getActivity().getString(
				R.string.config_device_settings_package);
		// not implicitly set, resolve common device settings intents
		// starting with the most common
		// we break on the first resolved one, if any
		if (TextUtils.isEmpty(_package)) {
			ai = getDeviceSettingsComponent(DEVICE_SETTINGS_CM);
			if (ai == null) {
				Log.i(TAG, DEVICE_SETTINGS_CM + " not found!");
				ai = getDeviceSettingsComponent(DEVICE_SETTINGS_EOS);
				if (ai == null) {
					Log.i(TAG, DEVICE_SETTINGS_EOS + " not found!");
					ai = getDeviceSettingsComponent(DEVICE_SETTINGS_CFX);
					if (ai == null) {
						Log.i(TAG,
								DEVICE_SETTINGS_CFX
										+ " not found! No device settings, removing pref");
						// we're done, device does not have Device Settings
						getPreferenceScreen().removePreference(mDeviceSettings);
						mDeviceSettings = null;
						return;
					}
				}
			}
			// componentName should be resolved here
			Log.i(TAG, "Found " + ai.name + " " + ai.packageName);
			intent = new Intent().setAction(Intent.ACTION_MAIN).setClassName(
					ai.packageName, ai.name);
			mDeviceSettings.setIntent(intent);
		} else {
			// package implicitly set in device config
			String _class = getActivity().getString(
					R.string.config_device_settings_class);
			if (TextUtils.isEmpty(_class)) {
				// if package is set, the class must be set too
				getPreferenceScreen().removePreference(mDeviceSettings);
				mDeviceSettings = null;
				return;
			}
			intent = new Intent().setAction(Intent.ACTION_MAIN).setClassName(
					_package, _class);
			// make sure device maintainer didn't derp their config
			if (intent.resolveActivity(getPackageManager()) != null) {
				mDeviceSettings.setIntent(intent);
				try {
					ai = getPackageManager().getActivityInfo(
							intent.getComponent(), 0);
				} catch (NameNotFoundException e) {
					e.printStackTrace();
					// should never get here
					getPreferenceScreen().removePreference(mDeviceSettings);
					mDeviceSettings = null;
					return;
				}
			} else {
				// invalid configuration, config set but settings app isn't
				// resolved
				getPreferenceScreen().removePreference(mDeviceSettings);
				mDeviceSettings = null;
				return;
			}
		}

		// the intent logic is handled, now for title and summary
		// if the title is not set in config, use application label
		label = getActivity().getString(R.string.config_device_settings_title);
		if (TextUtils.isEmpty(label)) {
			label = String.valueOf(getPackageManager().getApplicationLabel(
					ai.applicationInfo));
			if ("null".equals(label)) { // very strange if it gets here
				label = getActivity().getString(
						R.string.device_settings_title_def);
			}
		}

		// finally, summary. either custom or default
		summary = getActivity().getString(
				R.string.config_device_settings_summary);
		if (TextUtils.isEmpty(summary)) {
			summary = getActivity().getString(
					R.string.device_settings_summary_def);
		}

		mDeviceSettings.setTitle(label);
		mDeviceSettings.setSummary(summary);
	}

	ActivityInfo getDeviceSettingsComponent(String customAction) {
		ActivityInfo ai = null;
		Intent intent = new Intent(customAction);
		ResolveInfo info = getPackageManager().resolveActivity(intent,
				PackageManager.GET_RESOLVED_FILTER);
		if (info != null) {
			ai = info.activityInfo;
		}
		return ai;
	}
}
