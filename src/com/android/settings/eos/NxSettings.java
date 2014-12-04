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

import java.util.ArrayList;

import com.android.settings.R;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.preference.SwitchPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.provider.Settings;

public class NxSettings extends ActionSettings implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = NxSettings.class.getSimpleName();
    private static final String NX_TRAILS_ENABLE_KEY = "eos_nx_trails_enable";

    Context mContext;

    SwitchPreference mNxTrailsEnable;
    SwitchPreference mShowLogo;
    SwitchPreference mAnimateLogo;
    SwitchPreference mShowPulse;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.nx_settings);

        ActionBar bar = getActivity().getActionBar();
        if (bar != null) {
            bar.setTitle(R.string.eos_nx_interface);
        }

        mContext = (Context) getActivity();

        mNxTrailsEnable = (SwitchPreference) findPreference(NX_TRAILS_ENABLE_KEY);
        mNxTrailsEnable.setChecked(Settings.System.getInt(getContentResolver(),
                "eos_nx_trails_enabled", 0) == 1);
        mNxTrailsEnable.setOnPreferenceChangeListener(this);

        mShowLogo = (SwitchPreference) findPreference("eos_nx_show_logo");
        mShowLogo.setChecked(Settings.System.getInt(getContentResolver(),
                "nx_logo_visible", 1) == 1);
        mShowLogo.setOnPreferenceChangeListener(this);

        mAnimateLogo = (SwitchPreference) findPreference("eos_nx_animate_logo");
        mAnimateLogo.setChecked(Settings.System.getInt(getContentResolver(),
                "nx_logo_animates", 1) == 1);
        mAnimateLogo.setOnPreferenceChangeListener(this);

        mShowPulse = (SwitchPreference) findPreference("eos_nx_show_pulse");
        mShowPulse.setChecked(Settings.System.getInt(getContentResolver(),
                "eos_nx_pulse", 0) == 1);
        mShowPulse.setOnPreferenceChangeListener(this);

        PreferenceCategory appearance = (PreferenceCategory) findPreference("eos_nx_appearance");
        appearance.removePreference(mNxTrailsEnable); // disable trails for now

        onPreferenceScreenLoaded();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.equals(mNxTrailsEnable)) {
            boolean enabled = ((Boolean) newValue).booleanValue();
            Settings.System.putInt(getContentResolver(),
                    "eos_nx_trails_enabled", enabled ? 1 : 0);
            return true;
        } else if (preference.equals(mShowLogo)) {
            boolean enabled = ((Boolean) newValue).booleanValue();
            Settings.System.putInt(getContentResolver(),
                    "nx_logo_visible", enabled ? 1 : 0);
            return true;
        } else if (preference.equals(mAnimateLogo)) {
            boolean enabled = ((Boolean) newValue).booleanValue();
            Settings.System.putInt(getContentResolver(),
                    "nx_logo_animates", enabled ? 1 : 0);
            return true;
        } else if (preference.equals(mShowPulse)) {
            boolean enabled = ((Boolean) newValue).booleanValue();
            Settings.System.putInt(getContentResolver(),
                    "eos_nx_pulse", enabled ? 1 : 0);
            return true;
        }
        return false;
    }

    protected boolean usesExtendedActionsList() {
        return true;
    }

    protected void onActionPolicyEnforced(ArrayList<ActionPreference> prefs) {
        enforceAction(prefs, "task_back");
        enforceAction(prefs, "task_home");
    }

    /*
     * Iterate the list: if only one instance, disable it otherwise, enable
     */
    private void enforceAction(ArrayList<ActionPreference> prefs, String action) {
        ArrayList<ActionPreference> actionPrefs = new ArrayList<ActionPreference>();
        for (ActionPreference pref : prefs) {
            if (pref.getAction().equals(action)) {
                actionPrefs.add(pref);
            }
        }
        boolean moreThanOne = actionPrefs.size() > 1;
        for (ActionPreference pref : actionPrefs) {
            pref.setEnabled(moreThanOne);
        }
    }
}
