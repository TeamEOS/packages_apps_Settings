/*
 * Copyright (C) 2012 The CyanogenMod project
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

package com.android.settings.cyanogenmod;

import org.codefirex.utils.CFXConstants;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class SystemUiSettings extends SettingsPreferenceFragment implements
		Preference.OnPreferenceChangeListener {
	private static final String TAG = "SystemSettings";
	private static final String NET_VISIBLE = "cfx_netstats_visible";
	private static final String NET_INTERVAL = "cfx_netstats_refresh_interval";

	ContentResolver mResolver;
	Context mContext;

	private CheckBoxPreference mVisible;
	private ListPreference mInterval;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.system_ui_settings);

		mContext = (Context) getActivity();
		mResolver = getActivity().getContentResolver();

		mVisible = (CheckBoxPreference) findPreference(NET_VISIBLE);
		mVisible.setChecked(Settings.System.getBoolean(mResolver,
				CFXConstants.STATUS_BAR_NETWORK_STATS, false));
		mVisible.setOnPreferenceChangeListener(this);

		mInterval = (ListPreference) findPreference(NET_INTERVAL);
		Long currentVal = Settings.System.getLong(mResolver,
				CFXConstants.STATUS_BAR_NETWORK_STATS_UPDATE_INTERVAL, 500);
		mInterval.setValue(String.valueOf(currentVal));
		mInterval.setOnPreferenceChangeListener(this);
		updateIntervalSummary();

	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.equals(mVisible)) {
			Settings.System.putBoolean(mResolver,
					CFXConstants.STATUS_BAR_NETWORK_STATS,
					((Boolean) newValue).booleanValue());
			return true;
		}
		if (preference.equals(mInterval)) {
			Settings.System.putLong(mResolver,
					CFXConstants.STATUS_BAR_NETWORK_STATS_UPDATE_INTERVAL,
					Long.parseLong(String.valueOf(newValue)));
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					updateIntervalSummary();
				}
			}, 100);
			return true;
		}
		return false;
	}

	private void updateIntervalSummary() {
		String[] entries = mContext.getResources().getStringArray(
				R.array.netstats_entries);
		String[] vals = mContext.getResources().getStringArray(
				R.array.netstats_values);
		String currentVal = mInterval.getValue();
		String newEntry = "";
		for (int i = 0; i < vals.length; i++) {
			if (vals[i].equals(currentVal)) {
				newEntry = entries[i];
				break;
			}
		}
		mInterval.setSummary(newEntry);
	}
}
