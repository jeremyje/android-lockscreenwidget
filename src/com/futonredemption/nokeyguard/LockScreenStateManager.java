package com.futonredemption.nokeyguard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.preference.PreferenceManager;

public class LockScreenStateManager {

	private final Context context;
	private SharedPreferences prefs = null;
	
	public LockScreenStateManager(final Context context) {
		this.context = context;
	}

	public void determineIfLockShouldBeDeactivated(final LockScreenState state) {
		boolean prefActivateOnlyWhenCharging = isListeningToPowerChanges();
		boolean prefActivateOnlyWhenHeadphonesPluggedIn = false; //prefs.getBoolean("PrefActivateOnlyWhenHeadphonesPluggedIn", false);
		boolean prefActivateOnlyWhenDocked = false; //prefs.getBoolean("PrefActivateOnlyWhenDocked", false);
		
		// Assume that the lock will be disabled.
		state.Mode = Constants.MODE_Disabled;
		state.IsLockActive = false;
		
		if(prefActivateOnlyWhenCharging || prefActivateOnlyWhenHeadphonesPluggedIn || prefActivateOnlyWhenDocked) {
			
			// Since we have a restriction we need to build an or case against it to see if the lock really should be disabled.
			state.Mode = Constants.MODE_ConditionalToggle;
			state.IsLockActive = true;

			if(prefActivateOnlyWhenCharging) {
				if(isSystemCharging()) {
					state.IsLockActive = false;
				}
			}
			
			if(prefActivateOnlyWhenHeadphonesPluggedIn) {
				if(isHeadsetPluggedIn()) {
					state.IsLockActive = false;
				}
			}
		}
	}
	
	private boolean isSystemCharging() {
		boolean isCharging = false;
		final Intent powerstate = Intents.getBatteryState(context);
		if (powerstate != null) {
			final int plugstate = powerstate.getIntExtra(BatteryManager.EXTRA_PLUGGED, BatteryManager.BATTERY_PLUGGED_AC);
			if(plugstate == BatteryManager.BATTERY_PLUGGED_AC || plugstate == BatteryManager.BATTERY_PLUGGED_USB) {
				isCharging = true;
			}
		}

		return isCharging;
	}
	
	private boolean isHeadsetPluggedIn() {
		final HeadsetStateGetter getter = new HeadsetStateGetter(context);
		return getter.isHeadsetPluggedIn();
	}
	
	public boolean shouldHideNotification() {
		final SharedPreferences prefs = getPreferences();
		return prefs.getBoolean(Preferences.General.HideNotification, false);
	}
	
	public int getKeyguardEnabledPreference() {
		final SharedPreferences prefs = getPreferences();
		return prefs.getInt(Preferences.Internal.ToggleState, Constants.MODE_Enabled);
	}

	public void setKeyguardTogglePreference(final int param) {
		final SharedPreferences prefs = getPreferences();
		prefs.edit().putInt(Preferences.Internal.ToggleState, param).commit();
	}

	private SharedPreferences getPreferences() {
		
		if(prefs == null) {
			prefs = PreferenceManager.getDefaultSharedPreferences(context);
		}
		
		return prefs;
	}

	public boolean isListeningToPowerChanges() {
		final SharedPreferences prefs = getPreferences();
		return prefs.getBoolean("PrefActivateOnlyWhenCharging", false);
	}
}
