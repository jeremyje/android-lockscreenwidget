package com.futonredemption.nokeyguard.services;

import org.beryl.app.ComponentEnabler;
import org.beryl.app.ServiceBase;

import com.futonredemption.nokeyguard.AutoCancelingForegrounder;
import com.futonredemption.nokeyguard.Constants;
import com.futonredemption.nokeyguard.Intents;
import com.futonredemption.nokeyguard.KeyguardLockWrapper;
import com.futonredemption.nokeyguard.LockScreenState;
import com.futonredemption.nokeyguard.LockScreenStateManager;
import com.futonredemption.nokeyguard.R;
import com.futonredemption.nokeyguard.appwidgets.AppWidgetProvider1x1;
import com.futonredemption.nokeyguard.receivers.PowerStateChangedReceiver;
import com.futonredemption.nokeyguard.receivers.RelayRefreshWidgetReceiver;

import android.content.Intent;
import android.os.IBinder;

public class DisableKeyguardService extends ServiceBase {
	private Object _commandLock = new Object();

	private KeyguardLockWrapper _wrapper;
	
	private static final String KeyGuardTag = "KeyguardLockWrapper";
	
	public static final String RemoteAction_EnableKeyguard = "RemoteAction_EnableKeyguard";
	public static final String RemoteAction_DisableKeyguard = "RemoteAction_DisableKeyguard";
	public static final String RemoteAction_DisableKeyguardOnCharging = "RemoteAction_DisableKeyguardOnCharging";
	public static final String RemoteAction_RefreshWidgets = "RemoteAction_RefreshWidgets";
	public static final String RemoteAction_NotifyState = "RemoteAction_NotifyState";
	public static final String EXTRA_RemoteAction = "EXTRA_RemoteAction";
	public static final String EXTRA_ForceNotify = "EXTRA_ForceNotify";

	final RelayRefreshWidgetReceiver receiver = new RelayRefreshWidgetReceiver();
	
	private final AutoCancelingForegrounder foregrounder = new AutoCancelingForegrounder(this);
	private final LockScreenStateManager lockStateManager = new LockScreenStateManager(this);
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		_wrapper = new KeyguardLockWrapper(this, KeyGuardTag);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		foregrounder.stopForeground();
		_wrapper.dispose();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	protected int handleOnStartCommand(Intent intent, int flags, int startId) {
		handleCommand(intent);
		return START_STICKY;
	}
	
	private void handleCommand(final Intent intent) {
		synchronized (_commandLock) {
			
			if(intent != null) {
				final String remote_action = intent.getStringExtra(EXTRA_RemoteAction);
				
				// Backwards compatibility. If the old "disable on charging" preference is set then put it to enable keyguard.
				if (remote_action.equals(RemoteAction_EnableKeyguard) || remote_action.equals(RemoteAction_DisableKeyguardOnCharging)) {
					onEnableKeyguard();
				} else if (remote_action.equals(RemoteAction_DisableKeyguard)) {
					onDisableKeyguard();
				} else if (remote_action.equals(RemoteAction_RefreshWidgets)) {
					onRefreshWidgets();
				} else if (remote_action.equals(RemoteAction_NotifyState)) {
					onNotifyState();
				}else { /* On all else fails just refresh the widgets. */
					onRefreshWidgets();
				}
			} else {
				onDisableKeyguard();
			}
		}
	}

	private void updateAllWidgets() {
		final LockScreenState state = getLockScreenState();
		
		if(state.IsLockActive) {
			enableLockscreen();
		}
		else {
			disableLockscreen();
		}

		broadcastState(state);
		AppWidgetProvider1x1.UpdateAllWidgets(this, state);
	}

	private void broadcastState(LockScreenState state) {
		this.sendBroadcast(Intents.broadcastLockState(state));
	}

	private void disableLockscreen() {
		setLockscreenMode(false);
	}

	private void enableLockscreen() {
		setLockscreenMode(true);
	}

	private void setLockscreenMode(boolean enableLockscreen) {

		if (enableLockscreen) {
			_wrapper.enableKeyguard();
		} else {
			_wrapper.disableKeyguard();
		}

		if(enableLockscreen) {
			stopForeground();
		}
		else {
			beginForeground();
		}
	}

	public void beginForeground() {

		if(! foregrounder.isForegrounded()) {
			
			foregrounder.startForeground(
					R.drawable.ic_stat_nolock,
					R.string.lockscreen_is_off,
					R.string.select_to_configure,
					R.string.lockscreen_is_off,
					Intents.pendingShowPreferencesActivity(this));
		}
		
		// Some users don't like the notification so it will not appear but they don't get foregrounding.
		if(lockStateManager.shouldHideNotification()) {
			foregrounder.beginRemoveForeground();
		} else {
			foregrounder.beginChangeAction(
					R.drawable.ic_stat_nolock,
					R.string.lockscreen_is_off,
					R.string.tap_to_turn_on,
					R.string.lockscreen_is_off,
					Intents.pendingEnableKeyguard(this));
		}
	}
	
	public void stopForeground() {
		foregrounder.stopForeground();
	}

	private void onRefreshWidgets() {
		toggleComponents();
		updateAllWidgets();
		determineIfShouldDie();
	}

	private void toggleComponents() {
		final ComponentEnabler enabler = new ComponentEnabler(this);
		
		if(lockStateManager.isListeningToPowerChanges()) {
			enabler.enable(PowerStateChangedReceiver.class);
		} else {
			enabler.disable(PowerStateChangedReceiver.class);
		}
	}

	private LockScreenState getLockScreenState() {
		final LockScreenState state = new LockScreenState();
		
		state.Mode = lockStateManager.getKeyguardEnabledPreference();

		if (state.Mode == Constants.MODE_Enabled) {
			state.IsLockActive = true;
		} else {
			lockStateManager.determineIfLockShouldBeDeactivated(state);
		}
		
		return state;
	}
	
	private void onNotifyState() {
		broadcastState(getLockScreenState());
		determineIfShouldDie();
		
	}
	
	private void determineIfShouldDie() {
		// HACK: Sometimes this is called when it shouldn't. Always check to see if the lock is active otherwise quit.
		if(! _wrapper.isKeyguardDisabled()) {
			destroyKeyguard();
		}
	}

	private void onDisableKeyguard() {
		lockStateManager.setKeyguardTogglePreference(Constants.MODE_Disabled);
		updateAllWidgets();
	}

	private void onEnableKeyguard() {
		lockStateManager.setKeyguardTogglePreference(Constants.MODE_Enabled);
		updateAllWidgets();
		destroyKeyguard();
	}

	private void destroyKeyguard() {
		_wrapper.dispose();
		this.stopSelf();
	}

	@Override
	protected String getTag() {
		return DisableKeyguardService.class.getName();
	}
}
