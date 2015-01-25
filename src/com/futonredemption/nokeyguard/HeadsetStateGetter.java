package com.futonredemption.nokeyguard;

import org.beryl.app.AndroidVersion;

import android.app.Service;
import android.content.Context;
import android.media.AudioManager;

public class HeadsetStateGetter {

	private final IHeadsetStateGetter _headsetStateGetter;
	
	static interface IHeadsetStateGetter {
		boolean isHeadsetPluggedIn();
	}
	
	public HeadsetStateGetter(Context context) {
		
		if(AndroidVersion.isEclairOrHigher()) {
			_headsetStateGetter = new EclairOrHigherHeadsetStateGetter(context);
		}
		else {
			_headsetStateGetter = new DonutOrLowerHeadsetStateGetter(context);
		}
	}
	
	public boolean isHeadsetPluggedIn() {
		return _headsetStateGetter.isHeadsetPluggedIn();
	}

	static class EclairOrHigherHeadsetStateGetter implements IHeadsetStateGetter {
		
		private final Context _context;
		EclairOrHigherHeadsetStateGetter(final Context context) {
			_context = context;
		}
		public boolean isHeadsetPluggedIn() {
			final AudioManager audioManager = (AudioManager)_context.getSystemService(Service.AUDIO_SERVICE);
			return audioManager.isBluetoothA2dpOn() || audioManager.isWiredHeadsetOn();
		}
	}
	
	static class DonutOrLowerHeadsetStateGetter implements IHeadsetStateGetter {
		
		private final Context _context;
		DonutOrLowerHeadsetStateGetter(final Context context) {
			_context = context;
		}
		
		public boolean isHeadsetPluggedIn() {
			final AudioManager audioManager = (AudioManager)_context.getSystemService(Service.AUDIO_SERVICE);
			return audioManager.isBluetoothA2dpOn();
		}
	}
}
