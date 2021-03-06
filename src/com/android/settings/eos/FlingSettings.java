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

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.utils.eos.ActionConstants;
import com.android.internal.utils.eos.ActionHandler;
import com.android.settings.R;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.SwitchPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.provider.Settings;

public class FlingSettings extends ActionFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = FlingSettings.class.getSimpleName();

    Context mContext;

    SwitchPreference mShowLogo;
    SwitchPreference mAnimateLogo;
    SwitchPreference mShowPulse;
    SwitchPreference mShowRipple;
    SwitchPreference mLavaLampEnabled;
    SwitchPreference mTrailsEnabled;

    ColorPickerPreference mLogoColor;
    ColorPickerPreference mRippleColor;
    ColorPickerPreference mPulseColor;
    ColorPickerPreference mTrailsColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fling_settings);

        ActionBar bar = getActivity().getActionBar();
        if (bar != null) {
            bar.setTitle(R.string.fling_interface);
        }

        mContext = (Context) getActivity();

        mShowLogo = (SwitchPreference) findPreference("eos_fling_show_logo");
        mShowLogo.setChecked(Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.FLING_LOGO_VISIBLE, 1) == 1);
        mShowLogo.setOnPreferenceChangeListener(this);

        mAnimateLogo = (SwitchPreference) findPreference("eos_fling_animate_logo");
        mAnimateLogo.setChecked(Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.FLING_LOGO_ANIMATES, 1) == 1);
        mAnimateLogo.setOnPreferenceChangeListener(this);

        int logoColor = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.FLING_LOGO_COLOR, Color.WHITE, UserHandle.USER_CURRENT);
        mLogoColor = (ColorPickerPreference) findPreference("eos_fling_logo_color");
        mLogoColor.setNewPreviewColor(logoColor);
        mLogoColor.setOnPreferenceChangeListener(this);

        mShowRipple = (SwitchPreference) findPreference("eos_fling_show_ripple");
        mShowRipple.setChecked(Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.FLING_RIPPLE_ENABLED, 1) == 1);
        mShowRipple.setOnPreferenceChangeListener(this);

        int rippleColor = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.FLING_RIPPLE_COLOR, Color.WHITE, UserHandle.USER_CURRENT);
        mRippleColor = (ColorPickerPreference) findPreference("eos_fling_ripple_color");
        mRippleColor.setNewPreviewColor(rippleColor);
        mRippleColor.setOnPreferenceChangeListener(this);

        mTrailsEnabled = (SwitchPreference) findPreference("eos_fling_trails_enable");
        mTrailsEnabled.setChecked(Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.FLING_TRAILS_ENABLED, 1) == 1);
        mTrailsEnabled.setOnPreferenceChangeListener(this);

        int trailsColor = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.FLING_TRAILS_COLOR, Color.WHITE, UserHandle.USER_CURRENT);
        mTrailsColor = (ColorPickerPreference) findPreference("eos_fling_trails_color");
        mTrailsColor.setNewPreviewColor(trailsColor);
        mTrailsColor.setOnPreferenceChangeListener(this);

        mShowPulse = (SwitchPreference) findPreference("eos_fling_show_pulse");
        mShowPulse.setChecked(Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.FLING_PULSE_ENABLED, 1) == 1);
        mShowPulse.setOnPreferenceChangeListener(this);

        int pulseColor = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.FLING_PULSE_COLOR, Color.WHITE, UserHandle.USER_CURRENT);
        mPulseColor = (ColorPickerPreference) findPreference("eos_fling_pulse_color");
        mPulseColor.setNewPreviewColor(pulseColor);
        mPulseColor.setOnPreferenceChangeListener(this);

        mLavaLampEnabled = (SwitchPreference) findPreference("eos_fling_lavalamp");
        mLavaLampEnabled.setChecked(Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.FLING_PULSE_LAVALAMP_ENABLED, 1) == 1);
        mLavaLampEnabled.setOnPreferenceChangeListener(this);

        onPreferenceScreenLoaded(ActionConstants.getDefaults(ActionConstants.FLING));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.equals(mShowLogo)) {
            boolean enabled = ((Boolean) newValue).booleanValue();
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.FLING_LOGO_VISIBLE, enabled ? 1 : 0);
            return true;
        } else if (preference.equals(mAnimateLogo)) {
            boolean enabled = ((Boolean) newValue).booleanValue();
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.FLING_LOGO_ANIMATES, enabled ? 1 : 0);
            return true;
        } else if (preference.equals(mLogoColor)) {
            int color = ((Integer) newValue).intValue();
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.FLING_LOGO_COLOR, color);
            return true;
        } else if (preference.equals(mShowPulse)) {
            boolean enabled = ((Boolean) newValue).booleanValue();
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.FLING_PULSE_ENABLED, enabled ? 1 : 0);
            return true;
        } else if (preference.equals(mShowRipple)) {
            boolean enabled = ((Boolean) newValue).booleanValue();
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.FLING_RIPPLE_ENABLED, enabled ? 1 : 0);
            return true;
        } else if (preference.equals(mRippleColor)) {
            int color = ((Integer) newValue).intValue();
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.FLING_RIPPLE_COLOR, color);
            return true;
        } else if (preference.equals(mPulseColor)) {
            int color = ((Integer) newValue).intValue();
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.FLING_PULSE_COLOR, color);
            return true;
        } else if (preference.equals(mTrailsEnabled)) {
            boolean enabled = ((Boolean) newValue).booleanValue();
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.FLING_TRAILS_ENABLED, enabled ? 1 : 0);
            return true;
        } else if (preference.equals(mTrailsColor)) {
            int color = ((Integer) newValue).intValue();
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.FLING_TRAILS_COLOR, color);
            return true;
        } else if (preference.equals(mLavaLampEnabled)) {
            boolean enabled = ((Boolean) newValue).booleanValue();
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.FLING_PULSE_LAVALAMP_ENABLED, enabled ? 1 : 0);
            return true;
        }
        return false;
    }

    protected boolean usesExtendedActionsList() {
        return true;
    }

    protected void onActionPolicyEnforced(ArrayList<ActionPreference> prefs) {
        enforceAction(prefs, ActionHandler.SYSTEMUI_TASK_BACK);
        enforceAction(prefs, ActionHandler.SYSTEMUI_TASK_HOME);
    }

    /*
     * Iterate the list: if only one instance, disable it otherwise, enable
     */
    private void enforceAction(ArrayList<ActionPreference> prefs, String action) {
        ArrayList<ActionPreference> actionPrefs = new ArrayList<ActionPreference>();
        for (ActionPreference pref : prefs) {
            if (pref.getActionConfig().getAction().equals(action)) {
                actionPrefs.add(pref);
            }
        }
        boolean moreThanOne = actionPrefs.size() > 1;
        for (ActionPreference pref : actionPrefs) {
            pref.setEnabled(moreThanOne);
        }
    }

    @Override
    protected int getMetricsCategory() {
        // todo add a constant in MetricsLogger.java
        return MetricsLogger.MAIN_SETTINGS;
    }
}
