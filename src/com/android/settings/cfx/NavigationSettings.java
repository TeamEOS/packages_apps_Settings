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

package com.android.settings.cfx;

import org.codefirex.utils.CFXConstants;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.provider.Settings;

import com.android.settings.R;

public class NavigationSettings extends ActionSettings implements
        Preference.OnPreferenceChangeListener {
    private static final String NAVBAR_SIZE = "cfx_interface_navbar_size";

    ContentResolver mResolver;
    Context mContext;

    private ListPreference mNavbarSize;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.navigation_settings);

        mContext = (Context) getActivity();
        mResolver = getActivity().getContentResolver();

        mNavbarSize = (ListPreference) findPreference(NAVBAR_SIZE);
        int sizeVal = Settings.System.getInt(mResolver,
                CFXConstants.SYSTEMUI_NAVBAR_SIZE_DP,
                CFXConstants.SYSTEMUI_NAVBAR_SIZE_DEF_INDEX);
        mNavbarSize.setDefaultValue(String.valueOf(sizeVal));
        mNavbarSize.setValue(String.valueOf(sizeVal));
        mNavbarSize.setOnPreferenceChangeListener(this);
        updateSizeSummary();

        onPreferenceScreenLoaded();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.equals(mNavbarSize)) {
            int val = Integer.parseInt(((String) newValue).toString());
            Settings.System.putInt(mResolver,
                    CFXConstants.SYSTEMUI_NAVBAR_SIZE_DP, val);
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
