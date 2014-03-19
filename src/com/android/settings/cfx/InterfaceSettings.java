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

import android.content.ContentResolver;
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
	}

	@Override
	public void onStart() {
		super.onStart();
		if (mObserver != null) {
			mObserver.observe();
		}
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
		boolean isForced = isForcedNavbar();
		if (isForced) {
			if (mNavCat.findPreference(NAVBAR_SETTINGS) == null) {
				mNavCat.addPreference(mNavbarSettings);
			}
		} else {
			mNavCat.removePreference(mNavbarSettings);
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
		}
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.equals(mNavForce)) {
			Settings.System.putBoolean(getContentResolver(),
					Settings.System.DEV_FORCE_SHOW_NAVBAR,
					((Boolean) newValue).booleanValue());
			return true;
		}
		return false;
	}
}
