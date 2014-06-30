/*
 * Copyright (C) 2014 TeamEos project
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

import org.codefirex.utils.ActionHandler;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.internal.util.cm.QSUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.cyanogenmod.PackageListAdapter;
import com.android.settings.cyanogenmod.PackageListAdapter.PackageItem;

public abstract class ActionSettings extends SettingsPreferenceFragment {
    private static final String TAG = ActionSettings.class.getSimpleName();
    private static final int DIALOG_ACTIONS = 0;
    private static final int DIALOG_PACKAGES = 1;

    protected ArrayList<ActionPreference> mPrefHolder = new ArrayList<ActionPreference>();
    private String mHolderKey;
    private PackageListAdapter mPackageAdapter;
    private PackageManager mPackageManager;

    private CharSequence[] mItem_entries;
    private CharSequence[] mItem_values;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPackageManager = getPackageManager();
        mPackageAdapter = new PackageListAdapter(getActivity(), false);
        setActionsList();
    }

    @Override
    public void onStart() {
        super.onStart();
        for (ActionPreference pref : mPrefHolder) {
            pref.updateResources();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        onActionPolicyEnforced(mPrefHolder);
    }

    protected boolean usesExtendedActionsList() {
        return false;
    }

    protected void onActionPolicyEnforced(ArrayList<ActionPreference> prefs) {
    }

    // populate holder list once everything is added and removed
    protected void onPreferenceScreenLoaded() {
        final PreferenceScreen prefScreen = getPreferenceScreen();
        for (int i = 0; i < prefScreen.getPreferenceCount(); i++) {
            Preference pref = prefScreen.getPreference(i);
            if (pref instanceof PreferenceCategory) {
                PreferenceCategory cat = (PreferenceCategory) pref;
                for (int j = 0; j < cat.getPreferenceCount(); j++) {
                    Preference child = cat.getPreference(j);
                    if (child instanceof ActionPreference) {
                        mPrefHolder.add((ActionPreference) child);
                        Log.i(TAG, child.getKey());
                    }
                }
            } else if (pref instanceof ActionPreference) {
                mPrefHolder.add((ActionPreference) pref);
                Log.i(TAG, pref.getKey() + " added to button settings");
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference instanceof ActionPreference) {
            String key = preference.getKey();
            for (ActionPreference pref : mPrefHolder) {
                if (key.equals(pref.getKey())) {
                    mHolderKey = key;
                    showDialog(DIALOG_ACTIONS);
                    break;
                }
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final Dialog dialog;
        switch (id) {
            case DIALOG_ACTIONS:
                final CharSequence[] item_entries = mItem_entries;
                final CharSequence[] item_values = mItem_values;
                builder.setTitle("Set action")
                        .setNegativeButton(
                                getResources().getString(
                                        com.android.internal.R.string.cancel),
                                new Dialog.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                            int which) {
                                        dialog.dismiss();
                                    }
                                })
                        .setItems(item_entries, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String pressed = (String) item_values[which];
                                if (pressed.equals("app")) {
                                    showDialog(DIALOG_PACKAGES);
                                } else {
                                    for (ActionPreference pref : mPrefHolder) {
                                        if (pref.getKey().equals(mHolderKey)) {
                                            pref.updateAction(pressed);
                                            onActionPolicyEnforced(mPrefHolder);
                                            break;
                                        }
                                    }
                                }
                                dialog.cancel();
                            }
                        });
                dialog = builder.create();
                break;
            case DIALOG_PACKAGES:
                final ListView list = new ListView(getActivity());
                list.setAdapter(mPackageAdapter);
                builder.setTitle(R.string.profile_choose_app);
                builder.setView(list);
                dialog = builder.create();
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent,
                            View view, int position, long id) {
                        PackageItem info = (PackageItem) parent.getItemAtPosition(position);
                        String component = mPackageManager
                                .getLaunchIntentForPackage(info.packageName).getComponent()
                                .flattenToString();
                        String label = info.title.toString();
                        for (ActionPreference pref : mPrefHolder) {
                            if (pref.getKey().equals(mHolderKey)) {
                                pref.updateAction(component, label);
                                onActionPolicyEnforced(mPrefHolder);
                                break;
                            }
                        }
                        dialog.cancel();
                    }
                });
                break;
            default:
                return null;
        }
        return dialog;
    }

    private void setActionsList() {
        List<String> temp_entries = Arrays.asList(getResources()
                .getStringArray(R.array.action_dialog_entries));
        List<String> temp_values = Arrays.asList(getResources().getStringArray(
                R.array.action_dialog_values));

        ArrayList<String> item_entries = new ArrayList<String>();
        ArrayList<String> item_values = new ArrayList<String>();

        for (String s : temp_entries) {
            item_entries.add(s);
        }

        for (String s : temp_values) {
            item_values.add(s);
        }

        if (!usesExtendedActionsList()) {
            int i = item_values.indexOf("task_home");
            item_entries.remove(i);
            item_values.remove(i);

            i = item_values.indexOf("task_back");
            item_entries.remove(i);
            item_values.remove(i);
        }

        if (!QSUtils.deviceSupportsMobileData(getActivity())) {
            int i = item_values.indexOf(ActionHandler.SYSTEMUI_TASK_WIFIAP);
            item_entries.remove(i);
            item_values.remove(i);
        }

        if (!QSUtils.deviceSupportsBluetooth()) {
            int i = item_values.indexOf(ActionHandler.SYSTEMUI_TASK_BT);
            item_entries.remove(i);
            item_values.remove(i);
        }

        if (!QSUtils.deviceSupportsTorch(getActivity())) {
            int i = item_values.indexOf(ActionHandler.SYSTEMUI_TASK_TORCH);
            item_entries.remove(i);
            item_values.remove(i);
        }

        // only use for FFC only, i.e. Grouper
        // all other devices set action from packages
        if (hasRearCam()) {
            int i = item_values.indexOf(ActionHandler.SYSTEMUI_TASK_CAMERA);
            item_entries.remove(i);
            item_values.remove(i);
        }

        mItem_entries = new CharSequence[item_entries.size()];
        mItem_values = new CharSequence[item_values.size()];

        int i = 0;
        for (String s : item_entries) {
            mItem_entries[i] = s;
            i++;
        }
        i = 0;
        for (String s : item_values) {
            mItem_values[i] = s;
            i++;
        }
    }

    private boolean hasRearCam() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }
}
