package com.android.settings.cfx;

import java.util.ArrayList;

import com.android.settings.R;

import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class NxSettings extends ActionSettings implements
		Preference.OnPreferenceChangeListener {
	private static final String TAG = NxSettings.class.getSimpleName();
	private static final String NX_ENABLE_KEY = "eos_nx_enable";
	private static final String NX_ENABLE_URI = "eos_nx_enabled";

	Context mContext;

	CheckBoxPreference mNxEnable;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.nx_settings);

		mContext = (Context) getActivity();

		mNxEnable = (CheckBoxPreference) findPreference(NX_ENABLE_KEY);
		mNxEnable.setChecked(Settings.System.getBoolean(getContentResolver(),
				NX_ENABLE_URI, false));
		mNxEnable.setOnPreferenceChangeListener(this);

		onPreferenceScreenLoaded();

	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.equals(mNxEnable)) {
			Settings.System.putBoolean(getContentResolver(), NX_ENABLE_URI,
					((Boolean) newValue).booleanValue());
			return true;
		}
		return false;
	}

	protected boolean usesExtendedActionsList() {
		return true;
	}	

	protected boolean onActionPolicyEnforced(ArrayList<ActionPreference> prefs,
			ActionPreference targetPref) {
		Log.i(TAG, "onActionPolicyEnforced " + targetPref.getAction());
		// if empty, it can be assigned
		if (targetPref.checkEmptyAction()) {
			Log.i(TAG, "onActionPolicyEnforced " + "empty action, safe to assign");
			return false;
		}

		// if current action is not enforced, we're fine
		if (!targetPref.getAction().equals("task_back")
				&& !targetPref.getAction().equals("task_home")) {
			return false;
		}

		boolean shouldEnforce = true;

		// enforce Back
		if (targetPref.getAction().equals("task_back")) {
			Log.i(TAG, "onActionPolicyEnforced " + "Enforcing Back action policy");
			String currentKey = targetPref.getKey();
			// check the other ActionPreferences for a current Back assignment
			for (ActionPreference pref : prefs) {
				if (!pref.getKey().equals(currentKey)) {
					if (pref.getAction().equals("task_back")) {
						shouldEnforce = false;
						break;
					}
				}
			}
			return shouldEnforce;
		}

		// enforce Home
		if (targetPref.getAction().equals("task_home")) {
			Log.i(TAG, "onActionPolicyEnforced " + "Enforcing Home action policy");
			String currentKey = targetPref.getKey();
			// check the other ActionPreferences for a current Back assignment
			for (ActionPreference pref : prefs) {
				if (!pref.getKey().equals(currentKey)) {
					if (pref.getAction().equals("task_home")) {
						shouldEnforce = false;
						break;
					}
				}
			}
			return shouldEnforce;
		}

		return shouldEnforce;
	}

	protected void onNotifyPolicyViolation(String action) {
		String label = "";
		if (action.equals("task_back")) {
			label = "Back";
		} else {
			label = "Home";
		}
		StringBuilder b = new StringBuilder().append(label).append(" ")
				.append("must be assigned at all times");
		Toast.makeText(getActivity(), b.toString(), Toast.LENGTH_SHORT).show();
	}

}
