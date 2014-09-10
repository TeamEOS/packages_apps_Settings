
package com.android.settings.cfx;

import org.codefirex.utils.CFXUtils;
import org.cyanogenmod.hardware.KeyDisabler;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.widget.Editor;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.cyanogenmod.ButtonBacklightBrightness;

public class KeySettings extends ActionSettings implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = KeySettings.class.getSimpleName();

    private static final String KEY_BUTTON_BACKLIGHT = "button_backlight";
    private static final String KEY_SWAP_VOLUME_BUTTONS = "swap_volume_buttons";
    private static final String KEY_VOLUME_KEY_CURSOR_CONTROL = "volume_key_cursor_control";
    private static final String NAVBAR_FORCE = "interface_force_navbar";

    private static final String CATEGORY_BACK = "back_key";
    private static final String CATEGORY_HOME = "home_key";
    private static final String CATEGORY_MENU = "menu_key";
    private static final String CATEGORY_ASSIST = "assist_key";
    private static final String CATEGORY_APPSWITCH = "app_switch_key";
    private static final String CATEGORY_VOLUME = "volume_keys";

    // Masks for checking presence of hardware keys.
    // Must match values in frameworks/base/core/res/res/values/config.xml
    public static final int KEY_MASK_HOME = 0x01;
    public static final int KEY_MASK_BACK = 0x02;
    public static final int KEY_MASK_MENU = 0x04;
    public static final int KEY_MASK_ASSIST = 0x08;
    public static final int KEY_MASK_APP_SWITCH = 0x10;

    private ListPreference mVolumeKeyCursorControl;
    private CheckBoxPreference mSwapVolumeButtons;
    private CheckBoxPreference mNavForce;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.key_settings);

        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        final boolean hasHardKeys = CFXUtils.isCapKeyDevice(getActivity());

        mNavForce = (CheckBoxPreference) prefScreen.findPreference(NAVBAR_FORCE);
        if (hasHardKeys) {
            boolean navBarEnabled = isForcedNavbar();
            mNavForce.setChecked(navBarEnabled);
            mNavForce.setSummary(getForcedNavbarSummary(navBarEnabled));
            mNavForce.setOnPreferenceChangeListener(this);
        } else {
            PreferenceCategory navCat = (PreferenceCategory) prefScreen
                    .findPreference("interface_navigation");
            prefScreen.removePreference(navCat);
            mNavForce = null;
        }

        final int deviceKeys = getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);
        final boolean hasHomeKey = (deviceKeys & KEY_MASK_HOME) != 0;
        final boolean hasMenuKey = (deviceKeys & KEY_MASK_MENU) != 0;
        final boolean hasAssistKey = (deviceKeys & KEY_MASK_ASSIST) != 0;
        final boolean hasAppSwitchKey = (deviceKeys & KEY_MASK_APP_SWITCH) != 0;

        final PreferenceCategory backCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_BACK);
        final PreferenceCategory homeCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_HOME);
        final PreferenceCategory menuCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_MENU);
        final PreferenceCategory assistCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_ASSIST);
        final PreferenceCategory appSwitchCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_APPSWITCH);

        if (!hasHardKeys)
            prefScreen.removePreference(backCategory);
        if (!hasHomeKey)
            prefScreen.removePreference(homeCategory);
        if (!hasMenuKey)
            prefScreen.removePreference(menuCategory);
        if (!hasAppSwitchKey)
            prefScreen.removePreference(appSwitchCategory);
        if (!hasAssistKey)
            prefScreen.removePreference(assistCategory);

        final PreferenceCategory volumeCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_VOLUME);

        if (Utils.hasVolumeRocker(getActivity())) {
            int swapVolumeKeys = Settings.System.getInt(getContentResolver(),
                    Settings.System.SWAP_VOLUME_KEYS_ON_ROTATION, 0);
            mSwapVolumeButtons = (CheckBoxPreference)
                    prefScreen.findPreference(KEY_SWAP_VOLUME_BUTTONS);
            mSwapVolumeButtons.setChecked(swapVolumeKeys > 0);
            int cursorControlAction = Settings.System.getInt(resolver,
                    Settings.System.VOLUME_KEY_CURSOR_CONTROL, 0);
            mVolumeKeyCursorControl = initActionList(KEY_VOLUME_KEY_CURSOR_CONTROL,
                    cursorControlAction);
        } else {
            prefScreen.removePreference(volumeCategory);
        }

        final ButtonBacklightBrightness backlight =
                (ButtonBacklightBrightness) findPreference(KEY_BUTTON_BACKLIGHT);
        if (!backlight.isButtonSupported() && !backlight.isKeyboardSupported()) {
            prefScreen.removePreference(backlight);
        }

        if (hasHardKeys)
            updateDisableNavkeysOption();
        onPreferenceScreenLoaded();
    }

    private boolean isForcedNavbar() {
        return Settings.System.getIntForUser(getContentResolver(),
                Settings.System.DEV_FORCE_SHOW_NAVBAR, 0,
                UserHandle.USER_CURRENT) != 0;
    }

    private String getForcedNavbarSummary(boolean barShowing) {
        final Resources res = getResources();
        if (!isKeyDisablerSupported()) {
            return res.getString(R.string.eos_key_disabler_unsupported);
        } else {
            return res.getString(barShowing ? R.string.eos_key_disabler_active
                    : R.string.eos_key_disabler_off);
        }
    }

    private static void writeDisableNavkeysOption(Context context, boolean enabled) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final int defaultBrightness = context.getResources().getInteger(
                com.android.internal.R.integer.config_buttonBrightnessSettingDefault);

        KeyDisabler.setActive(enabled);

        /* Save/restore button timeouts to disable them in softkey mode */
        SharedPreferences.Editor editor = prefs.edit();

        if (enabled) {
            int currentBrightness = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.BUTTON_BRIGHTNESS, defaultBrightness);
            if (!prefs.contains("pre_navbar_button_backlight")) {
                editor.putInt("pre_navbar_button_backlight", currentBrightness);
            }
            Settings.System.putInt(context.getContentResolver(),
                    Settings.System.BUTTON_BRIGHTNESS, 0);
        } else {
            int oldBright = prefs.getInt("pre_navbar_button_backlight", -1);
            if (oldBright != -1) {
                Settings.System.putInt(context.getContentResolver(),
                        Settings.System.BUTTON_BRIGHTNESS, oldBright);
                editor.remove("pre_navbar_button_backlight");
            }
        }
        editor.commit();
    }

    private void updateDisableNavkeysOption() {
        boolean enabled = isForcedNavbar();

        final PreferenceScreen prefScreen = getPreferenceScreen();

        /* Disable hw-key options if they're disabled */
        final PreferenceCategory homeCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_HOME);
        final PreferenceCategory menuCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_MENU);
        final PreferenceCategory assistCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_ASSIST);
        final PreferenceCategory appSwitchCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_APPSWITCH);
        final ButtonBacklightBrightness backlight =
                (ButtonBacklightBrightness) prefScreen.findPreference(KEY_BUTTON_BACKLIGHT);

        /*
         * Toggle backlight control depending on navbar state, force it to off
         * if enabling
         */
        if (backlight != null) {
            backlight.setEnabled(!enabled);
        }

        /* Toggle hardkey control availability depending on navbar state */
        if (homeCategory != null) {
            homeCategory.setEnabled(!enabled);
        }
        if (menuCategory != null) {
            menuCategory.setEnabled(!enabled);
        }
        if (assistCategory != null) {
            assistCategory.setEnabled(!enabled);
        }
        if (appSwitchCategory != null) {
            appSwitchCategory.setEnabled(!enabled);
        }
    }

    public static void restoreKeyDisabler(Context context) {
        if (!isKeyDisablerSupported()) {
            return;
        }

        writeDisableNavkeysOption(context, Settings.System.getInt(context.getContentResolver(),
                Settings.System.DEV_FORCE_SHOW_NAVBAR, 0) != 0);
    }

    public static boolean isKeyDisablerSupported() {
        try {
            return KeyDisabler.isSupported();
        } catch (NoClassDefFoundError e) {
            // Hardware abstraction framework not installed
            return false;
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mSwapVolumeButtons) {
            int value = mSwapVolumeButtons.isChecked()
                    ? (Utils.isTablet(getActivity()) ? 2 : 1) : 0;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SWAP_VOLUME_KEYS_ON_ROTATION, value);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mVolumeKeyCursorControl) {
            handleActionListChange(mVolumeKeyCursorControl, newValue,
                    Settings.System.VOLUME_KEY_CURSOR_CONTROL);
            return true;
        } else if (preference.equals(mNavForce)) {
            boolean enabled = ((Boolean) newValue).booleanValue();
            Settings.System.putBoolean(getContentResolver(),
                    Settings.System.DEV_FORCE_SHOW_NAVBAR, enabled);
            updateDisableNavkeysOption();
            restoreKeyDisabler(getActivity());
            mNavForce.setSummary(getForcedNavbarSummary(enabled));
            return true;
        }
        return false;
    }

    private ListPreference initActionList(String key, int value) {
        ListPreference list = (ListPreference) getPreferenceScreen().findPreference(key);
        list.setValue(Integer.toString(value));
        list.setSummary(list.getEntry());
        list.setOnPreferenceChangeListener(this);
        return list;
    }

    private void handleActionListChange(ListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);

        pref.setSummary(pref.getEntries()[index]);
        Settings.System.putInt(getContentResolver(), setting, Integer.valueOf(value));
    }
}
