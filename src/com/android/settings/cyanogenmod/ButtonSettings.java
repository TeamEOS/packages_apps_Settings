/*
 * Copyright (C) 2013 The CyanogenMod project
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

package com.android.settings.cyanogenmod;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class ButtonSettings extends SettingsPreferenceFragment implements
		Preference.OnPreferenceChangeListener {
	private static final String KEY_HOME_LONG_PRESS = "hardware_keys_home_long_press";
	private static final String KEY_HOME_DOUBLE_TAP = "hardware_keys_home_double_tap";
	private static final String KEY_MENU_PRESS = "hardware_keys_menu_press";
	private static final String KEY_MENU_LONG_PRESS = "hardware_keys_menu_long_press";
	private static final String KEY_ASSIST_PRESS = "hardware_keys_assist_press";
	private static final String KEY_ASSIST_LONG_PRESS = "hardware_keys_assist_long_press";
	private static final String KEY_APP_SWITCH_PRESS = "hardware_keys_app_switch_press";
	private static final String KEY_APP_SWITCH_LONG_PRESS = "hardware_keys_app_switch_long_press";
	private static final String KEY_BUTTON_BACKLIGHT = "button_backlight";
	private static final String KEY_SWAP_VOLUME_BUTTONS = "swap_volume_buttons";

	private static final String CATEGORY_HOME = "home_key";
	private static final String CATEGORY_MENU = "menu_key";
	private static final String CATEGORY_ASSIST = "assist_key";
	private static final String CATEGORY_APPSWITCH = "app_switch_key";
	private static final String CATEGORY_VOLUME = "volume_keys";
	private static final String CATEGORY_BACKLIGHT = "key_backlight";

	// Available custom actions to perform on a key press.
	// Must match values for KEY_HOME_LONG_PRESS_ACTION in:
	// frameworks/base/core/java/android/provider/Settings.java
	private static final int ACTION_NOTHING = 0;
	private static final int ACTION_MENU = 1;
	private static final int ACTION_APP_SWITCH = 2;
	private static final int ACTION_SEARCH = 3;
	private static final int ACTION_VOICE_SEARCH = 4;
	private static final int ACTION_IN_APP_SEARCH = 5;

	// Masks for checking presence of hardware keys.
	// Must match values in frameworks/base/core/res/res/values/config.xml
	public static final int KEY_MASK_HOME = 0x01;
	public static final int KEY_MASK_BACK = 0x02;
	public static final int KEY_MASK_MENU = 0x04;
	public static final int KEY_MASK_ASSIST = 0x08;
	public static final int KEY_MASK_APP_SWITCH = 0x10;

	private ListPreference mHomeLongPressAction;
	private ListPreference mHomeDoubleTapAction;
	private ListPreference mMenuPressAction;
	private ListPreference mMenuLongPressAction;
	private ListPreference mAssistPressAction;
	private ListPreference mAssistLongPressAction;
	private ListPreference mAppSwitchPressAction;
	private ListPreference mAppSwitchLongPressAction;
	private CheckBoxPreference mShowActionOverflow;
	private CheckBoxPreference mSwapVolumeButtons;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.button_settings);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// TODO Auto-generated method stub
		return false;
	}
}
