
package com.android.settings.cfx;

import java.util.HashMap;
import java.util.Map;

import org.codefirex.utils.CFXUtils;

import com.android.settings.R;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.preference.Preference;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;

public class ActionPreference extends Preference {
	private static final String SETTINGSNS = "http://schemas.android.com/apk/res/com.android.settings";
	private static final String ICON_PACKAGE = "com.android.systemui";
	private static final String ATTR_URI = "observedUri";
	private static final String ATTR_EXT_ICON = "externalIcon";
	private static final String ATTR_DEF_VAL = "defaultVal";
	private static final String ATTR_ARRAY_ENTRIES = "arrayEntries";
	private static final String ATTR_ARRAY_VALUES = "arrayValues";
    private static final String EMPTY = "empty";
    private static final String APP_PREFIX = "app:";

    private String mActionUri;
    private String mIconRes;
    private String mAction;
    private String mDefSummary;
    private String mDefValue;
    private Context mContext;

    private Map<String, String> mEntryMap;

    public ActionPreference(Context context) {
        this(context, null);
    }

    public ActionPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActionPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        mContext = context;
        mEntryMap = new HashMap<String, String>();

		int actionEntryRes = attrs.getAttributeResourceValue(SETTINGSNS,
				ATTR_ARRAY_ENTRIES, -1);
		String[] mapEntries = mContext.getResources().getStringArray(
				actionEntryRes == -1 ? R.array.action_dialog_entries
						: actionEntryRes);

		int actionValueRes = attrs.getAttributeResourceValue(SETTINGSNS,
				ATTR_ARRAY_VALUES, -1);
		String[] mapValues = mContext.getResources().getStringArray(
				actionValueRes == -1 ? R.array.action_dialog_values
						: actionValueRes);

        for (int i = 0; i < mapValues.length; i++) {
        	mEntryMap.put(mapValues[i], mapEntries[i]);
        }

        int lpRes = attrs.getAttributeResourceValue(SETTINGSNS, ATTR_URI, -1);
        mActionUri = lpRes != -1 ?  mContext.getResources().getString(lpRes) : EMPTY;

        int defVal = attrs.getAttributeResourceValue(SETTINGSNS, ATTR_DEF_VAL, -1);
        mDefValue = defVal != -1 ? mContext.getResources().getString(defVal) : EMPTY;

        int iconRes = attrs.getAttributeResourceValue(SETTINGSNS, ATTR_EXT_ICON, -1);
        if (iconRes != -1) mIconRes = mContext.getResources().getString(iconRes);

		if (!mDefValue.equals(EMPTY)) {
			mDefSummary = mEntryMap.get(mDefValue);
		} else {
			mDefSummary = String.valueOf(getSummary());
		}

    }

    public void updateResources() {
    	refreshIcon();
    	refreshSummary();
    }

    // refresh onStart() to catch a possible theme change
	private void refreshIcon() {
		if (mIconRes != null) {
			setPrefIcon(this, mContext, mIconRes);
		}
	}

    // SystemUI will catch a package removal and set default value
    // but we need to update our summary onStart() to catch it
    private void refreshSummary() {        
        mAction = Settings.System.getString(mContext.getContentResolver(), mActionUri);
        if (checkEmptyAction()) mAction = EMPTY;

		// set initial summary based on loaded values
		// we don't map package labels, only the component name
    	String newSummary = "";
    	if (mAction.startsWith(APP_PREFIX)) {
    		newSummary = CFXUtils.getLabelFromComponent(mContext.getPackageManager(), mAction);
            // maybe package got removed, who knows
            if (TextUtils.isEmpty(newSummary)) {
                newSummary = EMPTY;
            } else {
            	// append just so updateSummary doesn't think it's a custom action
            	StringBuilder b = new StringBuilder().append(APP_PREFIX).append(newSummary);
            	newSummary = b.toString();
            }
    	} else {
    		newSummary = mAction;
    	}
    	updateSummary(newSummary);
    }

    public boolean checkEmptyAction() {
    	return mAction == null || TextUtils.isEmpty(mAction);
    }

    public String getAction() {
    	return mAction;
    }

	// update a action and it's summary when selected from package chooser
	public void updateAction(String component, String label) {
		StringBuilder b = new StringBuilder().append(APP_PREFIX).append(component);
		mAction = b.toString();
		Settings.System.putStringForUser(mContext.getContentResolver(),
				mActionUri, mAction, UserHandle.USER_CURRENT);
		b = new StringBuilder().append(APP_PREFIX).append(label);
		updateSummary(b.toString());
	}

    // update a action and it's summary when a non-package action is chosen
    public void updateAction(String customAction) {
    	// if action is "empty" we write a empty string to uri, not a string valued "empty"
    	String temp = "";
        mAction = customAction;
        if (!EMPTY.equals(mAction)) {
        	temp = mAction;
        }
        Settings.System.putStringForUser(mContext.getContentResolver(), mActionUri, temp,
                UserHandle.USER_CURRENT);
        updateSummary(mAction);
    }

    private void updateSummary(String action) {
    	String newSummary = "";
        if (action.startsWith(APP_PREFIX)) {
            newSummary = action.substring(4);
        } else {
        	if (action.equals(EMPTY)) {
        	    if (mDefValue.equals(EMPTY)) {
                    newSummary = mDefSummary;
        	    } else {
        		    newSummary = mEntryMap.get(mDefValue);
        	    }
        	} else {
        		newSummary = mEntryMap.get(action);
        	}
        }
        setSummary(newSummary);
    }

    private static void setPrefIcon(Preference pref, Context context, String icon_name) {
        try {
            Resources res = context.getPackageManager().getResourcesForApplication(ICON_PACKAGE);
            Drawable icon = res.getDrawable(res.getIdentifier(icon_name, "drawable", ICON_PACKAGE));
            if (icon != null)
                pref.setIcon(icon);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
