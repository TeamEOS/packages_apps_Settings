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

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.cyanogenmod.SystemSettingCheckBoxPreference;

public class StatusbarSettings extends ActionSettings implements
		Preference.OnPreferenceChangeListener {

    private static final String STATUS_BAR_BATTERY = "status_bar_battery";
    private static final String STATUS_BAR_SIGNAL = "status_bar_signal";
    private static final String STATUS_BAR_BATTERY_SHOW_PERCENT = "status_bar_battery_show_percent";
    private static final String STATUS_BAR_STYLE_HIDDEN = "4";
    private static final String STATUS_BAR_STYLE_TEXT = "6";

	private static final String NET_VISIBLE = "cfx_netstats_visible";
	private static final String NET_INTERVAL = "cfx_netstats_refresh_interval";
	private static final String WEATHER_DATE_VIEW = "cfx_weather_date_view";
	private static final String WEATHER_NOTIFICATION = "cfx_weather_notification";

	ContentResolver mResolver;
	Context mContext;

	private CheckBoxPreference mVisible;
	private ListPreference mInterval;
	private CheckBoxPreference mWeatherDate;
	private CheckBoxPreference mWeatherNot;
    private ListPreference mStatusBarBattery;
    private SystemSettingCheckBoxPreference mStatusBarBatteryShowPercent;
	private ListPreference mStatusbarClock;
	private ListPreference mAmPmStyle;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.statusbar_settings);

		mContext = (Context) getActivity();
		mResolver = getActivity().getContentResolver();

		mStatusBarBattery = (ListPreference) findPreference(STATUS_BAR_BATTERY);
		mStatusBarBatteryShowPercent = (SystemSettingCheckBoxPreference) findPreference(STATUS_BAR_BATTERY_SHOW_PERCENT);
		int batteryStyle = Settings.System.getInt(mResolver,
				Settings.System.STATUS_BAR_BATTERY, 0);
		mStatusBarBattery.setValue(String.valueOf(batteryStyle));
		mStatusBarBattery.setSummary(mStatusBarBattery.getEntry());
		mStatusBarBattery.setOnPreferenceChangeListener(this);

		mStatusbarClock = (ListPreference) findPreference("cfx_style_statusbar_clock_state");
		mStatusbarClock.setValue(String.valueOf(Settings.System.getInt(
				mResolver, CFXConstants.SYSTEMUI_CLOCK_VISIBLE,
				CFXConstants.SYSTEMUI_CLOCK_CLUSTER)));
		mStatusbarClock.setOnPreferenceChangeListener(this);

		mAmPmStyle = (ListPreference) findPreference("cfx_style_statusbar_clock_am_pm");
		String defValue = String.valueOf(Settings.System.getInt(mResolver,
				CFXConstants.SYSTEMUI_CLOCK_AMPM,
				CFXConstants.SYSTEMUI_CLOCK_AMPM_DEF));
		mAmPmStyle.setValue(defValue);
		mAmPmStyle.setOnPreferenceChangeListener(this);

		mVisible = (CheckBoxPreference) findPreference(NET_VISIBLE);
		mVisible.setChecked(Settings.System.getBoolean(mResolver,
				CFXConstants.STATUS_BAR_NETWORK_STATS, false));
		mVisible.setOnPreferenceChangeListener(this);

		mInterval = (ListPreference) findPreference(NET_INTERVAL);
		Long currentVal = Settings.System.getLong(mResolver,
				CFXConstants.STATUS_BAR_NETWORK_STATS_UPDATE_INTERVAL, 500);
		mInterval.setValue(String.valueOf(currentVal));
		mInterval.setOnPreferenceChangeListener(this);

		mWeatherDate = (CheckBoxPreference) findPreference(WEATHER_DATE_VIEW);
		mWeatherDate.setChecked(Settings.System.getBoolean(mResolver,
				CFXConstants.SYSTEMUI_WEATHER_HEADER_VIEW, false));
		mWeatherDate.setOnPreferenceChangeListener(this);

		mWeatherNot = (CheckBoxPreference) findPreference(WEATHER_NOTIFICATION);
		mWeatherNot.setChecked(Settings.System.getBoolean(mResolver, WEATHER_NOTIFICATION, false));
		mWeatherNot.setOnPreferenceChangeListener(this);

		updateIntervalSummary();
		enableStatusBarBatteryDependents(mStatusBarBattery.getValue());
		onPreferenceScreenLoaded();
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.equals(mStatusBarBattery)) {
			int batteryStyle = Integer.valueOf((String) newValue);
			int index = mStatusBarBattery.findIndexOfValue((String) newValue);
			Settings.System.putInt(mResolver,
					Settings.System.STATUS_BAR_BATTERY, batteryStyle);
			mStatusBarBattery.setSummary(mStatusBarBattery.getEntries()[index]);
			enableStatusBarBatteryDependents((String) newValue);
			return true;
		}else if (preference.equals(mStatusbarClock)) {
			int val = Integer.parseInt(((String) newValue).toString());
			Settings.System.putInt(mContext.getContentResolver(),
					CFXConstants.SYSTEMUI_CLOCK_VISIBLE, (val));
			return true;
		} else if (preference.equals(mAmPmStyle)) {
			int val = Integer.parseInt(((String) newValue).toString());
			Settings.System.putInt(mContext.getContentResolver(),
					CFXConstants.SYSTEMUI_CLOCK_AMPM, (val));
			return true;
		} else if (preference.equals(mVisible)) {
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
		} else if (preference.equals(mWeatherDate)) {
			Settings.System.putBoolean(mResolver,
					CFXConstants.SYSTEMUI_WEATHER_HEADER_VIEW,
					((Boolean) newValue).booleanValue());
			return true;
		} else if (preference.equals(mWeatherNot)) {
			Settings.System.putBoolean(mResolver, WEATHER_NOTIFICATION,
					((Boolean) newValue).booleanValue());
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

	private void enableStatusBarBatteryDependents(String value) {
		boolean enabled = !(value.equals(STATUS_BAR_STYLE_TEXT) || value
				.equals(STATUS_BAR_STYLE_HIDDEN));
		mStatusBarBatteryShowPercent.setEnabled(enabled);
	}
}
