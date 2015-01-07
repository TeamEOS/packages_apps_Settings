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
import org.teameos.utils.EosUtils;
import org.cyanogenmod.hardware.KeyDisabler;

import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.cyanogenmod.BaseSystemSettingSwitchBar;
import com.android.settings.widget.SwitchBar;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

public class NavigationSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, BaseSystemSettingSwitchBar.SwitchBarChangeCallback {

    private static final String NAVBAR_MODE = "systemui_navbar_mode";
    private static final String NAVMODE_SETTINGS = "navigation_mode_settings";
    private static final String NX_ENABLE_URI = "eos_nx_enabled";
    private static final String KEY_CATEGORY_NAVIGATION_GENERAL = "category_navbar_general";
    private static final String KEY_NAVIGATION_BAR_LEFT = "navigation_bar_left";

    private ListPreference mNavbarMode;
    private PreferenceScreen mSettingsTarget;
    private SwitchPreference mNavigationBarLeftPref;

    private BaseSystemSettingSwitchBar mEnabledSwitch;
    private boolean mLastEnabledState;
    private ViewGroup mPrefsContainer;
    private TextView mDisabledText;
    private boolean mHasHardwareKeys;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.navigation_settings);

        // only show switch bar to enable navigation bar on hardware key devices
        mHasHardwareKeys = EosUtils.isCapKeyDevice(getActivity());

        mSettingsTarget = (PreferenceScreen) findPreference(NAVMODE_SETTINGS);

        mNavbarMode = (ListPreference) findPreference(NAVBAR_MODE);
        int val = Settings.System.getInt(getContentResolver(), NX_ENABLE_URI, 0);
        mNavbarMode.setDefaultValue(val);
        mNavbarMode.setValue(String.valueOf(val));
        mNavbarMode.setOnPreferenceChangeListener(this);
        updateSummaryFromValue(mNavbarMode, R.array.systemui_navbar_mode_entries,
                R.array.systemui_navbar_mode_values);
        updateSettingsTarget(val);

        // Navigation bar left
        mNavigationBarLeftPref = (SwitchPreference) findPreference(KEY_NAVIGATION_BAR_LEFT);
        if (!Utils.isPhone(getActivity())) {
            PreferenceCategory navbarGeneral = (PreferenceCategory) findPreference(KEY_CATEGORY_NAVIGATION_GENERAL);
            navbarGeneral.removePreference(mNavigationBarLeftPref);
            // temporary handling until more settings are added to this category
            mNavigationBarLeftPref = null;
            getPreferenceScreen().removePreference(navbarGeneral);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.hideable_fragment, container, false);
        mPrefsContainer = (ViewGroup) v.findViewById(R.id.prefs_container);
        mDisabledText = (TextView) v.findViewById(R.id.disabled_text);

        StringBuilder builder = new StringBuilder();
        builder.append(getString(R.string.navigation_disabled_notice));
        if (!ButtonSettings.isKeyDisablerSupported()) {
            builder.append(" ").append(getString(R.string.navigation_disabled_extended));
        }
        mDisabledText.setText(builder.toString());

        View prefs = super.onCreateView(inflater, mPrefsContainer, savedInstanceState);
        mPrefsContainer.addView(prefs);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        final SettingsActivity activity = (SettingsActivity) getActivity();
        if (mHasHardwareKeys) {
            mEnabledSwitch = new BaseSystemSettingSwitchBar(activity, activity.getSwitchBar(),
                    Settings.System.DEV_FORCE_SHOW_NAVBAR, true, this);
        } else {
            mEnabledSwitch = null;
            updateEnabledState();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        final SettingsActivity activity = (SettingsActivity) getActivity();
        if (mHasHardwareKeys && mEnabledSwitch != null) {
            mEnabledSwitch.resume(activity);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mHasHardwareKeys && mEnabledSwitch != null) {
            mEnabledSwitch.pause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mHasHardwareKeys && mEnabledSwitch != null) {
            mEnabledSwitch.teardownSwitchBar();
        }
    }

    @Override
    public void onEnablerChanged(boolean isEnabled) {
        if (mHasHardwareKeys) {
            mLastEnabledState = isEnabled;
            updateEnabledState();
            ButtonSettings.restoreKeyDisabler(getActivity());
        }
    }

    private void updateEnabledState() {
        if (!mHasHardwareKeys) {
            mLastEnabledState = true;
        }
        mPrefsContainer.setVisibility(mLastEnabledState ? View.VISIBLE : View.GONE);
        mDisabledText.setVisibility(mLastEnabledState ? View.GONE : View.VISIBLE);
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
