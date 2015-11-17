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

import com.android.internal.logging.MetricsLogger;
import com.android.internal.utils.eos.EosActionUtils;

import com.android.internal.util.cm.ScreenType;
import com.android.settings.ButtonSettings;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.widget.SwitchBar;

import android.content.Context;
import cyanogenmod.hardware.CMHardwareManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.IWindowManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManagerGlobal;
import android.widget.Switch;
import android.widget.TextView;

public class NavigationSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String DISABLE_NAV_KEYS = "disable_nav_keys";
    private static final String KEY_NAVBAR_MODE = "systemui_navbar_mode";
    private static final String KEY_NAVMODE_SETTINGS = "navigation_mode_settings";
    private static final String KEY_CATEGORY_NAVIGATION_INTERFACE = "category_navbar_interface";
    private static final String KEY_CATEGORY_NAVIGATION_GENERAL = "category_navbar_general";
    private static final String KEY_NAVIGATION_BAR_LEFT = "navigation_bar_left";
    private static final String KEY_NAVIGATION_BAR_SIZE = "navigation_bar_size";

    private SwitchPreference mDisableNavigationKeys;
    private ListPreference mNavbarMode;
    private PreferenceScreen mSettingsTarget;
    private SwitchPreference mNavigationBarLeftPref;
    private PreferenceCategory mNavInterface;
    private PreferenceCategory mNavGeneral;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.navigation_settings);

        final PreferenceScreen prefScreen = getPreferenceScreen();

        mDisableNavigationKeys = (SwitchPreference) findPreference(DISABLE_NAV_KEYS);
        mNavInterface = (PreferenceCategory) findPreference(KEY_CATEGORY_NAVIGATION_INTERFACE);
        mNavGeneral = (PreferenceCategory) findPreference(KEY_CATEGORY_NAVIGATION_GENERAL);        

        boolean needsNavigationBar = false;
            try {
                IWindowManager wm = WindowManagerGlobal.getWindowManagerService();
                needsNavigationBar = wm.needsNavigationBar();
            } catch (RemoteException e) {
            }

            if (needsNavigationBar) {
                prefScreen.removePreference(mDisableNavigationKeys);
            } else {
                updateDisableNavkeysOption();
                mDisableNavigationKeys.setOnPreferenceChangeListener(this);
                mNavInterface.setEnabled(mDisableNavigationKeys.isChecked());
                mNavGeneral.setEnabled(mDisableNavigationKeys.isChecked());
            }


        mSettingsTarget = (PreferenceScreen) findPreference(KEY_NAVMODE_SETTINGS);

        mNavbarMode = (ListPreference) findPreference(KEY_NAVBAR_MODE);
        int val = Settings.Secure.getInt(getContentResolver(), Settings.Secure.NAVIGATION_BAR_MODE, 0);
        mNavbarMode.setDefaultValue(val);
        mNavbarMode.setValue(String.valueOf(val));
        mNavbarMode.setOnPreferenceChangeListener(this);
        updateSummaryFromValue(mNavbarMode, R.array.systemui_navbar_mode_entries,
                R.array.systemui_navbar_mode_values);
        updateSettingsTarget(val);

        // Navigation bar left
        mNavigationBarLeftPref = (SwitchPreference) findPreference(KEY_NAVIGATION_BAR_LEFT);
        if (!ScreenType.isPhone(getActivity())) {
            mNavGeneral.removePreference(mNavigationBarLeftPref);
            mNavigationBarLeftPref = null;
        }
    }

    private void updateDisableNavkeysOption() {
        boolean enabled = Settings.Secure.getInt(getActivity().getContentResolver(),
                Settings.Secure.DEV_FORCE_SHOW_NAVBAR, 0) != 0;
        mDisableNavigationKeys.setChecked(enabled);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.equals(mNavbarMode)) {
            int val = Integer.parseInt(((String) newValue).toString());
            boolean ret = Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.NAVIGATION_BAR_MODE, val);
            mNavbarMode.setValue(String.valueOf(val));
            updateSummaryFromValue(mNavbarMode, R.array.systemui_navbar_mode_entries,
                    R.array.systemui_navbar_mode_values);
            updateSettingsTarget(val);
            return ret;
        } else if (preference.equals(mDisableNavigationKeys)) {
            int val = ((Boolean) newValue) ? 1 : 0;
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.DEV_FORCE_SHOW_NAVBAR, val);
            updateDisableNavkeysOption();
            mNavInterface.setEnabled(mDisableNavigationKeys.isChecked());
            mNavGeneral.setEnabled(mDisableNavigationKeys.isChecked());
            ButtonSettings.restoreKeyDisabler(getActivity());
            return true;
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

    @Override
    protected int getMetricsCategory() {
        // todo add a constant in MetricsLogger.java
        return MetricsLogger.MAIN_SETTINGS;
    }
}
