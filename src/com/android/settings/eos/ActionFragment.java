/*
 * Copyright (C) 2015 TeamEos project
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
 *
 * Handle assignable action dialogs and instances of the ActionPreference
 * class that holds target widget state
 */

package com.android.settings.eos;

import java.util.ArrayList;

import com.android.internal.util.actions.ActionHandler;
import com.android.internal.util.actions.ActionHandler.ActionBundle;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.cyanogenmod.ShortcutPickHelper;

public class ActionFragment extends SettingsPreferenceFragment implements
        ShortcutPickHelper.OnPickListener {

    private ShortcutPickHelper mPicker;
    protected ArrayList<ActionPreference> mPrefHolder;
    private String mHolderKey;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mPicker = new ShortcutPickHelper(getActivity(), this);
        mPrefHolder = new ArrayList<ActionPreference>();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPicker.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void shortcutPicked(String uri, String friendlyName, boolean isApplication) {
        // activity dialogs pass null here if they are dismissed
        // if null, do nothing, no harm
        if (uri == null) {
            return;
        }
        findAndUpdatePreference(new ActionBundle(getActivity(), uri));
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference instanceof ActionPreference) {
            mHolderKey = preference.getKey();
            createAndShowCategoryDialog();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onStart() {
        super.onStart();
        for (ActionPreference pref : mPrefHolder) {
            pref.load();
        }
        onActionPolicyEnforced(mPrefHolder);
    }

    // subclass overrides to include back and home actions
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
                    }
                }
            } else if (pref instanceof ActionPreference) {
                mPrefHolder.add((ActionPreference) pref);
            }
        }
    }

    private void onTargetChange(String uri) {
        if (uri == null) {
            return;
        } else if (uri.equals(getString(R.string.action_value_default_action))) {
            findAndUpdatePreference(null);
        } else if (uri.equals(getString(R.string.action_value_select_app))) {
            mPicker.pickShortcut(null, null, getId());
        } else if (uri.equals(getString(R.string.action_value_custom_action))) {
            createAndShowSystemActionDialog();
        }
    }

    private void findAndUpdatePreference(ActionBundle bundle) {
        for (ActionPreference pref : mPrefHolder) {
            if (pref.getKey().equals(mHolderKey)) {
                pref.updateAction(bundle == null ? new ActionBundle(getActivity(), pref
                        .getDefaultAction()) : bundle);
                onActionPolicyEnforced(mPrefHolder);
                break;
            }
        }
    }

    private void createAndShowCategoryDialog() {
        final DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                onTargetChange(getResources().getStringArray(R.array.action_dialog_values)[item]);
                dialog.dismiss();
            }
        };

        final DialogInterface.OnCancelListener cancel = new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                onTargetChange(null);
            }
        };

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.lockscreen_choose_action_title)
                .setItems(getResources().getStringArray(R.array.action_dialog_entries), l)
                .setOnCancelListener(cancel)
                .create();
        dialog.show();
    }

    private void createAndShowSystemActionDialog() {
        final CustomActionListAdapter adapter = new CustomActionListAdapter(getActivity());
        if (!usesExtendedActionsList()) {
            adapter.removeAction(ActionHandler.SYSTEMUI_TASK_HOME);
            adapter.removeAction(ActionHandler.SYSTEMUI_TASK_BACK);
        }
        final DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                findAndUpdatePreference(adapter.getItem(item));
                dialog.dismiss();
            }
        };

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.action_entry_custom_action))
                .setAdapter(adapter, l)
                .create();
        dialog.show();
    }
}
