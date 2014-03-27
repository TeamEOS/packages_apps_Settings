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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codefirex.utils.CFXConstants;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.provider.Settings;
import android.text.TextUtils;

import com.android.settings.R;
import com.android.settings.SubSettings;

public class NavigationSettings extends ActionSettings implements
		Preference.OnPreferenceChangeListener {
	private static final String NAVBAR_SIZE = "cfx_interface_navbar_size";
	private static final String CATEGORY_SOFTKEY_ACTIONS = "cfx_softkey_longpress_category";
	private static final String CATEGORY_SOFTKEY_COLOR = "cfx_softkey_key_color_category";
	private static final String CATEGORY_SOFTKEY_GLOW = "cfx_softkey_glow_color_category";
	private static final String EXTRA1 = "cfx_softkey_extra1";
	private static final String EXTRA2 = "cfx_softkey_extra2";
	private static final String SOFTKEY_COLOR_KEY = "cfx_softkey_key_color_multipref";
	private static final String SOFTKEY_COLOR_RESTORE_KEY = "cfx_softkey_key_color_restore_multipref";
	private static final String SOFTKEY_GLOW_COLOR_KEY = "cfx_softkey_glow_color_multipref";
	private static final String SOFTKEY_GLOW_COLOR_RESTORE_KEY = "cfx_softkey_glow_color_restore_multipref";

	private static final int COLOR_REQUEST_CODE = 6969;

	PreferenceCategory pc_ui;
	PreferenceCategory pc_sofkey_actions;
	PreferenceCategory pc_softkey_color;
	PreferenceCategory pc_softkey_glow;

	ContentResolver mResolver;
	Context mContext;

	List<CharSequence> mColorUriHolder;

	private ListPreference mNavbarSize;

    private static final boolean DEBUG = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.navigation_settings);

		mContext = (Context) getActivity();
		mResolver = getActivity().getContentResolver();

		pc_sofkey_actions = (PreferenceCategory) findPreference(CATEGORY_SOFTKEY_ACTIONS);
		pc_softkey_color = (PreferenceCategory) findPreference(CATEGORY_SOFTKEY_COLOR);
		pc_softkey_glow = (PreferenceCategory) findPreference(CATEGORY_SOFTKEY_GLOW);

		mNavbarSize = (ListPreference) findPreference(NAVBAR_SIZE);
		int sizeVal = Settings.System.getInt(mResolver,
				CFXConstants.SYSTEMUI_NAVBAR_SIZE_DP,
				CFXConstants.SYSTEMUI_NAVBAR_SIZE_DEF_INDEX);
		mNavbarSize.setDefaultValue(String.valueOf(sizeVal));
		mNavbarSize.setValue(String.valueOf(sizeVal));
		mNavbarSize.setOnPreferenceChangeListener(this);
		updateSizeSummary();

		findPreference(SOFTKEY_COLOR_KEY).setOnPreferenceChangeListener(this);
		findPreference(SOFTKEY_COLOR_RESTORE_KEY)
				.setOnPreferenceChangeListener(this);
		findPreference(SOFTKEY_GLOW_COLOR_KEY).setOnPreferenceChangeListener(
				this);
		findPreference(SOFTKEY_GLOW_COLOR_RESTORE_KEY)
				.setOnPreferenceChangeListener(this);

		mColorUriHolder = new ArrayList<CharSequence>();

		PreferenceCategory mLongPressCat = (PreferenceCategory) findPreference(CATEGORY_SOFTKEY_ACTIONS);

		mLongPressCat.removePreference(findPreference(EXTRA1));
		mLongPressCat.removePreference(findPreference(EXTRA2));

		if(DEBUG) {
			getPreferenceScreen().removePreference(pc_softkey_color);
			getPreferenceScreen().removePreference(pc_softkey_glow);
		}

		onPreferenceScreenLoaded();
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		String aKey = preference.getKey();
		if (preference.equals(mNavbarSize)) {
			int val = Integer.parseInt(((String) newValue).toString());
			Settings.System.putInt(mResolver,
					CFXConstants.SYSTEMUI_NAVBAR_SIZE_DP, (val));
			Intent intent = new Intent().setAction(
					CFXConstants.ACTION_CFX_UI_CHANGE).putExtra(
					CFXConstants.INTENT_REASON_UI_CHANGE,
					CFXConstants.INTENT_REASON_UI_BAR_SIZE);
			mContext.sendBroadcastAsUser(intent, new UserHandle(
					UserHandle.USER_ALL));
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					updateSizeSummary();
				}
			}, 250);
			return true;
		} else if (aKey.equals(SOFTKEY_COLOR_KEY)
				|| aKey.equals(SOFTKEY_GLOW_COLOR_KEY)) {
			mColorUriHolder = Arrays.asList((CharSequence[]) newValue);
			((SubSettings) getActivity())
					.registerActivityResultReceiverFragment(NavigationSettings.this);
			Intent intent = new Intent().setClassName("org.codefirex.cfxtools",
					"org.codefirex.cfxtools.ColorPicker");
			getActivity().startActivityForResult(intent, COLOR_REQUEST_CODE);
			return true;
		} else if (aKey.equals(SOFTKEY_COLOR_RESTORE_KEY)
				|| aKey.equals(SOFTKEY_GLOW_COLOR_RESTORE_KEY)) {
			mColorUriHolder = Arrays.asList((CharSequence[]) newValue);
			for (CharSequence uri : mColorUriHolder) {
				Settings.System.putIntForUser(getContentResolver(),
						String.valueOf(uri), -1, UserHandle.USER_CURRENT);
			}
			return true;
		}
		return false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == COLOR_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				String rawColor = data.getStringExtra("result");
				if (rawColor != null && !TextUtils.isEmpty(rawColor)) {
					int color = Integer.parseInt(rawColor);
					for (CharSequence uri : mColorUriHolder) {
						Settings.System.putIntForUser(getContentResolver(),
								String.valueOf(uri), color,
								UserHandle.USER_CURRENT);
					}
				}
			}
		}
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
