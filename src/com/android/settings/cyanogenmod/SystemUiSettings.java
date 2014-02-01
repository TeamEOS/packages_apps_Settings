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
import org.codefirex.utils.CFXUtils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class SystemUiSettings extends SettingsPreferenceFragment implements
		Preference.OnPreferenceChangeListener {
	private static final String TAG = "SystemSettings";
	private static final String NET_VISIBLE = "cfx_netstats_visible";
	private static final String NET_INTERVAL = "cfx_netstats_refresh_interval";
	private static final String CAT_NAVIGATION ="cfx_systemui_navigation_bar";
	private static final String NAVBAR_SIZE = "cfx_interface_navbar_size";

	ContentResolver mResolver;
	Context mContext;

	private CheckBoxPreference mVisible;
	private ListPreference mInterval;
	private ListPreference mNavbarSize;

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

		if (!CFXUtils.hasNavBar(mContext)) {
			getPreferenceScreen().removePreference(
					(PreferenceCategory) getPreferenceScreen().findPreference(
							CAT_NAVIGATION));
		} else {
			mNavbarSize = (ListPreference) findPreference(NAVBAR_SIZE);
			int sizeVal = Settings.System.getInt(mResolver,
					CFXConstants.SYSTEMUI_NAVBAR_SIZE_DP,
					CFXConstants.SYSTEMUI_NAVBAR_SIZE_DEF_INDEX);
			mNavbarSize.setDefaultValue(String.valueOf(sizeVal));
			mNavbarSize.setValue(String.valueOf(sizeVal));
			mNavbarSize.setOnPreferenceChangeListener(this);
			updateSizeSummary();
		}

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
		} else if (preference.equals(mInterval)) {
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
		} else if (preference.equals(mNavbarSize)) {
			int val = Integer.parseInt(((String) newValue).toString());
			Settings.System.putInt(mResolver,
					CFXConstants.SYSTEMUI_NAVBAR_SIZE_DP, (val));
			Intent intent = new Intent().setAction(
					CFXConstants.ACTION_CFX_UI_CHANGE).putExtra(
					CFXConstants.INTENT_REASON_UI_CHANGE,
					CFXConstants.INTENT_REASON_UI_BAR_SIZE);
			mContext.sendBroadcastAsUser(intent, new UserHandle(
					UserHandle.USER_ALL));
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					updateSizeSummary();
				}
			}, 250);
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

	private void updateSizeSummary() {
		String[] entries = mContext.getResources().getStringArray(
				R.array.systemui_navbar_size_entries);
		String[] vals = mContext.getResources().getStringArray(
				R.array.systemui_navbar_size_values);
		String currentVal = mNavbarSize.getValue();
		String newEntry = "";
		for (int i = 0; i < vals.length; i++) {
			if (vals[i].equals(currentVal)) {
				newEntry = entries[i];
				break;
			}
		}
		mNavbarSize.setSummary(newEntry);
	}
}
