package com.android.settings.cfx;

import java.util.ArrayList;

import com.android.settings.R;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.provider.Settings;

public class NxSettings extends ActionSettings implements
		Preference.OnPreferenceChangeListener {
	private static final String TAG = NxSettings.class.getSimpleName();
	private static final String NX_ENABLE_KEY = "eos_nx_enable";
	private static final String NX_TRAILS_ENABLE_KEY = "eos_nx_trails_enable";
	private static final String NX_ENABLE_URI = "eos_nx_enabled";

	Context mContext;

	CheckBoxPreference mNxEnable;
	CheckBoxPreference mNxTrailsEnable;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.nx_settings);

		mContext = (Context) getActivity();

		mNxEnable = (CheckBoxPreference) findPreference(NX_ENABLE_KEY);
		mNxEnable.setChecked(Settings.System.getBoolean(getContentResolver(),
				NX_ENABLE_URI, false));
		mNxEnable.setOnPreferenceChangeListener(this);

		mNxTrailsEnable = (CheckBoxPreference) findPreference(NX_TRAILS_ENABLE_KEY);
		mNxTrailsEnable.setChecked(Settings.System.getBoolean(getContentResolver(),
				"eos_nx_trails_enabled", true));
		mNxTrailsEnable.setOnPreferenceChangeListener(this);

		PreferenceCategory advanced = (PreferenceCategory) findPreference("eos_long_swipe_category");
		advanced.removePreference(mNxTrailsEnable);  // disable trails for now

		onPreferenceScreenLoaded();

	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.equals(mNxEnable)) {
			boolean enabled = ((Boolean) newValue).booleanValue();
			Settings.System.putBoolean(getContentResolver(), NX_ENABLE_URI,
					enabled);
			if (enabled) {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						onActionPolicyEnforced(mPrefHolder);
					}
				}, 100);
			}
			return true;
		} else if (preference.equals(mNxTrailsEnable)) {
			boolean enabled = ((Boolean) newValue).booleanValue();
			Settings.System.putBoolean(getContentResolver(),
					"eos_nx_trails_enabled", enabled);
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
