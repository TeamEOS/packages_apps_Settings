
package com.android.settings.cfx;

import java.util.ArrayList;

import com.android.settings.R;
import com.android.settings.Utils;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

public class NxSettings extends ActionSettings implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = NxSettings.class.getSimpleName();
    private static final String NX_TRAILS_ENABLE_KEY = "eos_nx_trails_enable";
    private static final String NX_ENABLE_URI = "eos_nx_enabled";

    Context mContext;

    private Switch mActionBarSwitch;
    private NxEnabler mNxEnabler;
    private ViewGroup mPrefsContainer;
    private View mDisabledText;

    CheckBoxPreference mNxTrailsEnable;
    CheckBoxPreference mShowLogo;
    CheckBoxPreference mAnimateLogo;

    private ContentObserver mSettingsObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            updateEnabledState();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.nx_settings);

        mContext = (Context) getActivity();

        mNxTrailsEnable = (CheckBoxPreference) findPreference(NX_TRAILS_ENABLE_KEY);
        mNxTrailsEnable.setChecked(Settings.System.getBoolean(getContentResolver(),
                "eos_nx_trails_enabled", true));
        mNxTrailsEnable.setOnPreferenceChangeListener(this);

        mShowLogo = (CheckBoxPreference) findPreference("eos_nx_show_logo");
        mShowLogo.setChecked(Settings.System.getBoolean(getContentResolver(),
                "nx_logo_visible", true));
        mShowLogo.setOnPreferenceChangeListener(this);

        mAnimateLogo = (CheckBoxPreference) findPreference("eos_nx_animate_logo");
        mAnimateLogo.setChecked(Settings.System.getBoolean(getContentResolver(),
                "nx_logo_animates", false));
        mAnimateLogo.setOnPreferenceChangeListener(this);

        PreferenceCategory appearance = (PreferenceCategory) findPreference("eos_nx_appearance");
        appearance.removePreference(mNxTrailsEnable); // disable trails for now

        onPreferenceScreenLoaded();
    }

    @Override
    public void onActivityCreated(Bundle icicle) {
        Activity activity = getActivity();
        mActionBarSwitch = new Switch(activity);

        if (activity instanceof PreferenceActivity) {
            PreferenceActivity preferenceActivity = (PreferenceActivity) activity;
            if (preferenceActivity.onIsHidingHeaders() || preferenceActivity.onIsMultiPane()) {
                final int padding = activity.getResources().getDimensionPixelSize(
                        R.dimen.action_bar_switch_padding);
                mActionBarSwitch.setPaddingRelative(0, 0, padding, 0);
                activity.getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                        ActionBar.DISPLAY_SHOW_CUSTOM);
                activity.getActionBar().setCustomView(mActionBarSwitch, new ActionBar.LayoutParams(
                        ActionBar.LayoutParams.WRAP_CONTENT,
                        ActionBar.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER_VERTICAL | Gravity.END));
            }
        }

        mNxEnabler = new NxEnabler(activity, mActionBarSwitch);
        super.onActivityCreated(icicle);
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroyView() {
        getActivity().getActionBar().setCustomView(null);
        super.onDestroyView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.nx_fragment, container, false);
        mPrefsContainer = (ViewGroup) v.findViewById(R.id.prefs_container);
        mDisabledText = v.findViewById(R.id.disabled_text);

        View prefs = super.onCreateView(inflater, mPrefsContainer, savedInstanceState);
        mPrefsContainer.addView(prefs);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mNxEnabler != null) {
            mNxEnabler.resume();
        }
        getActivity().invalidateOptionsMenu();
        getContentResolver().registerContentObserver(
                Settings.System.getUriFor(NX_ENABLE_URI),
                true, mSettingsObserver);
        updateEnabledState();

        // If running on a phone, remove padding around container
        // and the preference listview
        if (!Utils.isTablet(getActivity())) {
            mPrefsContainer.setPadding(0, 0, 0, 0);
            getListView().setPadding(0, 0, 0, 0);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mNxEnabler != null) {
            mNxEnabler.pause();
        }
        getContentResolver().unregisterContentObserver(mSettingsObserver);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.equals(mNxTrailsEnable)) {
            boolean enabled = ((Boolean) newValue).booleanValue();
            Settings.System.putBoolean(getContentResolver(),
                    "eos_nx_trails_enabled", enabled);
            return true;
        } else if (preference.equals(mShowLogo)) {
            boolean enabled = ((Boolean) newValue).booleanValue();
            Settings.System.putBoolean(getContentResolver(),
                    "nx_logo_visible", enabled);
            return true;
        } else if (preference.equals(mAnimateLogo)) {
            boolean enabled = ((Boolean) newValue).booleanValue();
            Settings.System.putBoolean(getContentResolver(),
                    "nx_logo_animates", enabled);
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

    private void updateEnabledState() {
        boolean enabled = Settings.System.getInt(getContentResolver(),
                NX_ENABLE_URI, 0) != 0;
        mPrefsContainer.setVisibility(enabled ? View.VISIBLE : View.GONE);
        mDisabledText.setVisibility(enabled ? View.GONE : View.VISIBLE);
        if (enabled) onActionPolicyEnforced(mPrefHolder);
    }
}
