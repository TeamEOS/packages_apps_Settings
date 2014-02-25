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
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class DisplayPower extends SettingsPreferenceFragment implements
		Preference.OnPreferenceChangeListener {
	private static final String CAT_BATTERY = "battery_charging";
	private static final String BATTERY_WARNING = "disable_low_batt_dialog";

	ContentResolver mResolver;
	Context mContext;

	private CheckBoxPreference mBatteryWarning;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.display_power_settings);

		mContext = (Context) getActivity();
		mResolver = getActivity().getContentResolver();

		mBatteryWarning = (CheckBoxPreference) findPreference(BATTERY_WARNING);

		boolean enabled = Settings.System.getBoolean(mResolver,
				CFXConstants.SYSTEMUI_DISABLE_BATTERY_WARNING, false);
		mBatteryWarning.setChecked(enabled);
		mBatteryWarning.setOnPreferenceChangeListener(this);
		updateBatterySummary(enabled);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.equals(mBatteryWarning)) {
			boolean enabled = ((Boolean) newValue).booleanValue();
			Settings.System.putBoolean(mResolver,
					CFXConstants.SYSTEMUI_DISABLE_BATTERY_WARNING, enabled);
			updateBatterySummary(enabled);
			return true;
		}
		return false;
	}

	private void updateBatterySummary(boolean enabled) {
		mBatteryWarning
				.setSummary(enabled ? getString(R.string.battery_disable_low_dialog_summary_enabled)
						: getString(R.string.battery_disable_low_dialog_summary_disabled));
	}

}
