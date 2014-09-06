/*
 * Copyright (C) 2014 The TeamEos Project
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

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class LockscreenSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String BATTERY_AROUND_LOCKSCREEN_RING = "battery_around_lockscreen_ring";
    private static final String HIDE_SEARCH_BUTTON_KEY = "hide_search_button";
    private static final String HIDE_CAMERA_BUTTON_KEY = "hide_camera_button";

    private static final String HIDE_SEARCH_BUTTON_URI = "eos_navbar_lockscreen_hide_search";
    private static final String HIDE_CAMERA_BUTTON_URI = "eos_navbar_lockscreen_hide_camera";

    private CheckBoxPreference mLockRingBattery;
    private CheckBoxPreference mHideSearchButton;
    private CheckBoxPreference mHideCameraButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.lockscreen_settings);

        mLockRingBattery = (CheckBoxPreference) findPreference(BATTERY_AROUND_LOCKSCREEN_RING);
        if (mLockRingBattery != null) {
            mLockRingBattery.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.BATTERY_AROUND_LOCKSCREEN_RING, 0) == 1);
            mLockRingBattery.setOnPreferenceChangeListener(this);
        }

        mHideSearchButton = (CheckBoxPreference) findPreference(HIDE_SEARCH_BUTTON_KEY);
        if (mHideSearchButton != null) {
            mHideSearchButton.setChecked(Settings.System.getInt(getContentResolver(),
                    HIDE_SEARCH_BUTTON_URI, 0) == 1);
            mHideSearchButton.setOnPreferenceChangeListener(this);
        }

        mHideCameraButton = (CheckBoxPreference) findPreference(HIDE_CAMERA_BUTTON_KEY);
        if (mHideCameraButton != null) {
            mHideCameraButton.setChecked(Settings.System.getInt(getContentResolver(),
                    HIDE_CAMERA_BUTTON_URI, 0) == 1);
            mHideCameraButton.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLockRingBattery) {
            return Settings.System.putInt(getContentResolver(),
                    Settings.System.BATTERY_AROUND_LOCKSCREEN_RING,
                    ((Boolean) newValue).booleanValue() ? 1 : 0);
        } else if (preference == mHideSearchButton) {
            return Settings.System.putInt(getContentResolver(),
                    HIDE_SEARCH_BUTTON_URI,
                    ((Boolean) newValue).booleanValue() ? 1 : 0);
        } else if (preference == mHideCameraButton) {
            return Settings.System.putInt(getContentResolver(),
                    HIDE_CAMERA_BUTTON_URI,
                    ((Boolean) newValue).booleanValue() ? 1 : 0);
        }
        return false;
    }

}
