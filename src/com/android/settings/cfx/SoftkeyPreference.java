
package com.android.settings.cfx;

import org.codefirex.utils.CFXConstants;
import org.codefirex.utils.CFXUtils;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.preference.Preference;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.android.settings.R;

public class SoftkeyPreference extends Preference {
    static final String SETTINGSNS = "http://schemas.android.com/apk/res/com.android.settings";
    static final String ICON_PACKAGE = "com.android.systemui";
    static final String ATTR_URI = "observedUri";
    static final String ATTR_EXT_ICON = "externalIcon";
    private static final String NO_LP = "empty";

    private String mLpUri;
    private String iconRes;
    private String mLpAction;
    private Context mContext;

    public SoftkeyPreference(Context context) {
        this(context, null);
    }

    public SoftkeyPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SoftkeyPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        mContext = context;
        mLpUri = mContext.getResources().getString(
                attrs.getAttributeResourceValue(SETTINGSNS, ATTR_URI, -1));
        iconRes = mContext.getResources().getString(
                attrs.getAttributeResourceValue(SETTINGSNS, ATTR_EXT_ICON, -1));
        if (!TextUtils.isEmpty(mLpUri)) {
            updateLpActionFromUri();
        } else {
            mLpUri = NO_LP;
            mLpAction = NO_LP;
        }
    }

    public void updateSummary() {
        updateSummaryInitialState();
    }

    public void refreshIcon() {
        setPrefIcon(this, mContext, iconRes);
    }

    private void updateLpActionFromUri() {
        mLpAction = Settings.System.getString(mContext.getContentResolver(), mLpUri);
        if (mLpAction == null || TextUtils.isEmpty(mLpAction))
            mLpAction = NO_LP;
    }

    public void updateLpActionFromSelection(String component, String label) {
        StringBuilder b = new StringBuilder()
                .append("app:")
                .append(component);
        mLpAction = b.toString();
        Settings.System.putStringForUser(mContext.getContentResolver(), mLpUri, mLpAction,
                UserHandle.USER_CURRENT);
        updateLpPackageSummary(label);
    }

    public void updateLpCustomAction(String customAction) {
        mLpAction = customAction;
        Settings.System.putStringForUser(mContext.getContentResolver(), mLpUri, mLpAction,
                UserHandle.USER_CURRENT);
        updateLpCustomSummary(mLpAction);
    }

    private void updateLpCustomSummary(String customAction) {
        String someAction;
        if (customAction.equals(NO_LP)) {
            setSummary(mContext.getResources().getString(R.string.cfx_softkey_longpress_summary));
            return;
        } else if (customAction.equals(CFXConstants.SYSTEMUI_TASK_SCREENSHOT)) {
            someAction = "Take screenshot";
        } else if (customAction.equals(CFXConstants.SYSTEMUI_TASK_KILL_PROCESS)) {
            someAction = "Kill process";
        } else if (customAction.equals(CFXConstants.SYSTEMUI_TASK_SCREENOFF)) {
            someAction = "Screen off";
        } else if (customAction.equals(CFXConstants.SYSTEMUI_TASK_ASSIST)) {
            someAction = "Search assist";
        } else if (customAction.equals(CFXConstants.SYSTEMUI_TASK_TORCH)) {
            someAction = "Torch";
        } else if (customAction.equals(CFXConstants.SYSTEMUI_TASK_CAMERA)) {
            someAction = "Camera";
        } else if (customAction.equals(CFXConstants.SYSTEMUI_TASK_WIFI)) {
            someAction = "Toggle Wifi";
        } else if (customAction.equals(CFXConstants.SYSTEMUI_TASK_WIFIAP)) {
            someAction = "Toggle WifiAP";
        } else if (customAction.equals(CFXConstants.SYSTEMUI_TASK_BT)) {
            someAction = "Toggle Bluetooth";
        } else if (customAction.equals(CFXConstants.SYSTEMUI_TASK_POWER_MENU)) {
            someAction = "Power menu";
        } else if (customAction.equals(CFXConstants.SYSTEMUI_TASK_RECENTS)) {
            someAction = "Recent apps";
        } else if (customAction.equals(CFXConstants.SYSTEMUI_TASK_VOICE_SEARCH)) {
            someAction = "Voice search";
        } else if (customAction.equals(CFXConstants.SYSTEMUI_TASK_APP_SEARCH)) {
            someAction = "In-app search";
        } else if (customAction.equals(CFXConstants.SYSTEMUI_TASK_MENU)) {
            someAction = "Menu";
        } else {
            setSummary(mContext.getResources().getString(R.string.cfx_softkey_longpress_summary));
            return;
        }
        StringBuilder b = new StringBuilder()
                .append(someAction);
        setSummary(b.toString());
    }

    private void updateLpPackageSummary(String aPackage) {
        if (aPackage.startsWith("app:")) {
            aPackage = aPackage.substring(4);
        }
        StringBuilder b = new StringBuilder()
                .append(aPackage);
        setSummary(b.toString());

    }

    private void updateSummaryInitialState() {
        updateLpActionFromUri();
        String aSummary;
        if (mLpAction.equals(NO_LP) || !mLpAction.startsWith("app:")) {
            updateLpCustomSummary(mLpAction);
        } else if (mLpAction.startsWith("app:")) {
            aSummary = CFXUtils.getLabelFromComponent(mContext.getPackageManager(), mLpAction);
            // shouldn't be null here
            if (TextUtils.isEmpty(aSummary)) {
                setSummary(mContext.getResources()
                        .getString(R.string.cfx_softkey_longpress_summary));
                return;
            } else {
                updateLpPackageSummary(aSummary);
                return;
            }
        } else {
            setSummary(mContext.getResources().getString(R.string.cfx_softkey_longpress_summary));
        }
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
