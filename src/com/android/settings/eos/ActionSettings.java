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

package com.android.settings.eos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.teameos.utils.ActionHandler;
import org.teameos.utils.ActionHandler.ActionBundle;
import org.teameos.utils.EosUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.ContactsContract.CommonDataKinds;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.cyanogenmod.PackageListAdapter;
import com.android.settings.cyanogenmod.PackageListAdapter.PackageItem;

public abstract class ActionSettings extends SettingsPreferenceFragment {
    private static final String TAG = ActionSettings.class.getSimpleName();
    private static final int DIALOG_ACTIONS = 0;
    private static final int DIALOG_PACKAGES = 1;
    private static final int DIALOG_CONTACT = 2;

    private static final String KEY_PREF_HOLDER = "key_pref_holder";
    private static final String KEY_URI_HOLDER = "key_uri_holder";

    protected ArrayList<ActionPreference> mPrefHolder = new ArrayList<ActionPreference>();
    private String mHolderKey;
    private PackageListAdapter mPackageAdapter;
    private PackageManager mPackageManager;

    private CharSequence[] mItem_entries;
    private CharSequence[] mItem_values;

    private enum ContactType {
        CALL,
        SMS,
        EMAIL
    };

    private ContactListAdapter mContactAdapter;
    private String mCurrentContact;
    private static final int REQUEST_CODE_CONTACT = 88;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        if (icicle != null) {
            mHolderKey = icicle.getString(KEY_PREF_HOLDER);
            mCurrentContact = icicle.getString(KEY_URI_HOLDER);
        }

