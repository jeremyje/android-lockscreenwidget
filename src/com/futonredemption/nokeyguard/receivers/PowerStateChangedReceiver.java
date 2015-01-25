package com.futonredemption.nokeyguard.receivers;

import com.futonredemption.nokeyguard.Intents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PowerStateChangedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		refreshWidgets(context);
	}

	private void refreshWidgets(final Context context) {
		context.startService(Intents.refreshWidgets(context));
	}
}
