/*
 * Copyright (C) 2014 TeamEos
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

package com.android.settings.eos;

import org.teameos.utils.EosConstants;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;

public class NavigationSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String NAVBAR_MODE = "systemui_navbar_mode";
    private static final String NAVMODE_SETTINGS = "navigation_mode_settings";
    private static final String NX_ENABLE_URI = "eos_nx_enabled";

    private ListPreference mNavbarMode;
    private PreferenceScreen mSettingsTarget;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.navigation_settings);

        mSettingsTarget = (PreferenceScreen) findPreference(NAVMODE_SETTINGS);

        mNavbarMode = (ListPreference) findPreference(NAVBAR_MODE);
        int val = Settings.System.getInt(getContentResolver(), NX_ENABLE_URI, 0);
        mNavbarMode.setDefaultValue(val);
        mNavbarMode.setValue(String.valueOf(val));
        mNavbarMode.setOnPreferenceChangeListener(this);
        updateSummaryFromValue(mNavbarMode, R.array.systemui_navbar_mode_entries,
                R.array.systemui_navbar_mode_values);
        updateSettingsTarget(val);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.equals(mNavbarMode)) {
            int val = Integer.parseInt(((String) newValue).toString());
            boolean ret = Settings.System.putInt(getContentResolver(),
                    NX_ENABLE_URI, val);
            mNavbarMode.setValue(String.valueOf(val));
            updateSummaryFromValue(mNavbarMode, R.array.systemui_navbar_mode_entries,
                    R.array.systemui_navbar_mode_values);
            updateSettingsTarget(val);
            return ret;
        }
        return false;
    }

    private void updateSummaryFromValue(ListPreference pref, int entryRes, int valueRes) {
        String[] entries = getResources().getStringArray(entryRes);
        String[] vals = getResources().getStringArray(valueRes);
        String currentVal = pref.getValue();
        String newEntry = "";
        for (int i = 0; i < vals.length; i++) {
            if (vals[i].equals(currentVal)) {
                newEntry = entries[i];
                break;
            }
        }
        pref.setSummary(newEntry);
    }

    private void updateSettingsTarget(int val) {
        mSettingsTarget.setFragment(getResources().getStringArray(
                R.array.systemui_navbar_settings_fragments)[val]);
        mSettingsTarget.setTitle(getResources().getStringArray(
                R.array.systemui_navbar_mode_settings)[val]);
    }

}
