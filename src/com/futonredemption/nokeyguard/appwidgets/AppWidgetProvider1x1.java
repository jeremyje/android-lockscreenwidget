package com.futonredemption.nokeyguard.appwidgets;

import com.futonredemption.nokeyguard.Constants;
import com.futonredemption.nokeyguard.Intents;
import com.futonredemption.nokeyguard.LockScreenState;
import com.futonredemption.nokeyguard.R;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class AppWidgetProvider1x1 extends AppWidgetProvider {
	
	public void onReceive(Context context, Intent intent) {
		
		// Handle the basic AppWidget intents.
		super.onReceive(context, intent);

		// Handle non-appwidget intents.
		final String action = intent.getAction();

		if (! AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
			refreshWidgets(context);
		}
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		refreshWidgets(context);
	}

	private void refreshWidgets(final Context context) {
		context.startService(Intents.refreshWidgets(context));
	}

	@Override
	public void onDisabled(Context context) {
		context.startService(Intents.enableKeyguard(context));
	}

	public static void UpdateAllWidgets(final Context context, final LockScreenState state) {
		int i, len;
		final AppWidgetManager widget_manager = AppWidgetManager.getInstance(context);
		final int[] ids1x1 = widget_manager.getAppWidgetIds(new ComponentName(context, AppWidgetProvider1x1.class));

		len = ids1x1.length;
		for (i = 0; i < len; i++) {
			AppWidgetProvider1x1.UpdateWidget(context, widget_manager, ids1x1[i], state);
		}
	}

	public static void UpdateWidget(final Context context, final AppWidgetManager widgetManager, final int widgetId, final LockScreenState state) {
		RemoteViews views = null;

		views = new RemoteViews(context.getPackageName(), R.layout.appwidget_1x1);

		PendingIntent toggleIntent = null;
		int iconId = 0;
		int indicatorId = 0;

		if (state.Mode == Constants.MODE_Disabled) {
			toggleIntent = Intents.pendingEnableKeyguard(context);
			indicatorId = R.drawable.appwidget_settings_ind_off_single;
		} else if (state.Mode == Constants.MODE_ConditionalToggle) {
			toggleIntent = Intents.pendingEnableKeyguard(context);
			indicatorId = R.drawable.appwidget_settings_ind_mid_single;
		} else {
			toggleIntent = Intents.pendingDisableKeyguard(context);
			indicatorId = R.drawable.appwidget_settings_ind_on_single;
		}

		if (state.IsLockActive) {
			iconId = R.drawable.ic_appwidget_screenlock_on;
		} else {
			iconId = R.drawable.ic_appwidget_screenlock_off;
		}

		views.setOnClickPendingIntent(R.id.main, toggleIntent);
		views.setOnClickPendingIntent(R.id.imgKeyguard, toggleIntent);
		views.setOnClickPendingIntent(R.id.indKeyguard, Intents.pendingShowPreferencesActivity(context));
		views.setImageViewResource(R.id.imgKeyguard, iconId);
		views.setImageViewResource(R.id.indKeyguard, indicatorId);
		widgetManager.updateAppWidget(widgetId, views);
	}
}
