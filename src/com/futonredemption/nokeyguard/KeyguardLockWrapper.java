package com.futonredemption.nokeyguard;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;

public class KeyguardLockWrapper {

	// FIXME: Should we have synchronize locks on the method calls?
	private KeyguardManager _guard;
	private KeyguardLock _keyguardLock;
	private final Context _context;
	private boolean _isKeyguardEnabled;
	private boolean _isDisposed;

	public KeyguardLockWrapper(final Context context, final String tag) {
		_context = context;
		_guard = (KeyguardManager) _context.getSystemService(Context.KEYGUARD_SERVICE);
		_keyguardLock = _guard.newKeyguardLock(tag);
		_isKeyguardEnabled = true;
		_isDisposed = false;
	}

	public KeyguardManager getKeyguardManager() {
		return _guard;
	}

	public boolean disableKeyguard() {
		boolean performedAction = false;

		if (!_isDisposed && _isKeyguardEnabled) {
			_keyguardLock.disableKeyguard();
			_isKeyguardEnabled = false;
			performedAction = true;
		}

		return performedAction;
	}

	public boolean isKeyguardDisabled() {
		return !_isDisposed && !_isKeyguardEnabled;
	}
	
	public boolean enableKeyguard() {
		boolean performedAction = false;

		if (isKeyguardDisabled()) {
			_keyguardLock.reenableKeyguard();
			_isKeyguardEnabled = true;
			performedAction = true;
		}

		return performedAction;
	}

	public void dispose() {
		enableKeyguard();
		_isDisposed = true;
		_keyguardLock = null;
		_guard = null;
	}
}
