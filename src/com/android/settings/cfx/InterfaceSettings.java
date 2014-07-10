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

import org.codefirex.utils.CFXUtils;
import org.cyanogenmod.hardware.KeyDisabler;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class InterfaceSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "SystemSettings";
    private static final String NAVBAR_SETTINGS = "navigation_settings";
    private static final String NAVBAR_CATEGORY = "interface_navigation";
    private static final String NAVBAR_FORCE = "interface_force_navbar";

    PreferenceCategory mNavCat;
    Preference mNavNote;
    CheckBoxPreference mNavForce;
    PreferenceScreen mNavbarSettings;

    private DevForceNavbarObserver mObserver = null;
    private Preference mHeadsUp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.interface_settings);

        mNavCat = (PreferenceCategory) findPreference(NAVBAR_CATEGORY);

        mNavForce = (CheckBoxPreference) mNavCat.findPreference(NAVBAR_FORCE);
        mNavForce.setChecked(isForcedNavbar());
        mNavForce.setOnPreferenceChangeListener(this);

        mNavbarSettings = (PreferenceScreen) mNavCat.findPreference(NAVBAR_SETTINGS);

        if (!CFXUtils.isCapKeyDevice(getActivity())) {
            // device natively has navigation bar, remove the category
            // and add preference to screen hierarchy
            mNavCat.removePreference(mNavbarSettings);
            getPreferenceScreen().removePreference(mNavCat);
            getPreferenceScreen().addPreference(mNavbarSettings);
        } else {
            mObserver = new DevForceNavbarObserver(new Handler());
            updateForcedPrefsState();
        }

        mHeadsUp = findPreference(Settings.System.HEADS_UP_NOTIFICATION);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mObserver != null) {
            mObserver.observe();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean headsUpEnabled = Settings.System.getInt(
                getActivity().getContentResolver(),
                Settings.System.HEADS_UP_NOTIFICATION, 0) == 1;
        mHeadsUp.setSummary(headsUpEnabled
                ? R.string.summary_heads_up_enabled : R.string.summary_heads_up_disabled);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mObserver != null) {
            mObserver.unobserve();
        }
    }

    private boolean isForcedNavbar() {
        return Settings.System.getIntForUser(getContentResolver(),
                Settings.System.DEV_FORCE_SHOW_NAVBAR, 0,
                UserHandle.USER_CURRENT) == 1;
    }

    private void updateForcedPrefsState() {
        if (isForcedNavbar()) {
            if (mNavCat.findPreference(NAVBAR_SETTINGS) == null) {
                mNavCat.addPreference(mNavbarSettings);
                String summary;
                if (KeySettings.isKeyDisablerSupported()) {
                    summary = getResources().getString(R.string.eos_key_disabler_active);
                } else {
                    summary = getResources().getString(R.string.eos_key_disabler_unsupported);
                }
                mNavForce.setSummary(summary);
            }
        } else {
            mNavCat.removePreference(mNavbarSettings);
            mNavForce.setSummary(getResources().getString(R.string.eos_key_disabler_off));

        }
    }

    class DevForceNavbarObserver extends ContentObserver {
        DevForceNavbarObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = getContentResolver();
            resolver.registerContentObserver(Settings.System
                    .getUriFor(Settings.System.DEV_FORCE_SHOW_NAVBAR), false,
                    this);
        }

        void unobserve() {
            ContentResolver resolver = getContentResolver();
            resolver.unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateForcedPrefsState();
            KeySettings.restoreKeyDisabler((Context) getActivity());
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.equals(mNavForce)) {
            boolean enabled = ((Boolean) newValue).booleanValue();
            Settings.System.putBoolean(getContentResolver(),
                    Settings.System.DEV_FORCE_SHOW_NAVBAR, enabled);
            return true;
        }
        return false;
    }
}
