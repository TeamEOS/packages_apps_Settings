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

import org.codefirex.utils.CFXUtils;

import android.os.Bundle;
import android.os.UserHandle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class InterfaceSettings extends SettingsPreferenceFragment {
    private static final String TAG = InterfaceSettings.class.getSimpleName();
    private static final String NAVBAR_SETTINGS = "navigation_settings";

    private PreferenceScreen mNavbarSettings;
    private Preference mHeadsUp;
    private boolean mHasHardKeys;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.interface_settings);

        mHeadsUp = findPreference(Settings.System.HEADS_UP_NOTIFICATION);
        mNavbarSettings = (PreferenceScreen) findPreference(NAVBAR_SETTINGS);
        mHasHardKeys = CFXUtils.isCapKeyDevice(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean headsUpEnabled = Settings.System.getInt(
                getActivity().getContentResolver(),
                Settings.System.HEADS_UP_NOTIFICATION, 0) == 1;
        mHeadsUp.setSummary(headsUpEnabled
                ? R.string.summary_heads_up_enabled : R.string.summary_heads_up_disabled);

        if (mHasHardKeys) {
            boolean navEnabled = Settings.System.getIntForUser(getContentResolver(),
                    Settings.System.DEV_FORCE_SHOW_NAVBAR, 0,
                    UserHandle.USER_CURRENT) != 0;
            mNavbarSettings.setEnabled(navEnabled);
            mNavbarSettings.setSelectable(navEnabled);
        }
    }
}