        mPackageManager = getPackageManager();
        mPackageAdapter = new PackageListAdapter(getActivity());
        mContactAdapter = new ContactListAdapter(getActivity());
        setActionsList();
    }

    @Override
    public void onSaveInstanceState(Bundle icicle) {
        super.onSaveInstanceState(icicle);
        icicle.putString(KEY_PREF_HOLDER, mHolderKey);
        icicle.putString(KEY_URI_HOLDER, mCurrentContact);
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
                                } else if (pressed.equals(getString(R.string.action_value_quick_contact))) {
                                    Intent contactListIntent = new Intent(Intent.ACTION_PICK);
                                    contactListIntent.setType(CommonDataKinds.Phone.CONTENT_TYPE);
                                    startActivityForResult(contactListIntent, REQUEST_CODE_CONTACT);
                                } else {
                                    for (ActionPreference pref : mPrefHolder) {
                                        if (pref.getKey().equals(mHolderKey)) {
                                            if (pressed.equals(getString(R.string.action_value_default_action))) {
                                                pressed = pref.getDefaultAction();
                                            }
                                            ActionBundle b = new ActionBundle(getActivity(), pressed);
                                            pref.updateAction(b);
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
                        for (ActionPreference pref : mPrefHolder) {
                            if (pref.getKey().equals(mHolderKey)) {
                                String action = ActionHandler.APP_PREFIX + component;
                                ActionBundle b = new ActionBundle(getActivity(), action);
                                pref.updateAction(b);
                                onActionPolicyEnforced(mPrefHolder);
                                break;
                            }
                        }
                        dialog.cancel();
                    }
                });
                break;
            case DIALOG_CONTACT:
                final ListView contactListView = new ListView(getActivity());
                mContactAdapter.loadList(Uri.parse(mCurrentContact));
                contactListView.setAdapter(mContactAdapter);
                builder.setTitle(ActionHandler
                        .getContactName(getContentResolver(), Uri.parse(mCurrentContact)));
                builder.setIcon(ActionHandler.getIconFromContacts(getActivity(), Uri.parse(mCurrentContact)));
                builder.setView(contactListView);
                dialog = builder.create();
                contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent,
                            View view, int position, long id) {
                        for (ActionPreference pref : mPrefHolder) {
                            if (pref.getKey().equals(mHolderKey)) {
                                ContactHolder info = (ContactHolder) parent
                                        .getItemAtPosition(position);
                                StringBuilder b = new StringBuilder();
                                b.append(getActionTypeForContactType(info.type))
                                        .append(info.num.second)
                                        .append("|")
                                        .append(mCurrentContact);
                                String action = b.toString();
                                ActionBundle bundle = new ActionBundle(getActivity(), action);
                                pref.updateAction(bundle);
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

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_CODE_CONTACT) {
            if (resultCode == Activity.RESULT_OK && intent != null) {
                mCurrentContact = intent.getData().toString();
                showDialog(DIALOG_CONTACT);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    private String getActionTypeForContactType(ContactType type) {
        switch (type) {
            case CALL:
                return ActionHandler.CALL_PREFIX;
            case EMAIL:
                return ActionHandler.EMAIL_PREFIX;
            case SMS:
                return ActionHandler.TEXT_PREFIX;
            default:
                return ActionHandler.CALL_PREFIX;
        }
    }

    private void setActionsList() {     
        // load non-action dialog entries first from xml
        String[] entries_start = getResources()
                .getStringArray(R.array.action_dialog_entries);
        String[] values_start = getResources().getStringArray(
                R.array.action_dialog_values);

        ArrayList<String> temp_entries = new ArrayList<String>();
        ArrayList<String> temp_values = new ArrayList<String>();

        for (int i = 0; i < entries_start.length; i++) {
            temp_entries.add(entries_start[i]);
            temp_values.add(values_start[i]);
        }

        // append actions to dialog
        ArrayList<ActionBundle> actions = ActionHandler.getAllActions(getActivity());
        for (ActionBundle b : actions) {
            temp_entries.add(b.label);
            temp_values.add(b.action);
        }

        // filter actions based on environment
        if (!usesExtendedActionsList()) {
            int i = temp_values.indexOf(ActionHandler.SYSTEMUI_TASK_HOME);
            temp_entries.remove(i);
            temp_values.remove(i);

            i = temp_values.indexOf(ActionHandler.SYSTEMUI_TASK_BACK);
            temp_entries.remove(i);
            temp_values.remove(i);
        }

        if (!EosUtils.deviceSupportsMobileData(getActivity())) {
            int i = temp_values.indexOf(ActionHandler.SYSTEMUI_TASK_WIFIAP);
            temp_entries.remove(i);
            temp_values.remove(i);
        }
/*
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
            int i = temp_values.indexOf(getString(R.string.action_value_quick_contact));
            temp_entries.remove(i);
            temp_values.remove(i);
        }
*/
        if (!EosUtils.deviceSupportsBluetooth()) {
            int i = temp_values.indexOf(ActionHandler.SYSTEMUI_TASK_BT);
            temp_entries.remove(i);
            temp_values.remove(i);
        }

        if (!EosUtils.deviceSupportsTorch(getActivity())) {
            int i = temp_values.indexOf(ActionHandler.SYSTEMUI_TASK_TORCH);
            temp_entries.remove(i);
            temp_values.remove(i);
        }

        // only use for FFC only, i.e. Grouper
        // all other devices set action from packages
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            int i = temp_values.indexOf(ActionHandler.SYSTEMUI_TASK_CAMERA);
            temp_entries.remove(i);
            temp_values.remove(i);
        }

        // populate global dialog arrays
        mItem_entries = new CharSequence[temp_entries.size()];
        mItem_values = new CharSequence[temp_values.size()];

        int i = 0;
        for (String s : temp_entries) {
            mItem_entries[i] = s;
            i++;
        }
        i = 0;
        for (String s : temp_values) {
            mItem_values[i] = s;
            i++;
        }
    }

    private class ContactListAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mInflater;
        private List<ContactHolder> mContacts = new LinkedList<ContactHolder>();

        public ContactListAdapter(Context ctx) {
            mContext = ctx;
            mInflater = LayoutInflater.from(ctx);
        }

        void loadList(Uri contactUri) {
            mContacts.clear();
            ArrayList<Pair<String, String>> numbers = ActionHandler.getAllContactNumbers(mContext,
                    contactUri);
            // ArrayList<Pair<String, String>> emails = ActionHandler.getAllContactEmails(mContext,
            // contactUri);

            // calls
            for (Pair<String, String> number : numbers) {
                // often, phone number types are disorganized, but it's likely
                // we don't want fax numbers here or in sms
                if (number.first.contains("Fax") || number.first.contains("fax")) {
                    continue;
                }
                ContactHolder holder = new ContactHolder();
                holder.num = number;
                holder.type = ContactType.CALL;
                mContacts.add(holder);
            }
            // sms
            for (Pair<String, String> number : numbers) {
                if (number.first.contains("Fax") || number.first.contains("fax")) {
                    continue;
                }
                ContactHolder holder = new ContactHolder();
                holder.num = number;
                holder.type = ContactType.SMS;
                mContacts.add(holder);
            }

             // always load email 
             // disable for now 
             // for (Pair<String, String> email : emails) { 
             //   ContactHolder holder = new ContactHolder();
             //   holder.num = email;
             //   holder.type = ContactType.EMAIL;
             //   mContacts.add(holder);
             // }

            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mContacts.size();
        }

        @Override
        public ContactHolder getItem(int position) {
            return mContacts.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView != null) {
                holder = (ViewHolder) convertView.getTag();
            } else {
                convertView = mInflater.inflate(R.layout.contact_types_list, null, false);
                holder = new ViewHolder();
                convertView.setTag(holder);
                holder.title = (TextView) convertView.findViewById(R.id.name);
                holder.summary = (TextView) convertView.findViewById(R.id.description);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            }
            ContactHolder contactHolder = getItem(position);
            holder.title.setText(contactHolder.num.second);
            holder.summary.setText(contactHolder.num.first);
            holder.icon.setImageDrawable(mContext.getResources().getDrawable(
                    getIconForType(contactHolder.type)));
            return convertView;
        }

        private int getIconForType(ContactType type) {
            switch (type) {
                case CALL:
                    return com.android.internal.R.drawable.sym_action_call;
                case EMAIL:
                    return com.android.internal.R.drawable.sym_action_email;
                case SMS:
                    return com.android.internal.R.drawable.sym_action_chat;
                default:
                    return com.android.internal.R.drawable.sym_action_call;
            }
        }
    }

    private static class ViewHolder {
        TextView title;
        TextView summary;
        ImageView icon;
    }

    private static class ContactHolder {
        Pair<String, String> num;
        ContactType type;
    }
}
