/*
 * Copyright (C) 2012 CyanogenMod
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


import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.content.ContentResolver;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class PowerMenu extends SettingsPreferenceFragment {
    CheckBoxPreference mRebootAdvanced;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.power_menu_settings);

        mRebootAdvanced = (CheckBoxPreference) findPreference(Settings.Secure.ADVANCED_REBOOT);
        mRebootAdvanced
                .setChecked(Settings.Secure.getInt(getActivity()
                        .getContentResolver(), Settings.Secure.ADVANCED_REBOOT,
                        0) == 1);
        mRebootAdvanced
                .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                    @Override
                    public boolean onPreferenceChange(Preference preference,
                            Object newValue) {
                        Settings.Secure.putInt(getActivity()
                                .getContentResolver(),
                                Settings.Secure.ADVANCED_REBOOT,
                                ((Boolean) newValue).booleanValue() ? 1 : 0);
                        return true;
                    }
                });
    }
}
